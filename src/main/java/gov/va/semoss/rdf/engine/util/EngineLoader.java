/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.remote.BigdataSailFactory;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;

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

import gov.va.semoss.poi.main.AbstractFileReader;
import gov.va.semoss.poi.main.CSVReader;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportFileReader;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.poi.main.POIReader;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import static gov.va.semoss.rdf.query.util.QueryExecutorAdapter.getCal;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UniqueSanitizer;
import gov.va.semoss.util.UriBuilder;
import info.aduna.iteration.Iterations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
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

	protected static final Pattern NAMEPATTERN
			= Pattern.compile( "(?:(?:\"([^\"]+)\")|([^@]+))@([a-z-A-Z]{1,8})" );
	protected static final Pattern DTPATTERN
			= Pattern.compile( "\"([^\\\\^]+)\"\\^\\^(.*)" );
	protected static final Pattern URISTARTPATTERN
			= Pattern.compile( "(^[A-Za-z_-]+://).*" );

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
	private final Set<URI> duplicates = new HashSet<>();

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

	public void setReader( String extension, ImportFileReader rdr ) {
		extReaderLkp.put( extension, rdr );
	}

	private BigdataSailRepositoryConnection initForLoad()
			throws RepositoryException, IOException {

		BigdataSailRepository repo;
		Properties props = new Properties();
		props.setProperty( BigdataSail.Options.BUFFER_CAPACITY, "100000" );
		if ( !stageInMemory ) {
			stagingdir = File.createTempFile( "semoss-staging-", "" );
			stagingdir.delete(); // get rid of the file, and make it a directory
			stagingdir.mkdirs();
			log.debug( "staging load in: " + stagingdir );

			props.setProperty( BigdataSail.Options.FILE,
					new File( stagingdir, "stage.jnl" ).getAbsolutePath() );
		}

		repo = BigdataSailFactory.createRepository( props,
				(BigdataSailFactory.Option) null );
		repo.initialize();
		BigdataSailRepositoryConnection rc = repo.getConnection();
		initNamespaces( rc );

		return rc;
	}

	public void cacheUris( AbstractFileReader.CacheType type,
			Map<String, URI> newtocache ) {

		// for ( Map.Entry<String, URI> en : newtocache.entrySet() ) {
		//	log.debug( type + " : " + en.getKey() + " -> " + en.getValue() );
		// }
		switch ( type ) {
			case CONCEPTCLASS:
				schemaNodes.putAll( newtocache );
				break;
			case RELATIONCLASS:
				relationClassCache.putAll( newtocache );
				break;
			case RELATION:
				relationCache.putAll( newtocache );
				break;
			default:
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
	 */
	public Collection<Statement> loadToEngine( Collection<File> toload, IEngine engine,
			boolean createmetamodel, ImportData conformanceErrors )
			throws RepositoryException, IOException {

		Set<Statement> mmstmts = new HashSet<>();

		for ( File fileToLoad : toload ) {
			try {
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
							log.error( "unhandled file type: " + fileToLoad );
					}
				}
				else {
					ImportData data = reader.readOneFile( fileToLoad );
					ImportMetadata im = data.getMetadata();
					im.setAutocreateMetamodel( createmetamodel );

					// fill in anything not already set. In legacy mode, nothing is set,
					// but the metadata tab might not set these variables, either
					if ( null == im.getSchemaBuilder() ) {
						im.setSchemaBuilder( engine.getSchemaBuilder().toString() );
					}
					if ( null == im.getDataBuilder() ) {
						im.setDataBuilder( engine.getDataBuilder().toString() );
					}
					if ( null == im.getBase() ) {
						im.setBase( engine.getBaseUri() );
					}

					loadIntermediateData( data, engine, conformanceErrors );
				}
			}
			catch ( IOException | RepositoryException e ) {
				log.error( e, e );
			}

			mmstmts.addAll( moveLoadingRcToEngine( engine, createmetamodel ) );
		}

		return mmstmts;
	}

	public void loadToEngine( ImportData data, IEngine engine,
			ImportData conformanceErrors ) throws RepositoryException, IOException {
		preloadCaches( engine );
		loadIntermediateData( data, engine, conformanceErrors );
		moveLoadingRcToEngine( engine, data.getMetadata().isAutocreateMetamodel() );
	}

	private void loadIntermediateData( ImportData data, IEngine engine,
			ImportData conformanceErrors ) {

		try {
			myrc.add( data.getStatements() );

			// we want to search all namespaces, but use the metadata's first
			Map<String, String> namespaces = engine.getNamespaces();
			namespaces.putAll( data.getMetadata().getNamespaces() );

			for ( Map.Entry<URI, String> en : data.getMetadata().getExtras().entrySet() ) {
				myrc.add( data.getMetadata().getBase(), en.getKey(),
						getRDFStringValue( en.getValue(), namespaces ) );
			}

			for ( LoadingSheetData n : data.getNodes() ) {
				addToEngine( n, engine, data.getMetadata() );
			}

			separateConformanceErrors( data, conformanceErrors, engine );

			for ( LoadingSheetData r : data.getRels() ) {
				addToEngine( r, engine, data.getMetadata() );
			}
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
			for ( LoadingSheetData d : data.getRels() ) {
				List<LoadingNodeAndPropertyValues> errs = checkConformance( d, engine, false );

				if ( !errs.isEmpty() ) {
					LoadingSheetData errdata
							= LoadingSheetData.relsheet( d.getName(), d.getSubjectType(),
									d.getObjectType(), d.getRelname() );
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

	public RepositoryConnection getConnection() {
		return myrc;
	}

	public Collection<Statement> getOwlData() {
		return owls;
	}

	private void add( RDFFormat format, File f )
			throws IOException, RepositoryException {
		try {
			log.debug( "adding data from " + format + " file: " + f );
			myrc.add( f, f.toURI().toString(), format );
		}
		catch ( RDFParseException rdfe ) {
			log.error( rdfe, rdfe );
		}
	}

	public void release() {
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
	 * @param eng the engine to check against
	 * @param loadcaches call {@link EngineUtil#preloadCaches(gov.va.semoss.rdf.engine.api.IEngine,
	 * gov.va.semoss.rdf.engine.util.EngineLoader) } first
	 * @return a list of all {@link LoadingNodeAndPropertyValues} that fail the
	 * check
	 */
	public List<LoadingNodeAndPropertyValues> checkConformance( LoadingSheetData data,
			IEngine eng, boolean loadcaches ) {
		List<LoadingNodeAndPropertyValues> failures = new ArrayList<>();

		if ( loadcaches ) {
			preloadCaches( eng );
		}

		if ( data.isRel() ) {
			String stype = data.getSubjectType();
			String otype = data.getObjectType();

			for ( LoadingNodeAndPropertyValues nap : data.getData() ) {
				// check that the subject and object are in our instance cache
				ConceptInstanceCacheKey skey
						= new ConceptInstanceCacheKey( stype, nap.getSubject() );
				ConceptInstanceCacheKey okey
						= new ConceptInstanceCacheKey( otype, nap.getObject() );

				nap.setSubjectIsError( !dataNodes.containsKey( skey ) );
				nap.setObjectIsError( !dataNodes.containsKey( okey ) );

				if ( nap.hasError() ) {
					// log.debug( nap );
					failures.add( nap );
				}
			}
		}

		return failures;
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
			data.setPropertyIsError( en.getKey(), !schemaNodes.containsKey( en.getKey() ) );
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
			URI uri = owlb.getRelationUri().build();
			vqa.bind( "type", uri );
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

			cacheUris( AbstractFileReader.CacheType.RELATIONCLASS, cacheo );
			cacheUris( AbstractFileReader.CacheType.RELATION, cacheb );

			vqa.bind( "isa", RDFS.SUBCLASSOF );
			uri = owlb.getConceptUri().build();
			vqa.bind( "type", uri );
			engine.query( vqa );
			cacheUris( AbstractFileReader.CacheType.CONCEPTCLASS, map );

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
			ImportMetadata metas ) throws RepositoryException {

		// we want to search all namespaces, but use the metadata's first
		Map<String, String> namespaces = engine.getNamespaces();
		namespaces.putAll( metas.getNamespaces() );

		// create all metamodel triples, even if we don't add them to the repository
		createMetamodel( sheet, namespaces, metas );

		if ( sheet.isRel() ) {
			for ( LoadingNodeAndPropertyValues nap : sheet.getData() ) {
				addRel( nap, namespaces, sheet, metas );
			}
		}
		else {
			for ( LoadingNodeAndPropertyValues nap : sheet.getData() ) {
				addNode( nap, namespaces, sheet, metas );
			}
		}
	}

	private URI ensureUnique( URI uri, String rawlabel ) {
		if ( duplicates.contains( uri ) ) {
			UriBuilder dupefixer = UriBuilder.getBuilder( uri.getNamespace() );
			dupefixer.setSanitizer( new UniqueSanitizer() );
			uri = dupefixer.build( rawlabel );
			duplicates.add( uri );
		}
		return uri;
	}

	private URI addRel( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
			LoadingSheetData sheet, ImportMetadata metas ) throws RepositoryException {

		String stype = nap.getSubjectType();
		String srawlabel = nap.getSubject();

		String otype = nap.getObjectType();
		String orawlabel = nap.getObject();

		// get both ends of the relationship...
		ConceptInstanceCacheKey skey = new ConceptInstanceCacheKey( stype, srawlabel );
		if ( !dataNodes.containsKey( skey ) ) {
			LoadingNodeAndPropertyValues filler
					= sheet.new LoadingNodeAndPropertyValues( srawlabel );
			addNode( filler, namespaces, sheet, metas );
		}
		URI subject = dataNodes.get( skey );

		ConceptInstanceCacheKey okey = new ConceptInstanceCacheKey( otype, orawlabel );
		if ( !dataNodes.containsKey( okey ) ) {
			LoadingSheetData lsd = LoadingSheetData.nodesheet( sheet.getName(), otype );
			LoadingNodeAndPropertyValues filler = lsd.add( orawlabel );
			addNode( filler, namespaces, lsd, metas );
		}
		URI object = dataNodes.get( okey );

		boolean alreadyMadeRel = isUri( sheet.getRelname(), namespaces );

		// ... and get a relationship that ties them together
		StringBuilder keybuilder = new StringBuilder( nap.getSubjectType() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( nap.getSubject() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( sheet.getRelname() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( nap.getObjectType() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( nap.getObject() );

		String lkey = keybuilder.toString();
		if ( !relationCache.containsKey( lkey ) ) {
			URI connector;
			String rellocalname;
			if ( alreadyMadeRel ) {
				rellocalname = srawlabel + Constants.RELATION_URI_CONCATENATOR + orawlabel;
				connector = metas.getDataBuilder().getRelationUri().build( rellocalname );
			}
			else {
				UriBuilder typebuilder
						= metas.getDataBuilder().getRelationUri().add( sheet.getRelname() );
				rellocalname = srawlabel + Constants.RELATION_URI_CONCATENATOR + orawlabel;
				connector = typebuilder.add( rellocalname ).build();
			}

			connector = ensureUnique( connector, rellocalname );
			relationCache.put( lkey, connector );
		}

		String typekey = stype + sheet.getRelname() + otype;
		URI relClassBaseURI = relationClassCache.get( typekey );

		URI connector = relationCache.get( lkey );
		if ( metas.isAutocreateMetamodel() ) {
			myrc.add( connector, RDFS.SUBPROPERTYOF, relClassBaseURI );
			myrc.add( connector, RDFS.LABEL, vf.createLiteral( srawlabel
					+ Constants.RELATION_LABEL_CONCATENATOR + orawlabel ) );
		}
		myrc.add( subject, connector, object );

		addProperties( connector, nap, namespaces, sheet, metas );

		return connector;
	}

	private URI addNode( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
			LoadingSheetData sheet, ImportMetadata metas )
			throws RepositoryException {

		String typename = nap.getSubjectType();
		String rawlabel = nap.getSubject();

		URI typeuri = schemaNodes.get( typename );

		boolean nodeIsAlreadyUri = isUri( rawlabel, namespaces );

		ConceptInstanceCacheKey key = new ConceptInstanceCacheKey( typename, rawlabel );
		if ( !dataNodes.containsKey( key ) ) {
			URI subject;

			if ( nodeIsAlreadyUri ) {
				subject = getUriFromRawString( rawlabel, namespaces );
			}
			else {
				if ( metas.isAutocreateMetamodel() ) {
					UriBuilder nodebuilder = metas.getDataBuilder().getConceptUri();
					if ( !typename.contains( ":" ) ) {
						nodebuilder.add( typename );
					}
					subject = nodebuilder.add( rawlabel ).build();
				}
				else {
					subject = metas.getDataBuilder().add( rawlabel ).build();
				}

				subject = ensureUnique( subject, rawlabel );
			}
			dataNodes.put( key, subject );
		}

		URI subject = dataNodes.get( key );
		myrc.add( subject, RDF.TYPE, typeuri );

		boolean savelabel = metas.isAutocreateMetamodel();
		if ( !metas.isLegacyMode() && rawlabel.contains( ":" ) ) {
			// we have something with a colon in it, so we need to figure out if it's
			// a namespace-prefixed string, or just a string with a colon in it

			Value val = getRDFStringValue( rawlabel, namespaces );
			// check if we have a prefixed URI
			URI u = getUriFromRawString( rawlabel, namespaces );
			savelabel = ( savelabel && null == u );
			rawlabel = val.stringValue();
		}

		// if we have a label property, skip this label-making
		// (it'll get handled in the addProperties function later)
		if ( savelabel && !nap.hasProperty( RDFS.LABEL, namespaces ) ) {
			myrc.add( subject, RDFS.LABEL, vf.createLiteral( rawlabel ) );
		}

		addProperties( subject, nap, namespaces, sheet, metas );

		return subject;
	}

	/**
	 * Create statements for all of the properties of the instanceURI
	 *
	 * @param subject URI containing the subject instance URI
	 * @param properties Map<String, Object> that contains all properties
	 * @param namespaces
	 * @param sheet
	 * @param metas
	 *
	 * @throws RepositoryException
	 */
	protected void addProperties( URI subject, Map<String, Value> properties,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas ) throws RepositoryException {

		for ( Map.Entry<String, Value> entry : properties.entrySet() ) {
			String relkey = sheet.getName() + entry.getKey();

			URI predicate = relationClassCache.get( relkey );

			Value value = entry.getValue();
			// not sure if we even use these values anymore
			switch ( value.toString() ) {
				case Constants.PROCESS_CURRENT_DATE:
					myrc.add( subject, predicate,
							vf.createLiteral( getCal( new Date() ) ) );
					break;
				case Constants.PROCESS_CURRENT_USER:
					myrc.add( subject, predicate,
							vf.createLiteral( System.getProperty( "user.name" ) ) );
					break;
				default:
					myrc.add( subject, predicate, value );
			}
		}
	}

	private void createMetamodel( LoadingSheetData sheet, Map<String, String> namespaces,
			ImportMetadata metas ) throws RepositoryException {
		UriBuilder schema = metas.getSchemaBuilder();
		boolean save = metas.isAutocreateMetamodel();

		String stype = sheet.getSubjectType();
		String scachekey = stype;
		URI suri;
		if ( !schemaNodes.containsKey( scachekey ) ) {
			boolean nodeAlreadyMade = isUri( stype, namespaces );

			URI uri = ( nodeAlreadyMade
					? getUriFromRawString( stype, namespaces )
					: schema.build( stype ) );
			schemaNodes.put( scachekey, uri );

			if ( save && !nodeAlreadyMade ) {
				myrc.add( uri, RDF.TYPE, OWL.CLASS );
				myrc.add( uri, RDFS.LABEL, vf.createLiteral( stype ) );
				myrc.add( uri, RDFS.SUBCLASSOF, schema.getConceptUri().build() );
			}
		}
		suri = schemaNodes.get( stype );

		if ( sheet.isRel() ) {
			String otype = sheet.getObjectType();
			String ocachekey = otype;
			if ( !schemaNodes.containsKey( ocachekey ) ) {
				boolean nodeAlreadyMade = isUri( otype, namespaces );

				URI uri = ( nodeAlreadyMade
						? getUriFromRawString( otype, namespaces )
						: schema.build( otype ) );

				schemaNodes.put( ocachekey, uri );

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
			// property names are unique per sheet
			String relkey = sheet.getName() + propname;
			boolean alreadyMadeProp = isUri( propname, namespaces );

			if ( !relationClassCache.containsKey( relkey ) ) {
				URI predicate;
				if ( alreadyMadeProp ) {
					predicate = getUriFromRawString( propname, namespaces );
				}
				else {
					// UriBuilder bb = schema.getRelationUri().add( Constants.CONTAINS );
					predicate = schema.build( propname );
				}
				relationClassCache.put( relkey, predicate );
			}
			URI predicate = relationClassCache.get( relkey );

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

	private static boolean isUri( String raw, Map<String, String> namespaces ) {
		Matcher m = URISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return true;
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = namespaces.get( pieces[0] );
				if ( !( null == namespace || namespace.trim().isEmpty() ) ) {
					return true;
				}
			}
		}
		return false;
	}

	protected URI getUriFromRawString( String raw, Map<String, String> namespaces ) {
		//resolve namespace
		URI uri = null;

		// if raw starts with <something>://, then assume it's just a URI
		Matcher m = URISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return vf.createURI( raw );
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = namespaces.get( pieces[0] );
				if ( null == namespace || namespace.trim().isEmpty() ) {
					log.warn( "No namespace found for raw value: " + raw );
				}
				else {
					uri = vf.createURI( namespace, pieces[1] );
				}
			}
			else {
				log.error( "cannot resolve namespace for: " + raw + " (too many colons)" );
			}
		}
		else {
			uri = vf.createURI( raw );
		}

		return uri;
	}

	protected Value getRDFStringValue( String rawval, Map<String, String> namespaces ) {
		// if rawval looks like a URI, assume it is
		Matcher urimatcher = URISTARTPATTERN.matcher( rawval );
		if ( urimatcher.matches() ) {
			return vf.createURI( rawval );
		}

		Matcher m = NAMEPATTERN.matcher( rawval );
		String val;
		String lang;
		if ( m.matches() ) {
			String g1 = m.group( 1 );
			String g2 = m.group( 2 );
			val = ( null == g1 ? g2 : g1 );
			lang = m.group( 3 );
		}
		else {
			val = rawval;
			lang = "";

			m = DTPATTERN.matcher( rawval );
			if ( m.matches() ) {
				val = m.group( 1 );
				String typestr = m.group( 2 );
				try {
					URI type = getUriFromRawString( typestr, namespaces );
					return vf.createLiteral( val, type );
				}
				catch ( Exception e ) {
					log.warn( "probably misinterpreting as string(unknown type URI?) :"
							+ rawval, e );
				}
			}
		}

		return ( lang.isEmpty() ? vf.createLiteral( val )
				: vf.createLiteral( val, lang ) );
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
	 * @param vf
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
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( RDF.PREFIX, RDF.NAMESPACE );
		namespaces.put( RDFS.PREFIX, RDFS.NAMESPACE );
		namespaces.put( OWL.PREFIX, OWL.NAMESPACE );
		namespaces.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		namespaces.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		namespaces.put( FOAF.PREFIX, FOAF.NAMESPACE );

		conn.begin();
		for ( Map.Entry<String, String> e : namespaces.entrySet() ) {
			conn.setNamespace( e.getKey(), e.getValue() );
		}
		conn.commit();
	}

	public static void initNamespaces( ImportData conn ) {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( RDF.PREFIX, RDF.NAMESPACE );
		namespaces.put( RDFS.PREFIX, RDFS.NAMESPACE );
		namespaces.put( OWL.PREFIX, OWL.NAMESPACE );
		namespaces.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		namespaces.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		namespaces.put( FOAF.PREFIX, FOAF.NAMESPACE );
		conn.getMetadata().setNamespaces( namespaces );
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
