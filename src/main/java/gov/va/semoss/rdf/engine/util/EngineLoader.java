/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.rdf.engine.edgemodelers.EdgeModeler;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.remote.BigdataSailFactory;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;

import gov.va.semoss.model.vocabulary.SEMOSS;
import gov.va.semoss.model.vocabulary.VAS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import gov.va.semoss.poi.main.CSVReader;
import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportFileReader;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.poi.main.POIReader;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import static gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler.getRDFStringValue;
import static gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler.getUriFromRawString;
import static gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler.isUri;
import gov.va.semoss.rdf.engine.edgemodelers.LegacyEdgeModeler;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import info.aduna.iteration.Iterations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * A class to handle loading files to an existing engine
 *
 * @author ryan
 */
public class EngineLoader {

	private static final Logger log = Logger.getLogger( EngineLoader.class );
	private final boolean stageInMemory;
	private final List<Statement> owls = new ArrayList<>();
	private BigdataSailRepositoryConnection myrc;
	private File stagingdir;
	private final Map<String, URI> schemaNodes = new HashMap<>();
	private final Map<ConceptInstanceCacheKey, URI> dataNodes = new HashMap<>();
	private final Map<String, URI> relationClassCache = new HashMap<>();
	private final Map<String, URI> relationCache = new HashMap<>();
	private final ValueFactory vf;
	private final Map<String, ImportFileReader> extReaderLkp = new HashMap<>();
	private URI defaultBaseUri;
	private boolean forceBaseUri;

	public static enum CacheType {

		CONCEPTCLASS, RELATIONCLASS, RELATION
	};

	public EngineLoader( boolean inmem ) {
		stageInMemory = inmem;
		try {
			myrc = initForLoad();
		}
		catch ( RepositoryException | IOException ioe ) {
			log.error( ioe, ioe );
		}

		vf = myrc.getValueFactory();

		POIReader poi = new POIReader();
		CSVReader csv = new CSVReader();
		extReaderLkp.put( "xlsx", poi );
		extReaderLkp.put( "xls", poi );
		extReaderLkp.put( "csv", csv );
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

	private BigdataSailRepositoryConnection initForLoad()
			throws RepositoryException, IOException {

		BigdataSailRepository repo;
		Properties props = new Properties();
		props.setProperty( BigdataSail.Options.BUFFER_CAPACITY, "100000" );
		props.setProperty( BigdataSail.Options.INITIAL_EXTENT, "10485760" );
		if ( stageInMemory ) {
			repo = BigdataSailFactory.createRepository( props,
					(BigdataSailFactory.Option) null );
		}
		else {
			stagingdir = File.createTempFile( "semoss-staging-", "" );
			stagingdir.delete(); // get rid of the file, and make it a directory
			stagingdir.mkdirs();
			log.debug( "staging load in: " + stagingdir );

			String jnl = new File( stagingdir, "stage.jnl" ).getAbsolutePath();
			repo = BigdataSailFactory.createRepository( props, jnl,
					(BigdataSailFactory.Option) null );
		}

		repo.initialize();
		BigdataSailRepositoryConnection rc = repo.getConnection();
		initNamespaces( rc );

		return rc;
	}

	public void cacheUris( CacheType type, Map<String, URI> newtocache ) {
		if ( CacheType.CONCEPTCLASS == type ) {
			schemaNodes.putAll( newtocache );
		}
		else if ( CacheType.RELATIONCLASS == type ) {
			relationClassCache.putAll( newtocache );
		}
		else if ( CacheType.RELATION == type ) {
			relationCache.putAll( newtocache );
		}
		else {
			throw new IllegalArgumentException( "unhandled cache type: " + type );
		}
	}

	public void cacheConceptInstances( Map<String, URI> instances, String typelabel ) {
		for ( Map.Entry<String, URI> en : instances.entrySet() ) {
			String l = en.getKey();
			URI uri = en.getValue();

			ConceptInstanceCacheKey key = new ConceptInstanceCacheKey( typelabel, l );
			//log.debug( "conceptinstances : " + key + " -> " + en.getValue() );
			dataNodes.put( key, uri );
		}
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
	 * @throws gov.va.semoss.poi.main.ImportValidationException
	 */
	public Collection<Statement> loadToEngine( Collection<File> toload, IEngine engine,
			boolean createmetamodel, ImportData conformanceErrors )
			throws RepositoryException, IOException, ImportValidationException {

		preloadCaches( engine );
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
				ImportData data = reader.readOneFile( fileToLoad );
				data.findPropertyLinks();
				ImportMetadata im = data.getMetadata();
				im.setAutocreateMetamodel( createmetamodel );
				loadIntermediateData( data, engine, conformanceErrors );
			}

			mmstmts.addAll( moveLoadingRcToEngine( engine, createmetamodel ) );
		}

		return mmstmts;
	}

	public void loadToEngine( ImportData data, IEngine engine,
			ImportData conformanceErrors ) throws RepositoryException, IOException, ImportValidationException {
		preloadCaches( engine );
		loadIntermediateData( data, engine, conformanceErrors );
		moveLoadingRcToEngine( engine, data.getMetadata().isAutocreateMetamodel() );
	}

	private void loadIntermediateData( ImportData data, IEngine engine,
			ImportData conformanceErrors ) throws ImportValidationException {

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
			im.setDataBuilder( im.getBase().stringValue() );
		}

		// we want to search all namespaces, but use the metadata's first
		Map<String, String> namespaces = engine.getNamespaces();
		namespaces.putAll( data.getMetadata().getNamespaces() );

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
				myrc.add( stmts );
			}

			// create all metamodel triples, even if we don't add them to the repository
			createMetamodel( data, namespaces );

			for ( LoadingSheetData n : data.getNodes() ) {
				addToEngine( n, engine, data );
			}

			separateConformanceErrors( data, conformanceErrors, engine );

			for ( LoadingSheetData r : data.getRels() ) {
				addToEngine( r, engine, data );
			}

			URI ebase = engine.getBaseUri();
			myrc.add( ebase, MetadataConstants.VOID_SUBSET, data.getMetadata().getBase() );
			myrc.add( data.getMetadata().getBase(), RDF.TYPE, MetadataConstants.VOID_DS );
			myrc.add( data.getMetadata().getBase(), RDF.TYPE, OWL.ONTOLOGY );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
	}

	/**
	 * Separates any non-conforming data from the loading data. This removes the
	 * offending data from <code>data</code> and puts them in <code>errors</code>
	 *
	 * @param data the data to check for errors
	 * @param errors where to put non-conforming data. If null, this function does
	 * nothing
	 * @param engine the engine to check against
	 */
	public void separateConformanceErrors( ImportData data, ImportData errors,
			IEngine engine ) {
		if ( null != errors ) {
			for ( LoadingSheetData d : data.getSheets() ) {
				List<LoadingNodeAndPropertyValues> errs
						= checkConformance( d, engine, false );

				if ( !errs.isEmpty() ) {
					LoadingSheetData errdata = LoadingSheetData.copyHeadersOf( d );
					errdata.setProperties( d.getPropertiesAndDataTypes() );
					errors.add( errdata );

					Set<LoadingNodeAndPropertyValues> errvals = new HashSet<>();
					List<LoadingNodeAndPropertyValues> reldata = d.getData();

					for ( LoadingNodeAndPropertyValues nap : errs ) {
						errvals.add( nap );
						errdata.add( nap );
					}

					reldata.removeAll( errvals );
				}
			}
		}
	}

	public ImportFileReader getReader( File toload ) {
		String ext = FilenameUtils.getExtension( toload.getName() ).toLowerCase();
		ImportFileReader rdr = extReaderLkp.get( ext );
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
			schemaNodes.clear();
			dataNodes.clear();
			relationClassCache.clear();
			relationCache.clear();
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}
	}

	public void release() {
		clear();

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
	 * Checks conformance of the given data. The <code>data</code> argument will
	 * be updated when errors are found. Only relationship data can be
	 * non-conforming.
	 *
	 * @param data the data to check
	 * @param eng the engine to check against. Can be null if
	 * <code>loadcaches</code> is false
	 * @param loadcaches call
	 * {@link #preloadCaches(gov.va.semoss.rdf.engine.api.IEngine)} first
	 * @return a list of all {@link LoadingNodeAndPropertyValues} that fail the
	 * check
	 */
	public List<LoadingNodeAndPropertyValues> checkConformance( LoadingSheetData data,
			IEngine eng, boolean loadcaches ) {
		List<LoadingNodeAndPropertyValues> failures = new ArrayList<>();

		if ( loadcaches ) {
			preloadCaches( eng );
		}

		String stype = data.getSubjectType();
		String otype = data.getObjectType();

		for ( LoadingNodeAndPropertyValues nap : data.getData() ) {
			// check that the subject and object are in our instance cache
			ConceptInstanceCacheKey skey
					= new ConceptInstanceCacheKey( stype, nap.getSubject() );
			nap.setSubjectIsError( !dataNodes.containsKey( skey ) );

			if ( data.isRel() ) {
				ConceptInstanceCacheKey okey
						= new ConceptInstanceCacheKey( otype, nap.getObject() );
				nap.setObjectIsError( !dataNodes.containsKey( okey ) );
			}

			if ( nap.hasError() ) {
				failures.add( nap );
			}
		}

		return failures;
	}

	/**
	 * Checks for an instance of the given type and label.
	 * {@link #preloadCaches(gov.va.semoss.rdf.engine.api.IEngine)} MUST be called
	 * prior to this function to have any hope at a true result
	 *
	 * @param type
	 * @param label
	 * @return true, if the type/label matches a cached value
	 */
	public boolean instanceExists( String type, String label ) {
		return dataNodes.containsKey( new ConceptInstanceCacheKey( type, label ) );
	}

	/**
	 * Checks that the Loading Sheet's {@link LoadingSheetData#subjectType},
	 * {@link LoadingSheetData#objectType}, and
	 * {@link LoadingSheetData#getProperties()} exist in the given engine
	 *
	 * @param data the data to check
	 * @param eng the engine to check against
	 * @param loadcaches call {@link EngineUtil#preloadCaches(gov.va.semoss.rdf.engine.api.IEngine,
	 * gov.va.semoss.rdf.engine.util.EngineLoader) } first
	 * @return the same loading sheet as the <code>data</code> arg
	 */
	public LoadingSheetData checkModelConformance( LoadingSheetData data,
			IEngine eng, boolean loadcaches ) {
		if ( loadcaches ) {
			preloadCaches( eng );
		}

		data.setSubjectTypeIsError( !schemaNodes.containsKey( data.getSubjectType() ) );

		if ( data.isRel() ) {
			data.setObjectTypeIsError( !schemaNodes.containsKey( data.getObjectType() ) );
			data.setRelationIsError( !relationClassCache.containsKey( data.getRelname() ) );
		}

		for ( Map.Entry<String, URI> en : data.getPropertiesAndDataTypes().entrySet() ) {
			data.setPropertyIsError( en.getKey(), !relationClassCache.containsKey( en.getKey() ) );
		}

		return data;
	}

	public void preloadCaches( IEngine engine ) {
		final Map<String, URI> map = new HashMap<>();
		String subpropq = "SELECT ?uri ?label WHERE { ?uri rdfs:label ?label . ?uri ?isa ?type }";
		VoidQueryAdapter vqa = new VoidQueryAdapter( subpropq ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				map.put( set.getValue( "label" ).stringValue(),
						URI.class.cast( cleanValue( set.getValue( "uri" ), fac ) ) );
			}

			@Override
			public void start( List<String> bnames ) {
				super.start( bnames );
				map.clear();
			}
		};
		vqa.useInferred( true );
		UriBuilder owlb = engine.getSchemaBuilder();

		try {
			URI type = owlb.getRelationUri().build();
			vqa.bind( "type", type );
			vqa.bind( "isa", RDFS.SUBPROPERTYOF );
			engine.query( vqa );

			Map<String, URI> cacheo = new HashMap<>();
			Map<String, URI> cacheb = new HashMap<>();
			for ( Map.Entry<String, URI> en : map.entrySet() ) {
				if ( owlb.contains( en.getValue() ) ) {
					cacheo.put( en.getKey(), en.getValue() );
				}
				else {
					cacheb.put( en.getKey(), en.getValue() );
				}
			}

			cacheUris( CacheType.RELATIONCLASS, cacheo );
			cacheUris( CacheType.RELATION, cacheb );

			vqa.bind( "isa", RDFS.SUBCLASSOF );
			type = owlb.getConceptUri().build();
			vqa.bind( "type", type );
			engine.query( vqa );
			cacheUris( CacheType.CONCEPTCLASS, map );

			vqa.bind( "isa", RDF.TYPE );
			Map<String, URI> concepts = new HashMap<>( map );
			for ( Map.Entry<String, URI> en : concepts.entrySet() ) {
				vqa.bind( "type", en.getValue() );

				engine.query( vqa );
				cacheConceptInstances( map, en.getKey() );
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( e, e );
		}
	}

	private void addToEngine( LoadingSheetData sheet, IEngine engine,
			ImportData alldata ) throws RepositoryException {

		// we want to search all namespaces, but use the metadata's first
		ImportMetadata metas = alldata.getMetadata();
		Map<String, String> namespaces = engine.getNamespaces();
		namespaces.putAll( metas.getNamespaces() );
		EdgeModeler modeler = getEdgeModeler( engine );

		if ( sheet.isRel() ) {
			for ( LoadingNodeAndPropertyValues nap : sheet.getData() ) {
				modeler.addRel( nap, namespaces, sheet, metas, myrc );
			}
		}
		else {
			for ( LoadingNodeAndPropertyValues nap : sheet.getData() ) {
				modeler.addNode( nap, namespaces, sheet, metas, myrc );
			}
		}
	}

	private void createMetamodel( ImportData alldata, Map<String, String> namespaces )
			throws RepositoryException {
		ImportMetadata metas = alldata.getMetadata();
		UriBuilder schema = metas.getSchemaBuilder();
		boolean save = metas.isAutocreateMetamodel();

		for ( LoadingSheetData sheet : alldata.getSheets() ) {

			String stype = sheet.getSubjectType();
			if ( !schemaNodes.containsKey( stype ) ) {
				boolean nodeAlreadyMade = isUri( stype, namespaces );

				URI uri = ( nodeAlreadyMade
						? getUriFromRawString( stype, namespaces )
						: schema.build( stype ) );
				schemaNodes.put( stype, uri );

				if ( save && !nodeAlreadyMade ) {
					myrc.add( uri, RDF.TYPE, OWL.CLASS );
					myrc.add( uri, RDFS.LABEL, vf.createLiteral( stype ) );
					myrc.add( uri, RDFS.SUBCLASSOF, schema.getConceptUri().build() );
				}
			}

			if ( sheet.isRel() ) {
				String otype = sheet.getObjectType();
				if ( !schemaNodes.containsKey( otype ) ) {
					boolean nodeAlreadyMade = isUri( otype, namespaces );

					URI uri = ( nodeAlreadyMade
							? getUriFromRawString( otype, namespaces )
							: schema.build( otype ) );

					schemaNodes.put( otype, uri );

					if ( save && !nodeAlreadyMade ) {
						myrc.add( uri, RDF.TYPE, OWL.CLASS );
						myrc.add( uri, RDFS.LABEL, vf.createLiteral( otype ) );
						myrc.add( uri, RDFS.SUBCLASSOF, schema.getConceptUri().build() );
					}
				}

				String rellabel = sheet.getRelname();
				String longNodeType = stype + rellabel + otype;

				if ( !relationClassCache.containsKey( longNodeType ) ) {
					boolean relationAlreadyMade = isUri( rellabel, namespaces );

					URI ret = ( relationAlreadyMade
							? getUriFromRawString( rellabel, namespaces )
							: schema.getRelationUri( rellabel ) );
					URI relation = schema.getRelationUri().build();

					relationClassCache.put( longNodeType, ret );

					if ( save ) {
						if ( !relationAlreadyMade ) {
							myrc.add( ret, RDF.TYPE, OWL.OBJECTPROPERTY );
							myrc.add( ret, RDFS.LABEL, vf.createLiteral( rellabel ) );
							myrc.add( ret, RDFS.SUBPROPERTYOF, relation );
						}
						// myrc.add( suri, ret, schemaNodes.get( ocachekey ) );

						myrc.add( schema.getConceptUri().build(), RDF.TYPE, RDFS.CLASS );

						myrc.add( schema.getContainsUri(), RDFS.SUBPROPERTYOF, schema.getContainsUri() );
						myrc.add( relation, RDF.TYPE, RDF.PROPERTY );
					}
				}
			}

			for ( String propname : sheet.getProperties() ) {
				// check to see if we're actually a link to some
				// other node (and not really a new property
				if ( sheet.isLink( propname ) || schemaNodes.containsKey( propname ) ) {
					log.debug( "linking " + propname + " as a " + SEMOSS.has
							+ " relationship to " + schemaNodes.get( propname ) );

					relationClassCache.put( propname, SEMOSS.has );
					continue;
				}

				boolean alreadyMadeProp = isUri( propname, namespaces );

				if ( !relationClassCache.containsKey( propname ) ) {
					URI predicate;
					if ( alreadyMadeProp ) {
						predicate = getUriFromRawString( propname, namespaces );
					}
					else {
						// UriBuilder bb = schema.getRelationUri().add( Constants.CONTAINS );
						predicate = schema.build( propname );
					}
					relationClassCache.put( propname, predicate );
				}
				URI predicate = relationClassCache.get( propname );

				if ( save && !alreadyMadeProp ) {
					myrc.add( predicate, RDFS.LABEL, vf.createLiteral( propname ) );
					// myrc.add( predicate, RDF.TYPE, schema.getContainsUri() );
					myrc.add( predicate, RDFS.SUBPROPERTYOF, schema.getRelationUri().build() );

					if ( !metas.isLegacyMode() ) {
						myrc.add( predicate, RDFS.SUBPROPERTYOF, schema.getContainsUri() );
					}
				}
			}
		}
	}

	/**
	 * Moves the statements from the loading RC to the given engine. The internal
	 * repository is committed before the copy happens
	 *
	 * @param engine
	 * @param copyowls
	 * @return the metamodel statements. Will always be empty if
	 * <code>copyowls</code> is false
	 * @throws RepositoryException
	 */
	private Collection<Statement> moveLoadingRcToEngine( IEngine engine,
			boolean copyowls ) throws RepositoryException {
		myrc.commit();
		Set<Statement> owlstmts = new HashSet<>();
		final List<Statement> stmts
				= Iterations.asList( myrc.getStatements( null, null, null, false ) );

		// we're done importing the files, so add all the statements to our engine
		ModificationExecutor mea = new ModificationExecutorAdapter() {
			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				initNamespaces( conn );

				conn.begin();
				for ( Statement s : stmts ) {
					conn.add( cleanStatement( s, vf ) );
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
			UriBuilder schema = engine.getSchemaBuilder();
			for ( Statement stmt : stmts ) {
				if ( schema.contains( stmt.getSubject() ) ) {
					owlstmts.add( cleanStatement( stmt, vf ) );
				}
			}

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

	public EdgeModeler getEdgeModeler( IEngine eng ) {
		MetadataQuery mq = new MetadataQuery( VAS.reification );
		EdgeModeler modeler = null;
		try {
			eng.query( mq );
			String val = mq.getOne();
			URI reif = ( null == val ? Constants.ANYNODE : new URIImpl( val ) );
			if ( VAS.VASEMOSS_Reification.equals( reif ) || Constants.ANYNODE == reif ) {
				modeler = new LegacyEdgeModeler();
			}
			else if ( VAS.W3C_Reification.equals( reif ) ) {

			}
			else if ( VAS.RDR_Reification.equals( reif ) ) {

			}
			else {
				throw new IllegalArgumentException( "Unknown reification model: " + reif );
			}

			modeler.setCaches( schemaNodes, dataNodes, relationClassCache, relationCache );

		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException ex ) {

		}

		return modeler;
	}

	public static class ConceptInstanceCacheKey {

		private final String typelabel;
		private final String rawlabel;

		public ConceptInstanceCacheKey( String typelabel, String conceptlabel ) {
			this.typelabel = typelabel;
			this.rawlabel = conceptlabel;
		}

		public String getTypeLabel() {
			return typelabel;
		}

		public String getConceptLabel() {
			return rawlabel;
		}

		@Override
		public String toString() {
			return "instance " + typelabel + "<->" + rawlabel;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 89 * hash + Objects.hashCode( this.typelabel );
			hash = 89 * hash + Objects.hashCode( this.rawlabel );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final ConceptInstanceCacheKey other = (ConceptInstanceCacheKey) obj;
			if ( !Objects.equals( this.typelabel, other.typelabel ) ) {
				return false;
			}
			return ( Objects.equals( this.rawlabel, other.rawlabel ) );
		}
	}
}
