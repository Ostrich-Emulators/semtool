/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.edgemodelers.EdgeModeler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.ostrichemulators.semtool.poi.main.CSVReader;
import com.ostrichemulators.semtool.poi.main.DiskBackedLoadingSheetData;
import com.ostrichemulators.semtool.poi.main.ImportValidationException;
import com.ostrichemulators.semtool.poi.main.ImportValidationException.ErrorType;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportFileReader;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.DataIterator;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import com.ostrichemulators.semtool.poi.main.POIReader;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.api.ModificationExecutor;
import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.rdf.engine.edgemodelers.LegacyEdgeModeler;
import com.ostrichemulators.semtool.rdf.engine.edgemodelers.SemtoolEdgeModeler;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;

import com.ostrichemulators.semtool.util.UriBuilder;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getRDFStringValue;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getUriFromRawString;
import com.ostrichemulators.semtool.util.Utility;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * A class to handle loading files to an existing engine
 *
 * @author ryan
 */
public class EngineLoader {

	private static final Logger log = Logger.getLogger( EngineLoader.class );
	private static final Map<String, ImportFileReader> defaultExtReaderLkp
			= new HashMap<>();
	private final List<Statement> owls = new ArrayList<>();
	private final ValueFactory vf;
	private final Map<String, ImportFileReader> extReaderLkp = new HashMap<>();
	private final boolean stageInMemory;
	private final boolean lsInMemory;
	private boolean forceBaseUri;
	private RepositoryConnection myrc;
	private File stagingdir;
	private URI defaultBaseUri;
	private QaChecker qaer = new QaChecker();

	static {
		POIReader poi = new POIReader();
		CSVReader csv = new CSVReader();
		defaultExtReaderLkp.put( "xlsx", poi );
		defaultExtReaderLkp.put( "xls", poi );
		defaultExtReaderLkp.put( "csv", csv );
	}

	public EngineLoader( boolean stageInMem, boolean keepLoadingSheetsInMemory ) {
		stageInMemory = stageInMem;
		lsInMemory = keepLoadingSheetsInMemory;
		try {
			myrc = initForLoad();
		}
		catch ( RepositoryException | IOException ioe ) {
			log.error( ioe, ioe );
		}

		vf = myrc.getValueFactory();
	}

	public EngineLoader( boolean inmem ) {
		this( inmem, false );
	}

	public EngineLoader() {
		this( true );
	}

	/**
	 * Sets the Base URI when loading files.
	 *
	 * @param base the default URI to use
	 * @param overrideFile if true, use <code>base</code> instead of anything
	 * specified in the loading files
	 */
	public void setDefaultBaseUri( URI base, boolean overrideFile ) {
		defaultBaseUri = base;
		forceBaseUri = overrideFile;
	}

	public void setReader( String extension, ImportFileReader rdr ) {
		extReaderLkp.put( extension, rdr );
	}

	private RepositoryConnection initForLoad()
			throws RepositoryException, IOException {

		Repository repo = null;

		NotifyingSailBase store = null;
		if ( stageInMemory ) {
			store = new MemoryStore();
		}
		else {
			stagingdir = File.createTempFile( "semoss-staging-", "" );
			stagingdir.delete(); // get rid of the file, and make it a directory
			stagingdir.mkdirs();
			log.debug( "staging load in: " + stagingdir );
			store = new NativeStore( stagingdir, "spoc" );
		}

		repo = new SailRepository( store );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		initNamespaces( rc );
		return rc;
	}

	/**
	 * Loads the given files to the given engine
	 *
	 * @param toload the files to load
	 * @param engine the engine to load it to
	 * @param createmetamodel create the metamodel from the input files?
	 * @param conformanceErrors if not-null, conformance will be checked and
	 * errors added here
	 * @return the metamodel statements created. will always be empty if the
	 * <code>createmetamodel</code> is false
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws com.ostrichemulators.semtool.poi.main.ImportValidationException
	 */
	public Collection<Statement> loadToEngine( Collection<File> toload, IEngine engine,
			boolean createmetamodel, ImportData conformanceErrors )
			throws RepositoryException, IOException, ImportValidationException {

		qaer.loadCaches( engine );
		Set<Statement> mmstmts = new HashSet<>();

		for ( File fileToLoad : toload ) {
			ImportFileReader reader = getReader( fileToLoad );
			if ( null == reader ) {
				String ext = FilenameUtils.getExtension( fileToLoad.getName() ).toLowerCase();
				switch ( ext ) {
					case "ttl":
						add( RDFFormat.TURTLE, fileToLoad );
						break;
					case "n3":
						add( RDFFormat.N3, fileToLoad );
						break;
					case "nt":
						add( RDFFormat.NTRIPLES, fileToLoad );
						break;
					case "rdf":
						add( RDFFormat.RDFXML, fileToLoad );
						break;
					default:
						throw new ImportValidationException( ErrorType.INVALID_DATA,
								"unhandled file type: " + fileToLoad );
				}
			}
			else {
				reader.keepLoadInMemory( lsInMemory );
				ImportData data = reader.readOneFile( fileToLoad );
				data.findPropertyLinks();
				ImportMetadata im = data.getMetadata();
				im.setAutocreateMetamodel( createmetamodel );
				loadIntermediateData( data, engine, conformanceErrors );
			}

			mmstmts.addAll( moveStagingToEngine( engine, createmetamodel ) );
		}

		return mmstmts;
	}

	/**
	 * Loads the given data to the engine. The input data is
	 * {@link ImportData#release() released} during the load, and is unusable
	 * afterwards
	 *
	 * @param data The data to load. The data is consumed during the load, so this
	 * object is unusable once this function completes.
	 * @param engine
	 * @param conformanceErrors
	 * @throws RepositoryException
	 * @throws IOException
	 * @throws ImportValidationException
	 */
	public void loadToEngine( ImportData data, IEngine engine,
			ImportData conformanceErrors ) throws RepositoryException, IOException, ImportValidationException {
		qaer.loadCaches( engine );
		loadIntermediateData( data, engine, conformanceErrors );
		moveStagingToEngine( engine, data.getMetadata().isAutocreateMetamodel() );
	}

	private void loadIntermediateData( ImportData data, IEngine engine,
			ImportData conformanceErrors ) throws ImportValidationException, IOException {

		ImportMetadata im = data.getMetadata();
		// fill in anything not already set. In legacy mode, nothing is set,
		// but the metadata tab might not set these variables, either
		if ( null == im.getBase() || forceBaseUri ) {
			im.setBase( defaultBaseUri );
		}

		if ( null == im.getBase() ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"No Base URI specified in either the EngineLoader or the file" );
		}

		if ( null == im.getSchemaBuilder() ) {
			im.setSchemaBuilder( engine.getSchemaBuilder().toString() );
		}

		if ( null == im.getDataBuilder() ) {
			im.setDataBuilder( engine.getDataBuilder().toString() );
		}

		// we want to search all namespaces, but use the metadata's first
		Map<String, String> namespaces = engine.getNamespaces();
		namespaces.putAll( im.getNamespaces() );

		try {
			List<Statement> stmts = new ArrayList<>();
			for ( String[] stmt : data.getStatements() ) {
				URI s = getUriFromRawString( stmt[0], namespaces );
				URI p = getUriFromRawString( stmt[1], namespaces );
				Value o = getRDFStringValue( stmt[2], namespaces, vf );

				if ( null == s || null == p || null == o ) {
					throw new ImportValidationException( ErrorType.INVALID_DATA,
							"Could not create metadata statement" + Arrays.toString( stmt ) );
				}

				Statement st = new StatementImpl( s, p, o );
				stmts.add( st );
			}

			if ( !stmts.isEmpty() ) {
				myrc.begin();
				myrc.add( stmts );
				myrc.commit();
			}

			// create all metamodel triples, even if we don't add them to the repository
			EdgeModeler modeler = getEdgeModeler( EngineUtil2.getReificationStyle( engine ) );
			myrc.begin();
			modeler.createMetamodel( data, namespaces, myrc );
			myrc.commit();

			for ( LoadingSheetData n : data.getNodes() ) {
				addToStaging( n, engine, data );
			}

			qaer.separateConformanceErrors( data, conformanceErrors, engine );

			for ( LoadingSheetData r : data.getRels() ) {
				addToStaging( r, engine, data );
			}

			data.release();

			myrc.begin();
			URI ebase = engine.getBaseUri();
			myrc.add( ebase, MetadataConstants.VOID_SUBSET, im.getBase() );
			myrc.add( ebase, OWL.IMPORTS, im.getBase() );

			myrc.add( im.getBase(), RDF.TYPE, MetadataConstants.VOID_DS );
			myrc.add( im.getBase(), RDF.TYPE, OWL.ONTOLOGY );
			myrc.commit();
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			try {
				myrc.rollback();
			}
			catch ( RepositoryException rr ) {
				log.warn( rr, rr );
			}
		}
	}

	/**
	 * Gets a reader for the given file, based on extension. If no reader has been
	 * explicitly set using {@link #setReader(java.lang.String, gov.va.semoss.poi.main.ImportFileReader)
	 * }, then this function relies on {@link #getDefaultReader(java.io.File) }
	 * to determine the appropriate reader for this file.
	 *
	 * @param toload
	 * @return
	 */
	public ImportFileReader getReader( File toload ) {
		String ext = FilenameUtils.getExtension( toload.getName() ).toLowerCase();
		ImportFileReader rdr = ( extReaderLkp.containsKey( ext )
				? extReaderLkp.get( ext ) : getDefaultReader( toload ) );
		return rdr;
	}

	public static ImportFileReader getDefaultReader( File toload ) {
		String ext = FilenameUtils.getExtension( toload.getName() ).toLowerCase();
		ImportFileReader rdr = defaultExtReaderLkp.get( ext );
		return rdr;
	}

	public Collection<Statement> getOwlData() {
		return owls;
	}

	private void add( RDFFormat format, File f ) throws IOException, RepositoryException {
		try {
			log.debug( "adding data from " + format + " file: " + f );
			myrc.add( f, f.toURI().toString(), format );
		}
		catch ( RDFParseException rdfe ) {
			log.error( rdfe, rdfe );
		}
	}

	public void clear() {
		try {
			myrc.clear();
			initNamespaces( myrc );

			owls.clear();
			qaer.clear();
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}
	}

	public void release() {
		clear();
		qaer.release();

		try {
			myrc.close();
		}
		catch ( Exception ioe ) {
			log.warn( ioe, ioe );
		}
		try {
			myrc.getRepository().shutDown();
		}
		catch ( Exception ioe ) {
			log.warn( ioe, ioe );
		}

		FileUtils.deleteQuietly( stagingdir );
	}

	/**
	 * Adds the given loading sheet data to the staging repository
	 *
	 * @param sheet
	 * @param engine
	 * @param alldata
	 * @throws ImportValidationException
	 * @throws RepositoryException
	 */
	private void addToStaging( LoadingSheetData sheet, IEngine engine,
			ImportData alldata ) throws ImportValidationException, RepositoryException {
		log.debug( "loading " + sheet.getName() + " to staging repository" );
		// we want to search all namespaces, but use the metadata's first
		ImportMetadata metas = alldata.getMetadata();
		Map<String, String> namespaces = engine.getNamespaces();
		namespaces.putAll( metas.getNamespaces() );
		EdgeModeler modeler = getEdgeModeler( EngineUtil2.getReificationStyle( engine ) );

		try {
			myrc.begin();
			if ( sheet.isRel() ) {
				DataIterator lit = sheet.iterator();
				while ( lit.hasNext() ) {
					LoadingNodeAndPropertyValues nap = lit.next();
					log.debug( "loading: " + nap.getSubject() + "..." + nap.getRelname()
							+ "..." + nap.getObject() );
					try {
						modeler.addRel( nap, namespaces, sheet, metas, myrc );
					}
					catch ( RuntimeException re ) {
						log.error( re, re );
					}
				}
			}
			else {
				DataIterator lit = sheet.iterator();
				while ( lit.hasNext() ) {
					LoadingNodeAndPropertyValues nap = lit.next();
					modeler.addNode( nap, namespaces, sheet, metas, myrc );
				}
			}
			myrc.commit();
			// myrc.close();
			// myrc = myrc.getRepository().getConnection();
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
			try {
				myrc.rollback();
			}
			catch ( RepositoryException rex ) {
				log.warn( rex, rex );
			}
			throw re;
		}
	}

	/**
	 * Moves the statements from the loading RC to the given engine. The internal
	 * repository is committed before the copy happens
	 *
	 * @param engine
	 * @param copyowls
	 * @param fileJustLoaded the file that was just loaded
	 * @return the metamodel statements. Will always be empty if
	 * <code>copyowls</code> is false
	 * @throws RepositoryException
	 */
	private Collection<Statement> moveStagingToEngine( IEngine engine,
			boolean copyowls ) throws RepositoryException {
		myrc.commit();
		log.debug( "moving staging data to engine" );
		Set<Statement> owlstmts = new HashSet<>();
		final RepositoryResult<Statement> stmts
				= myrc.getStatements( null, null, null, false );
		UriBuilder schema = engine.getSchemaBuilder();

		// we're done importing the files, so add all the statements to our engine
		ModificationExecutor mea = new ModificationExecutorAdapter() {
			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				initNamespaces( conn );

				conn.begin();
				while ( stmts.hasNext() ) {
					Statement s = stmts.next();
					conn.add( s );

					if ( copyowls && schema.contains( s.getSubject() ) ) {
						owlstmts.add( s );
					}
				}

				// NOTE: no commit here
			}
		};

		engine.execute( mea );

		if ( log.isTraceEnabled() ) {
			File exportfile
					= new File( FileUtils.getTempDirectory(), "file-load-export.nt" );
			try ( Writer fw = new BufferedWriter( new FileWriter( exportfile ) ) ) {
				myrc.export( new NTriplesWriter( fw ) );
			}
			catch ( RDFHandlerException | IOException ioe ) {
				log.warn( ioe, ioe );
			}
		}

		if ( copyowls ) {
			owls.addAll( owlstmts );
			engine.addOwlData( owlstmts );
		}

		engine.commit();
		return owlstmts;
	}

	/**
	 * BigData doesn't seem to handle moving statements between repositories, so
	 * this function "cleans" a statement so it can be added.
	 *
	 * @param stmt the statement to clean
	 * @param vf the thing to make the URIs/Literals from
	 * @return a "cleaned" statement that won't cause problems
	 */
	public static Statement cleanStatement( Statement stmt, ValueFactory vf ) {
		// URI s = URI.class.cast( cleanValue( stmt.getSubject(), vf ) );
		Value sv = cleanValue( stmt.getSubject(), vf );
		URI p = URI.class.cast( cleanValue( stmt.getPredicate(), vf ) );
		Value v = cleanValue( stmt.getObject(), vf );

		return ( sv instanceof BNode )
				? new StatementImpl( BNode.class.cast( sv ), p, v )
				: new StatementImpl( URI.class.cast( sv ), p, v );
	}

	/**
	 * "Cleans" a value for BigData
	 *
	 * @param v the value that needs cleaning
	 * @param vf the value factory to make the new Value from
	 * @return a value that won't make BigData bomb
	 */
	public static Value cleanValue( Value v, ValueFactory vf ) {
		Value newv;
		if ( v instanceof URI ) {
			newv = vf.createURI( v.stringValue() );
		}
		else if ( v instanceof BNode ) {
			newv = vf.createBNode( v.stringValue() );
		}
		else {
			Literal oldv = Literal.class.cast( v );
			if ( null != oldv.getLanguage() ) {
				newv = vf.createLiteral( oldv.stringValue(), oldv.getLanguage() );
			}
			else {
				newv = vf.createLiteral( oldv.stringValue(), oldv.getDatatype() );
			}
		}

		return newv;
	}

	/**
	 * Method to add the common namespaces into the namespace hash of our
	 * RepositoryConnection. This function starts and commits a transaction.
	 *
	 * @param conn the connection to add the namespaces to
	 *
	 * @throws org.openrdf.repository.RepositoryException
	 */
	private static void initNamespaces( RepositoryConnection conn ) throws RepositoryException {

		conn.begin();
		for ( Map.Entry<String, String> e : Utility.DEFAULTNAMESPACES.entrySet() ) {
			conn.setNamespace( e.getKey(), e.getValue() );
		}
		conn.commit();
	}

	public static void initNamespaces( ImportData conn ) {
		conn.getMetadata().setNamespaces( Utility.DEFAULTNAMESPACES );
	}

	public EdgeModeler getEdgeModeler( ReificationStyle reif ) {
		EdgeModeler modeler = null;
		switch ( reif ) {
			case SEMTOOL:
				modeler = new SemtoolEdgeModeler( qaer );
				break;
			case LEGACY:
				modeler = new LegacyEdgeModeler( qaer );
				break;
			default:
				throw new IllegalArgumentException( "Unhandled reification style: " + reif );
		}

		return modeler;
	}

	private static ImportData cacheOnDisk( ImportData data ) throws IOException {
		ImportData copy = new ImportData( data.getMetadata() );
		for ( LoadingSheetData lsd : data.getSheets() ) {
			copy.add( new DiskBackedLoadingSheetData( lsd ) );
		}

		return copy;
	}
}
