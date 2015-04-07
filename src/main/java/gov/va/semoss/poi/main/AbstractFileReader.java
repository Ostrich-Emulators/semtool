package gov.va.semoss.poi.main;

import com.bigdata.rdf.model.BigdataValueFactory;
import com.bigdata.rdf.model.BigdataValueFactoryImpl;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import info.aduna.iteration.Iterations;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.util.Constants;

import java.io.File;

import java.io.FileReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import static gov.va.semoss.rdf.query.util.QueryExecutorAdapter.getCal;
import gov.va.semoss.util.UriBuilder;
import java.util.Objects;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public abstract class AbstractFileReader {

	protected static final Pattern NAMEPATTERN
			= Pattern.compile( "(?:(?:\"([^\"]+)\")|([^@]+))@([a-z-A-Z]{1,8})" );
	protected static final Pattern DTPATTERN
			= Pattern.compile( "\"([^\\\\^]+)\"\\^\\^(.*)" );
	protected static final Pattern URISTARTPATTERN
			= Pattern.compile( "(^[A-Za-z_-]+://).*" );
	private static final Logger logger = Logger.getLogger( AbstractFileReader.class );

	public static enum CacheType {

		CONCEPTCLASS, CONCEPT, RELATIONCLASS, RELATION
	};

	private URI basePropURI;
	private UriBuilder schemaBuilder;
	private UriBuilder dataBuilder;
	private boolean legacyMode = true;

	protected Map<String, URI> conceptClassCache = new HashMap<>();
	protected Map<ConceptInstanceCacheKey, URI> conceptCache = new HashMap<>();
	protected Map<String, URI> relationClassCache = new HashMap<>();
	protected Map<String, URI> relationCache = new HashMap<>();

	protected Properties rdfMap = new Properties();
	private final Map<String, URI[]> baseRelations = new LinkedHashMap<>();
	private final Map<String, URI> nodelkp = new HashMap<>();
	private RepositoryConnection failRc;

	private boolean autocreateModel = true;

	public AbstractFileReader() {
		this( UriBuilder.class.cast( null ), null );
	}

	/**
	 * Sets the custom "base" URI for new entities, and the start of a URI to be
	 * used for creating OWL statements.
	 *
	 * @param data the "base" uri for new entities
	 * @param schema a class that can generate metamodel URIs
	 *
	 */
	public AbstractFileReader( UriBuilder data, UriBuilder schema ) {
		setSchemaBuilder( schema );
		setDataBuilder( data );
	}

	/**
	 * Gets a reference to the node name cache
	 *
	 * @return a map of string=&lt;URIs
	 */
	public Map<String, URI> getNodeLookup() {
		return nodelkp;
	}

	public void checkConformance( RepositoryConnection rc ) {
		failRc = rc;
	}

	public boolean checksConformance() {
		return ( null != failRc );
	}

	/**
	 * Adds a string-to-URI map to this instance's node lookup
	 *
	 * @param map the values to add
	 */
	public void addNodeLookup( Map<String, URI> map ) {
		nodelkp.putAll( map );
	}

	public void setAutocreateMetamodel( boolean b ) {
		autocreateModel = b;
	}

	public boolean isAutocreateMetamodel() {
		return autocreateModel;
	}

	public void setLegacyMode( boolean b ) {
		legacyMode = b;
	}

	public boolean isLegacyMode() {
		return legacyMode;
	}

	public URI getBasePropUri() {
		return basePropURI;
	}

	public UriBuilder getSchemaBuilder() {
		return schemaBuilder;
	}

	public final void setSchemaBuilder( UriBuilder bldr ) {
		schemaBuilder = bldr;
		if ( null != bldr ) {
			basePropURI = bldr.getContainsUri();
		}
	}

	public final void setDataBuilder( UriBuilder bldr ) {
		dataBuilder = bldr;
	}

	public UriBuilder getDataBuilder() {
		return dataBuilder;
	}

	/**
	 * Method to add the common namespaces into the namespace hash of our
	 * RepositoryConnection. This function starts and commits a transaction.
	 *
	 * @param conn the place to add the namespaces
	 *
	 */
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

	public static void setNamespace( String prefix, String value, RepositoryConnection rc ) throws RepositoryException {
		if ( prefix == null || value == null || prefix.trim().isEmpty() || value.trim().isEmpty() ) {
			logger.warn( "Can not set a prefix with null or empty values." );
			return;
		}

		prefix = prefix.replaceAll( ":", "" ).trim();

		logger.debug( "setting namespace prefix '" + prefix + "' to value '"
				+ value.trim() + "'" );

		rc.setNamespace( prefix, value.trim() );
	}

	public void cacheUris( CacheType type, Map<String, URI> newtocache ) {
		// we need to rebox all these URIs to avoid a bigdata exception (No Such Term)
		switch ( type ) {
			case CONCEPTCLASS:
				for ( Map.Entry<String, URI> en : newtocache.entrySet() ) {
					conceptClassCache.put( en.getKey(), new URIImpl( en.getValue().toString() ) );
				}
				break;
			case RELATIONCLASS:
				for ( Map.Entry<String, URI> en : newtocache.entrySet() ) {
					relationClassCache.put( en.getKey(), new URIImpl( en.getValue().toString() ) );
				}
				break;
			case RELATION:
				for ( Map.Entry<String, URI> en : newtocache.entrySet() ) {
					nodelkp.put( en.getKey(), new URIImpl( en.getValue().toString() ) );
					relationCache.put( en.getKey(), new URIImpl( en.getValue().toString() ) );
				}
				break;
		}
	}

	/**
	 * Imports the file into the given repository connection. It is not necessary
	 * for this function to call {@link RepositoryConnection#commit()}, as the
	 * caller will do so upon completion.
	 *
	 * @param f the file to load
	 * @param rcOWL the connection to load it into
	 *
	 * @throws java.io.IOException
	 * @throws RepositoryException
	 */
	protected abstract void importOneFile( File f, RepositoryConnection rcOWL )
			throws IOException, RepositoryException;

	/**
	 * Imports a single file to the given Repository. It is not necessary for this
	 * function to call {@link RepositoryConnection#commit()}, as the caller is
	 * responsible for maintaining the RepositoryConnection. It is recommended to
	 * call {@link #initNamespaces(org.openrdf.repository.RepositoryConnection)}
	 * with the <code>rc</code> before calling this function.
	 *
	 * @param f the file to load
	 * @param rc the repository to load the data into
	 *
	 * @return a collection of OWL statements (anything with a subject that starts
	 * with {@link #getOwlStarter()}) added from this file. If
   *         {@link #setAutocreateMetamodel(boolean) } is set to <code>false</code>, the
	 * returned list will always be empty
	 *
	 * @throws java.io.IOException
	 * @throws RepositoryException if anything goes wrong
	 */
	public Collection<Statement> importFile( File f, RepositoryConnection rc )
			throws IOException, RepositoryException {
		importOneFile( f, rc );
		return finishImport( rc );
	}

	protected Collection<Statement> finishImport( RepositoryConnection rc )
			throws RepositoryException {
		createBaseRelations( rc );

		final Set<Statement> toadd = new HashSet<>();
		final BigdataValueFactory fac = BigdataValueFactoryImpl.getInstance( "kb" );

		// if we don't have an OWL file, we still need to get the data into target
		if ( autocreateModel ) {
			for ( Statement stmt : Iterations.asList( rc.getStatements( null, null,
					null, false ) ) ) {
				if ( schemaBuilder.contains( stmt.getSubject() ) ) {
					toadd.add( fac.createStatement( stmt.getSubject(), stmt.getPredicate(),
							stmt.getObject() ) );
				}
			}

			for ( URI[] data : baseRelations.values() ) {
				toadd.add( fac.createStatement( data[0], data[1], data[2] ) );
			}
		}

		return toadd;
	}

	/**
	 * Loads the prop file for the CSV file
	 *
	 * @param fileName	Absolute path to the prop file specified in the last column
	 * of the CSV file
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected void setRdfMapFromFile( File fileName ) throws FileNotFoundException, IOException {
		Properties rdfPropMap = new Properties();
		rdfPropMap.load( new FileReader( fileName ) );

		for ( String name : rdfPropMap.stringPropertyNames() ) {
			rdfMap.put( name, rdfPropMap.getProperty( name ) );
		}
	}

	/**
	 * Creates all base relationships in the metamodel to add into the database
	 * and creates the OWL file
	 *
	 * @param rcOWL where to create the base relations
	 *
	 * @throws RepositoryException
	 */
	protected void createBaseRelations( RepositoryConnection rcOWL )
			throws RepositoryException {

		if ( !autocreateModel ) {
			return;
		}
		ValueFactory vf = new ValueFactoryImpl();

		// necessary triple saying Concept is a type of Class
		URI defaultnodeclass = getSchemaBuilder().getConceptUri().build();
		rcOWL.add( defaultnodeclass, RDF.TYPE, RDFS.CLASS );

		// necessary triple saying Relation is a type of Property
		URI defaultrelclass = getSchemaBuilder().getRelationUri().build();
		rcOWL.add( defaultrelclass, RDF.TYPE, RDF.PROPERTY );

		// RPB: I don't know why we add this statement
		// (we rebox this URI to try to avoid an error in BigData)
		URI baseprop = vf.createURI( basePropURI.stringValue() );
		rcOWL.add( baseprop, RDFS.SUBPROPERTYOF, baseprop );

		// make our custom instances subclass the semoss concept
		// add all of the base concepts that have been stored in the hash.
		for ( URI subject : conceptClassCache.values() ) {
			rcOWL.add( EngineLoader.cleanStatement( new StatementImpl( subject,
					RDFS.SUBCLASSOF, defaultnodeclass ), vf ) );
		}

		// add all of the base relations that have been stored in the hash.
		for ( URI subject : relationClassCache.values() ) {
			rcOWL.add( EngineLoader.cleanStatement( new StatementImpl( subject,
					RDFS.SUBPROPERTYOF, defaultrelclass ), vf ) );
		}

		for ( URI[] relArray : baseRelations.values() ) {
			rcOWL.add( EngineLoader.cleanStatement( new StatementImpl( relArray[0],
					relArray[1], relArray[2] ), vf ) );

			logger.debug( "RELATION TRIPLE:::: " + relArray[0] + " " + relArray[1]
					+ " " + relArray[2] );
		}
	}

	/**
	 * Setter to store the metamodel created by user as a Map
	 *
	 * @param newmap the new values
	 */
	public void setRdfMap( Map<String, String> newmap ) {
		rdfMap.clear();
		for ( Map.Entry<String, String> en : newmap.entrySet() ) {
			rdfMap.setProperty( en.getKey(), en.getValue() );
		}
	}

	protected static URI getUriFromRawString( String raw, ImportData id ) {
		//resolve namespace
		ValueFactory vf = new ValueFactoryImpl();
		URI uri = null;

		// if raw starts with <something>://, then assume it's just a URI
		Matcher m = URISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return vf.createURI( raw );
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = id.getMetadata().getNamespaces().get( pieces[0] );
				if ( null == namespace || namespace.trim().isEmpty() ) {
					logger.warn( "No namespace found for raw value: " + raw );
				}
				else {
					uri = vf.createURI( namespace, pieces[1] );
				}
			}
			else {
				logger.error( "cannot resolve namespace for: " + raw + " (too many colons)" );
			}
		}
		else {
			uri = vf.createURI( raw );
		}

		return uri;
	}

	/**
	 * Gets a URI that correctly parses out the namespace if it's present
	 *
	 * @param raw the text to turn into a URI
	 * @param rc a RepositoryConnection to verify the namespace, if present
	 *
	 * @return a URI, or null if a parsing problem occurs
	 *
	 * @throws org.openrdf.repository.RepositoryException
	 */
	protected static URI getUriFromRawString( String raw, RepositoryConnection rc )
			throws RepositoryException {
		//resolve namespace
		ValueFactory vf = rc.getValueFactory();
		URI uri = null;

		// if raw starts with <something>://, then assume it's just a URI
		Matcher m = URISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return vf.createURI( raw );
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = rc.getNamespace( pieces[0] );
				if ( null == namespace || namespace.trim().isEmpty() ) {
					logger.warn( "No namespace found for raw value: " + raw );
				}
				else {
					uri = vf.createURI( namespace, pieces[1] );
				}
			}
			else {
				logger.error( "cannot resolve namespace for: " + raw + " (too many colons)" );
			}
		}
		else {
			uri = vf.createURI( raw );
		}

		return uri;
	}

	protected URI resolveString( String nodeType,
			String relName, RepositoryConnection rc ) throws RepositoryException {
		URI uristr = null;
		ValueFactory vf = rc.getValueFactory();
		if ( nodeType.contains( ":" ) ) {
			return getUriFromRawString( nodeType, rc );
		}
		else if ( rc.getNamespace( "" ) != null && !rc.getNamespace( "" ).isEmpty() ) {
			//use the repo conn default namespace
			uristr = vf.createURI( rc.getNamespace( "" ),
					( null == relName ? nodeType : relName ) );
		}
		else if ( rdfMap.containsKey( nodeType ) ) {
			// check to see if user specified URI in custom map file
			uristr = vf.createURI( rdfMap.getProperty( nodeType ) );
		}

		return uristr;
	}

	protected URI getRelationInstanceURI( String rawlabel, String relName,
			RepositoryConnection rc ) throws RepositoryException {
		String key = "relinstance" + rawlabel + relName;
		if ( nodelkp.containsKey( key ) ) {
			return nodelkp.get( key );
		}

		if ( !relationCache.containsKey( rawlabel ) ) {
			URI ret = resolveString( rawlabel, relName, rc );
			if ( null == ret ) {
				// if no user specified URI, use generic customBaseURI
				ret = dataBuilder.getRelationUri( relName );
			}

			relationCache.put( rawlabel, ret );
			nodelkp.put( key, ret );
			rc.add( ret, RDF.TYPE, OWL.OBJECTPROPERTY );
		}

		return relationCache.get( rawlabel );
	}

	protected URI getRelationClassURI( String rawlabel, String relName,
			RepositoryConnection rc ) throws RepositoryException {
		String key = "relclass" + rawlabel + relName;
		if ( nodelkp.containsKey( key ) ) {
			return nodelkp.get( key );
		}

		if ( !relationClassCache.containsKey( rawlabel ) ) {
			URI uri = resolveString( rawlabel, relName, rc );
			if ( null == uri ) {
				uri = schemaBuilder.getRelationUri( relName );
			}
			relationClassCache.put( rawlabel, uri );
			nodelkp.put( key, uri );
		}

		return relationClassCache.get( rawlabel );
	}

	public void cacheConceptClasses( Map<String, URI> map ) {
		for ( Map.Entry<String, URI> en : map.entrySet() ) {
			String rawlabel = en.getKey();
			URI uri = new URIImpl( en.getValue().stringValue() );
			String key = "concept" + rawlabel;
			nodelkp.put( key, uri );
			conceptClassCache.put( rawlabel, uri );
		}
	}

	public void cacheConceptInstances( Map<ConceptInstanceCacheKey, URI> map ) {
		for ( Map.Entry<ConceptInstanceCacheKey, URI> en : map.entrySet() ) {
			String rawlabel = en.getKey().toString();
			URI uri = new URIImpl( en.getValue().stringValue() );
			conceptCache.put( en.getKey(), uri );
			nodelkp.put( rawlabel, uri );
		}
	}

	protected URI getConceptClassURI( String rawlabel, RepositoryConnection rc ) throws RepositoryException {
		String key = "concept" + rawlabel;
		if ( nodelkp.containsKey( key ) ) {
			return nodelkp.get( key );
		}

		if ( !conceptClassCache.containsKey( rawlabel ) ) {
			URI uri = resolveString( rawlabel, null, rc );
			if ( null == uri ) {
				// if no default namespace and no user specified URI, use generic SEMOSS URI
				uri = schemaBuilder.getConceptUri( rawlabel );
			}

			conceptClassCache.put( rawlabel, uri );

			if ( autocreateModel ) {
				rc.add( uri, RDFS.LABEL, rc.getValueFactory().createLiteral( rawlabel ) );
				rc.add( uri, RDF.TYPE, OWL.CLASS );
				// will return at the end of the function
			}
		}

		URI uri = conceptClassCache.get( rawlabel );

		// if we get to here, we didn't have this URI in nodelkp, but we DO have
		// it in the conceptClassCache lookup. Put it in nodelkp for next time
		nodelkp.put( key, uri );

		return uri;
	}

	protected URI getConceptInstanceURI( String rawtype, String rawlabel,
			RepositoryConnection rc ) throws RepositoryException {
		ConceptInstanceCacheKey key = new ConceptInstanceCacheKey( rawtype, rawlabel );
		String nodelkpkey = key.toString();

		if ( nodelkp.containsKey( nodelkpkey ) ) {
			return nodelkp.get( nodelkpkey );
		}

		if ( !conceptCache.containsKey( key ) ) {
			URI uri = resolveString( rawlabel, null, rc );
			if ( null == uri ) {
				// never seen this before, and can't figure it out, so make something new
				uri = dataBuilder.getConceptUri( rawlabel );
			}
			conceptCache.put( key, uri );
			nodelkp.put( nodelkpkey, uri );
		}
		return conceptCache.get( key );
	}

	public boolean containsInstance( String rawtype, String rawlabel ) {
		ConceptInstanceCacheKey key = new ConceptInstanceCacheKey( rawtype, rawlabel );
		return conceptCache.containsKey( key );
	}

	/**
	 * Create and add all triples associated with relationship tabs
	 *
	 * @param subjectNodeType String containing the subject node type
	 * @param objectNodeType String containing the object node type
	 * @param instanceSubjectName String containing the name of the subject
	 * instance
	 * @param instanceObjectName	String containing the name of the object instance
	 * @param relName String containing the name of the relationship between the
	 * subject and object
	 * @param properties all properties
	 * @param mainRc the repository connection to add data to
	 *
	 * @throws RepositoryException
	 */
	protected void createRelationship( String subjectNodeType, String objectNodeType,
			String instanceSubjectName, String instanceObjectName, String relName,
			Map<String, Value> properties, RepositoryConnection mainRc ) throws RepositoryException {

		String sbjlabel = instanceSubjectName;
		String objlabel = instanceObjectName;

		RepositoryConnection rc = null;
		if ( checksConformance() ) {
			boolean hassbj = ( containsInstance( subjectNodeType, sbjlabel ) );
			boolean hasobj = ( containsInstance( objectNodeType, objlabel ) );
			rc = ( hassbj && hasobj ? mainRc : failRc );
		}
		else {
			rc = mainRc;
		}

		ValueFactory vf = rc.getValueFactory();
		// create the subject and object nodes, in case they aren't yet created
		URI subject = addNodeWithProperties( subjectNodeType, instanceSubjectName,
				new HashMap<>(), rc );
		URI object = addNodeWithProperties( objectNodeType, instanceObjectName,
				new HashMap<>(), rc );

		// generate URIs for the relationship
		String rellabel = relName;
		// String label = subjectNodeType + " " + rellabel + " " + objectNodeType;
		String longNodeType = subjectNodeType + "_" + relName + "_" + objectNodeType;
		URI relClassBaseURI = getRelationClassURI( longNodeType, relName, rc );
		if ( autocreateModel ) {
			rc.add( relClassBaseURI, RDF.TYPE, OWL.OBJECTPROPERTY );
			rc.add( relClassBaseURI, RDFS.LABEL, vf.createLiteral( rellabel ) );
		}
		URI relInstanceBaseURI = getRelationInstanceURI( longNodeType, relName, rc );

		if ( autocreateModel ) {
			URI subjectType = getConceptClassURI( subjectNodeType, rc );
			URI objectType = getConceptClassURI( objectNodeType, rc );

			String relArrayKey = subjectType.stringValue()
					+ relClassBaseURI + objectType;
			if ( !baseRelations.containsKey( relArrayKey ) ) {
				baseRelations.put( relArrayKey, new URI[]{ subjectType,
					relClassBaseURI, objectType } );
			}
		}

		// create instance value of relationship and add instance relationship, subproperty, and label triples
		String lkey = instanceSubjectName + Constants.RELATION_LABEL_CONCATENATOR
				+ instanceObjectName;
		if ( !nodelkp.containsKey( lkey ) ) {
			String ukey = instanceSubjectName + Constants.RELATION_URI_CONCATENATOR
					+ instanceObjectName;
			UriBuilder urib = UriBuilder.getBuilder( relInstanceBaseURI );
			URI relsbj = urib.add( ukey ).build();
			nodelkp.put( lkey, relsbj );
		}
		URI relsbj = nodelkp.get( lkey );
		logger.debug( "Adding Relationship " + subjectNodeType + " " + instanceSubjectName
				+ " ... " + relName + " ... " + objectNodeType + " " + instanceObjectName );
		rc.add( relsbj, RDFS.SUBPROPERTYOF, relClassBaseURI );
		rc.add( relsbj, RDFS.LABEL,
				vf.createLiteral( sbjlabel + Constants.RELATION_LABEL_CONCATENATOR + objlabel ) );
		rc.add( subject, relsbj, object );

		addProperties( relsbj, properties, rc, true );
	}

	protected URI addNodeWithProperties( String typelabel, String rawlabel,
			Map<String, Value> properties, RepositoryConnection rc ) throws RepositoryException {
		ValueFactory vf = rc.getValueFactory();

		ConceptInstanceCacheKey key = new ConceptInstanceCacheKey( typelabel, rawlabel );
		String nodelkpkey = key.toString();

		if ( !nodelkp.containsKey( nodelkpkey ) ) {
			URI typeuri = getConceptInstanceURI( "", typelabel, rc );
			UriBuilder urib = UriBuilder.getBuilder( typeuri );

			if ( !conceptCache.containsKey( key ) ) {
				URI subject = urib.add( rawlabel ).build();
				conceptCache.put( key, subject );
			}
			nodelkp.put( nodelkpkey, conceptCache.get( key ) );
		}

		URI subject = nodelkp.get( nodelkpkey );
		rc.add( subject, RDF.TYPE, getConceptClassURI( typelabel, rc ) );

		boolean savelabel = true;
		if ( !isLegacyMode() && rawlabel.contains( ":" ) ) {
			// we have something with a colon in it, so we need to figure out if it's
			// a namespace-prefixed string, or just a string with a colon in it

			Value val = getRDFStringValue( rawlabel, rc );
			// check if we have a prefixed URI
			URI u = getUriFromRawString( rawlabel, rc );
			savelabel = ( null == u );
			rawlabel = val.stringValue();
		}

		if ( savelabel ) {
			rc.add( subject, RDFS.LABEL, vf.createLiteral( rawlabel ) );
		}

		addProperties( subject, properties, rc, false );

		return subject;
	}

	/**
	 * Create statements for all of the properties of the instanceURI
	 *
	 * @param subject URI containing the subject instance URI
	 * @param properties Map<String, Object> that contains all properties
	 * @param rc
	 * @param subjectIsRelation is the subject a relationship, or an entity?
	 *
	 * @throws RepositoryException
	 */
	protected void addProperties( URI subject, Map<String, Value> properties,
			RepositoryConnection rc, boolean subjectIsRelation ) throws RepositoryException {

		if ( null == properties ) {
			return;
		}

		ValueFactory vf = rc.getValueFactory();

		for ( Map.Entry<String, Value> entry : properties.entrySet() ) {
			String relkey = Constants.CONTAINS + "/" + entry.getKey();

			if ( !relationClassCache.containsKey( relkey ) ) {
				UriBuilder bb = schemaBuilder.getRelationUri().add( Constants.CONTAINS );
				URI predicate = bb.add( entry.getKey() ).build();
				relationClassCache.put( relkey, predicate );
			}
			URI predicate = relationClassCache.get( relkey );

			if ( autocreateModel ) {
				rc.add( predicate, RDFS.LABEL, vf.createLiteral( entry.getKey() ) );
				rc.add( predicate, RDF.TYPE, schemaBuilder.getContainsUri() );

				if ( !legacyMode ) {
					rc.add( predicate, RDFS.SUBPROPERTYOF, basePropURI );
				}
			}

			Value value = entry.getValue();
			// not sure if we even use these values anymore
			switch ( value.toString() ) {
				case Constants.PROCESS_CURRENT_DATE:
					rc.add( subject, predicate, vf.createLiteral( getCal( new Date() ) ) );
					break;
				case Constants.PROCESS_CURRENT_USER:
					rc.add( subject, predicate, vf.createLiteral( System.getProperty( "user.name" ) ) );
					break;
				default:
					rc.add( subject, predicate, value );
			}
		}
	}

	protected static Value getRDFStringValue( String rawval, RepositoryConnection rc ) {
		ValueFactory vf = rc.getValueFactory();
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
					URI type = getUriFromRawString( typestr, rc );
					return vf.createLiteral( val, type );
				}
				catch ( Exception e ) {
					logger.warn( "probably misinterpreting as string(unknown type URI?) :"
							+ rawval, e );
				}
			}
		}

		return ( lang.isEmpty() ? vf.createLiteral( val )
				: vf.createLiteral( val, lang ) );
	}

	protected static Value getRDFStringValue( String rawval, ImportData id, ValueFactory vf ) {
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
					URI type = getUriFromRawString( typestr, id );
					return vf.createLiteral( val, type );
				}
				catch ( Exception e ) {
					logger.warn( "probably misinterpreting as string(unknown type URI?) :"
							+ rawval, e );
				}
			}
		}

		return ( lang.isEmpty() ? vf.createLiteral( val )
				: vf.createLiteral( val, lang ) );
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
			return "instance " + typelabel + rawlabel;
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
			if ( !Objects.equals( this.rawlabel, other.rawlabel ) ) {
				return false;
			}
			return true;
		}

	}
}
