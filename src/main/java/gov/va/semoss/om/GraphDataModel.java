package gov.va.semoss.om;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.InMemoryJenaEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaUpdateWrapper;
import gov.va.semoss.ui.components.RDFEngineHelper;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.turtle.TurtleWriter;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class GraphDataModel {

	private static final Logger log = Logger.getLogger( GraphDataModel.class );

	private enum CREATION_METHOD {

		CREATE_NEW, OVERLAY, UNDO
	};
	private CREATION_METHOD method;

	private final Set<IEngine> loadedOWLS = new HashSet<>();

	private RepositoryConnection rc, curRC;
	private List<RepositoryConnection> rcStore = new ArrayList<>();

	private final Map<String, String> baseFilterHash = new HashMap<>();
	protected Map<URI, String> labelcache = new HashMap<>();

	private Model jenaModel, curModel;
	private List<Model> modelStore = new ArrayList<>();
	private int modelCounter = 0;

	private boolean search, prop, sudowl;

	protected Map<String, SEMOSSVertex> vertStore = new HashMap<>();
	protected Map<String, SEMOSSEdge> edgeStore = new HashMap<>();

	private boolean filterOutOwlData = true;
	private String typeOrSubclass = " a ";
	private String containsRelation;

	private static final URI containsURI = DIHelper.getContainsURI();
	private static final URI conceptURI = DIHelper.getConceptURI();
	private static final URI relationURI = DIHelper.getRelationURI();

	//these are used for keeping track of only what was added or subtracted and will only be populated when overlay is true
	private Map<String, SEMOSSVertex> incrementalVertStore;
	private Map<String, SEMOSSEdge> incrementalEdgeStore;

	public GraphDataModel() {
		try {
			ForwardChainingRDFSInferencer fcri
					= new ForwardChainingRDFSInferencer( new MemoryStore() );
			Repository myRepository = new SailRepository( fcri );
			myRepository.initialize();

			rc = myRepository.getConnection();
		}
		catch ( RepositoryException re ) {
			log.error( re, re );
		}

		initPropSudowlSearch();
	}

	public boolean enableSearchBar() {
		return search;
	}

	public boolean showSudowl() {
		return sudowl;
	}

	public void overlayData( String query, IEngine engine ) {
		log.debug( "Creating the new model" );

		curModel = null;
		curRC = null;

		incrementalVertStore = new HashMap<>();
		incrementalEdgeStore = new HashMap<>();

		try {
			Repository newRepo = new SailRepository(
					new ForwardChainingRDFSInferencer( new MemoryStore() )
			);
			newRepo.initialize();

			curRC = newRepo.getConnection();
			curModel = ModelFactory.createDefaultModel();
		}
		catch ( RepositoryException e ) {
			log.error( e );
		}

		processData( query, engine );
		processTraverseCourse();
	}

	public void createModel( String query, IEngine engine ) {
		if ( method == CREATION_METHOD.OVERLAY ) {
			overlayData( query, engine );
		}
		else {
			processData( query, engine );
		}
	}

	/**
	 * Method fillStoresFromModel. This function requires the rc to be completely
	 * full it will use the rc to create edge and node properties and then nodes
	 * and edges.
	 */
	public void fillStoresFromModel() {
		log.debug( "Creating the base Graph in fillStoresFromModel." );
		if ( rc == null ) {
			log.warn( "rc is null, could not create the base graph." );
			return;
		}

		RDFEngineHelper.genNodePropertiesLocal( rc, getContainsRelation(), this );
		RDFEngineHelper.genEdgePropertiesLocal( rc, getContainsRelation(), this );

		generateVerticesFromConceptsInRC();
		generateEdgesFromTriplesInRC();
	}

	/*
	 * Method processData @param query @param engine
	 *
	 * Need to take the base information from the base query and insert it into
	 * the jena model this is based on EXTERNAL ontology then take the ontology
	 * and insert it into the jena model (may be eventually we can run this
	 * through a reasoner too)
	 *
	 * Now insert our base model into the same ontology. Now query the model for
	 * Relations - Paint the basic graph. Now find a way to get all the predicate
	 * properties from them. Hopefully the property is done using subproperty of
	 * predicates - Pick all the predicates but for the properties.
	 *
	 */
	public void processData( String query, IEngine engine ) {
		Set<URI> urisNeedingLabels = new HashSet<>();
		String subjects = "";
		String predicates = "";
		String objects = "";

		try {
			rc.begin();

			Collection<SesameJenaConstructStatement> sjw
					= RDFEngineHelper.runSesameConstructOrSelectQuery( engine, query );
			for ( SesameJenaConstructStatement statement : sjw ) {
				String thisSubject = "(<" + statement.getSubject() + ">)";
				String thisPredicate = "(<" + statement.getPredicate() + ">)";
				String thisObject = "(<" + statement.getObject() + ">)";

				urisNeedingLabels.add( new URIImpl( statement.getSubject() ) );
				urisNeedingLabels.add( new URIImpl( statement.getPredicate() ) );
				if ( statement.getObject() instanceof URI ) {
					urisNeedingLabels.add( URI.class.cast( statement.getObject() ) );
				}

				if ( !subjects.contains( thisSubject ) ) {
					subjects += thisSubject;
				}
				if ( !predicates.contains( thisPredicate ) ) {
					predicates += thisPredicate;
				}
				if ( !objects.contains( thisObject ) ) {
					objects += thisObject;
				}

				addToSesame( statement );

				if ( search ) {
					addToJenaModel3( statement );
				}
			}
			log.debug( "\nSubs >> " + subjects + "\nPrds >> " + predicates + "\nObjs >> " + objects );

			loadOwlData( subjects, engine );
			labelcache.putAll( Utility.getInstanceLabels( urisNeedingLabels, engine ) );

			if ( subjects.length() > 0 || predicates.length() > 0 || objects.length() > 0 ) {
				RDFEngineHelper.loadConceptHierarchy( engine, subjects, objects, this );
				RDFEngineHelper.loadRelationHierarchy( engine, predicates, this );
			}

			if ( prop ) {
				RDFEngineHelper.loadPropertyHierarchy( engine, predicates, getContainsRelation(), this );
				RDFEngineHelper.genPropertiesRemote( engine, subjects, objects, predicates, getContainsRelation(), this );
			}

			rc.commit();

			try ( FileWriter fw = new FileWriter( new File( FileUtils.getTempDirectory(),
					"graph.ttl" ) ) ) {
				rc.export( new TurtleWriter( fw ) );
			}
			catch ( Exception e ) {
				log.error( e, e );
			}

			modelCounter++;
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	/*
	 * method loadOwlData @param engine IEngine to load the data from
	 *
	 * Adds the base relationships to the metamodel. This links the hierarchy that
	 * tool needs to the metamodel being queried.
	 */
	private void loadOwlData( String subjects, IEngine engine ) throws RepositoryException {
		if ( loadedOWLS.contains( engine ) ) {
			return;
		}

		loadedOWLS.add( engine );

		for ( Statement statement : engine.getOwlData() ) {
			if ( subjects.contains( statement.getSubject().stringValue() ) ) {
				rc.add( statement );
			}
		}

		if ( !filterOutOwlData ) {
			return;
		}

		int numStatementsAdded = 0;
		for ( Statement statement : engine.getOwlData() ) {
			String s = statement.getSubject().stringValue();
			String p = statement.getPredicate().stringValue();
			String o = statement.getObject().stringValue();

			baseFilterHash.put( s, s );
			baseFilterHash.put( p, p );
			baseFilterHash.put( o, o );
			numStatementsAdded++;
		}

		log.debug( "loadOwlData(engine) added " + numStatementsAdded
				+ " statements to the baseFilterHash." );
	}

	public void addToSesame( SesameJenaConstructStatement st ) {
		Resource subject = new URIImpl( st.getSubject() );
		URI predicate = new URIImpl( st.getPredicate() );
		Value object = null;

		if ( st.getObject() instanceof Resource ) {
			object = Resource.class.cast( st.getObject() );
		}
		else if ( st.getObject() instanceof Literal ) {
			object = Literal.class.cast( st.getObject() );
		}

		if ( null == object ) {
			object = new ValueFactoryImpl().createLiteral( object.toString() );
		}

		try {
			if ( rc.hasStatement( subject, predicate, object, true ) ) {
				return;
			}

			if ( method == CREATION_METHOD.OVERLAY ) {
				curRC.add( subject, predicate, object );
			}

			rc.add( subject, predicate, object );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
	}

	protected void setLabel( SEMOSSVertex v ) {
		setLabel( v, "" );
	}

	protected void setLabel( SEMOSSVertex v, String labelPieceToAppend ) {
		try {
			URI uri = new URIImpl( v.getURI() );
			if ( labelcache.containsKey( uri ) ) {
				v.setLabel( labelcache.get( uri ) + labelPieceToAppend );
				return;
			}
		}
		catch ( Exception e ) {
			// label won't be in the cache; don't worry about it
		}
		v.setLabel( v.getLabel() + labelPieceToAppend );
	}

	/**
	 * Method addToJenaModel3.
	 *
	 * @param st SesameJenaConstructStatement
	 */
	public void addToJenaModel3( SesameJenaConstructStatement st ) {
		if ( jenaModel == null ) {
			jenaModel = ModelFactory.createDefaultModel();
		}

		com.hp.hpl.jena.rdf.model.Resource subject = jenaModel.createResource( st.getSubject() );
		com.hp.hpl.jena.rdf.model.Property props = jenaModel.createProperty( st.getPredicate() );
		com.hp.hpl.jena.rdf.model.Resource object = jenaModel.createResource( st.getObject() + "" );

		com.hp.hpl.jena.rdf.model.Statement jenaSt = jenaModel.createStatement( subject, props, object );
		if ( !jenaModel.contains( jenaSt ) ) {
			jenaModel.add( jenaSt );

			if ( method == CREATION_METHOD.OVERLAY ) {
				curModel.add( jenaSt );
			}
		}
	}

	public SEMOSSVertex createOrRetrieveVertex( String vertexKey ) {
		SEMOSSVertex vertex = vertStore.get( vertexKey );
		if ( vertex == null ) {
			vertex = new SEMOSSVertex( vertexKey );
			setLabel( vertex );
			storeVertex( vertex );
		}

		return vertex;
	}

	private SEMOSSVertex createOrRetrieveVertex( String vertexKey, Object object ) {
		SEMOSSVertex vertex = vertStore.get( vertexKey );
		if ( vertex == null ) {
			if ( object instanceof URI ) {
				vertex = new SEMOSSVertex( vertexKey );
			}
			else // ok this is a literal
			{
				vertex = new SEMOSSVertex( vertexKey, object );
			}
			// setLabel( vertex );
			storeVertex( vertex );
		}

		return vertex;
	}

	public void storeVertex( SEMOSSVertex vert ) {
		String key = vert.getProperty( Constants.URI_KEY ) + "";
		setLabel( vert );
		vertStore.put( key, vert );

		if ( method == CREATION_METHOD.OVERLAY && incrementalVertStore != null ) {
			incrementalVertStore.put( key, vert );
		}

		else if ( method == CREATION_METHOD.UNDO && incrementalVertStore != null ) {
			incrementalVertStore.remove( key );
		}
	}

	public void storeEdge( SEMOSSEdge edge ) {
		String key = edge.getProperty( Constants.URI_KEY ) + "";
		edgeStore.put( key, edge );

		if ( method == CREATION_METHOD.OVERLAY && incrementalEdgeStore != null ) {
			incrementalEdgeStore.put( key, edge );
		}

		if ( method == CREATION_METHOD.UNDO && incrementalEdgeStore != null ) {
			incrementalEdgeStore.remove( key );
		}
	}

	public void addEdgeProperty( String edgeName, Object value, String propName, String outNode, String inNode ) {
		SEMOSSEdge edge = edgeStore.get( edgeName );

		if ( edge == null ) {
			SEMOSSVertex vertex1 = createOrRetrieveVertex( outNode );
			SEMOSSVertex vertex2 = createOrRetrieveVertex( inNode );

			edge = new SEMOSSEdge( vertex1, vertex2, edgeName );
		}

		//only set property and store edge if the property does not already exist on the edge
		String propNameInstance = Utility.getInstanceName( propName );
		if ( edge.getProperty( propNameInstance ) == null ) {
			edge.setProperty( propNameInstance, value );
			storeEdge( edge );
		}
	}

	/**
	 * Method getContainsRelation
	 *
	 * @return String
	 */
	protected String getContainsRelation() {
		if ( containsRelation != null ) {
			return containsRelation;
		}

		containsRelation = " <" + containsURI.stringValue() + "> ";

		String query
				= "SELECT DISTINCT ?Subject ?subProp ?contains WHERE { "
				+ "  BIND( rdfs:subPropertyOf AS ?subProp )"
				+ "  BIND( " + containsRelation + " AS ?contains)"
				+ "  {?Subject ?subProp  ?contains}"
				+ "}";

		Collection<SesameJenaConstructStatement> sjcw
				= RDFEngineHelper.runSesameJenaSelectCheater( rc, query );
		if ( !sjcw.isEmpty() ) {
			containsRelation = " <" + sjcw.iterator().next().getSubject() + "> ";
		}

		return containsRelation;
	}

	public void undoData() {
		log.debug( "rcStore  " + rcStore.toString() );
		RepositoryConnection lastRC = rcStore.get( modelCounter - 2 );
		// remove undo model from repository connection
		try {
			log.debug( "Number of undo statements " + lastRC.size() );
			log.debug( "Number of statements in the old model " + rc.size() );
			log.debug( "rcStore size              " + rcStore.size() );
			log.debug( "modelCounter              " + modelCounter );
		}
		catch ( RepositoryException e ) {
			// TODO Auto-generated catch block
			log.error( e );
		}
		IEngine sesameEngine = new InMemorySesameEngine( lastRC );
		RDFEngineHelper.removeAllData( sesameEngine, rc );
		//jenaModel.remove(lastModel);
		modelCounter--;

		incrementalVertStore.clear();
		incrementalVertStore.putAll( vertStore );
		incrementalEdgeStore.clear();
		incrementalEdgeStore.putAll( edgeStore );
		vertStore.clear();
		edgeStore.clear();
	}

	public void redoData() {
		RepositoryConnection newRC = rcStore.get( modelCounter - 1 );
		//add redo model from repository connection

		IEngine sesameEngine = new InMemorySesameEngine( newRC );
		RDFEngineHelper.addAllData( sesameEngine, rc );
		modelCounter++;

		incrementalVertStore.clear();
		incrementalEdgeStore.clear();
	}

	/**
	 * Method removeFromJenaModel.
	 *
	 * @param st SesameJenaConstructStatement
	 */
	protected void removeFromJenaModel( SesameJenaConstructStatement st ) {
		com.hp.hpl.jena.rdf.model.Resource subject = jenaModel.createResource( st.getSubject() );
		com.hp.hpl.jena.rdf.model.Property props = jenaModel.createProperty( st.getPredicate() );
		com.hp.hpl.jena.rdf.model.Resource object = jenaModel.createResource( st.getObject() + "" );
		com.hp.hpl.jena.rdf.model.Statement jenaSt;

		log.warn( "Removing Statement " + subject + "<>" + props + "<>" + object );
		jenaSt = jenaModel.createStatement( subject, props, object );
		jenaModel.remove( jenaSt );
	}

	/**
	 * Method generateEdgesFromTriplesInRC executes the first SPARQL query and
	 * generates the graphs
	 */
	public void generateEdgesFromTriplesInRC() {
		String query
				= "SELECT DISTINCT ?Subject ?Predicate ?Object WHERE {"
				+ "  ?Predicate a <" + relationURI.stringValue() + "> ."
				+ "  ?Subject " + typeOrSubclass + " <" + conceptURI.stringValue() + "> ."
				+ "  ?Subject ?Predicate ?Object"
				+ "}";

		int numResults = 0;
		Collection<SesameJenaConstructStatement> sjcw
				= RDFEngineHelper.runSesameJenaSelectCheater( rc, query );
		for ( SesameJenaConstructStatement sct : sjcw ) {
			if ( baseFilterHash.containsKey( sct.getSubject() )
					|| baseFilterHash.containsKey( sct.getPredicate() )
					|| baseFilterHash.containsKey( sct.getObject().toString() ) ) {
				continue;
			}

			SEMOSSVertex vertex1 = createOrRetrieveVertex( sct.getSubject() );
			SEMOSSVertex vertex2 = createOrRetrieveVertex( sct.getObject() + "", sct.getObject() );

			// check to see if this is another type of edge
			String edgeString = vertex1.getProperty( Constants.VERTEX_NAME ) + ":" + vertex2.getProperty( Constants.VERTEX_NAME );
			String predicateName = sct.getPredicate();
			if ( !predicateName.contains( edgeString ) ) {
				predicateName += "/" + edgeString;
			}

			if ( edgeStore.get( sct.getPredicate() ) == null && edgeStore.get( predicateName ) == null ) {
				storeEdge( new SEMOSSEdge( vertex1, vertex2, predicateName ) );
				log.debug( sct.getPredicate() + " <<>> " + predicateName );
				numResults++;
			}
		}

		log.debug( "generateEdgesFromTriplesInRC() stored " + numResults + " edges." );
	}

	/**
	 * Method generateVerticesFromConceptsInRC - create all the relationships
	 */
	public void generateVerticesFromConceptsInRC() {
		String query
				= "SELECT DISTINCT ?Subject ?Predicate ?Object WHERE {"
				+ "  {?Subject " + typeOrSubclass + " <" + conceptURI.stringValue() + ">;}"
				+ "  BIND(\"\" AS ?Predicate)" // these are only used so that I can use select cheater...
				+ "  BIND(\"\" AS ?Object)"
				+ "}";

		int numResults = 0;
		Collection<SesameJenaConstructStatement> sjcw
				= RDFEngineHelper.runSesameJenaSelectCheater( rc, query );
		for ( SesameJenaConstructStatement s : sjcw ) {
			String subject = s.getSubject();
			if ( !baseFilterHash.containsKey( subject ) && !vertStore.containsKey( subject ) ) {
				SEMOSSVertex vertex = new SEMOSSVertex( subject );
				//setLabel( vertex );
				storeVertex( vertex );
				numResults++;
			}
		}

		log.debug( "genBaseConcepts() loaded " + numResults + " vertices." );
	}

	/**
	 * Method updateAllModels. Update all internal models associated with this
	 * playsheet with the query passed in
	 *
	 * @param query String
	 */
	public void updateAllModels( String query ) {
		log.debug( query );

		// run query on rc
		try {
			rc.commit();
		}
		catch ( Exception e ) {

		}
		InMemorySesameEngine rcSesameEngine = new InMemorySesameEngine( rc );
		SesameJenaUpdateWrapper sjuw = new SesameJenaUpdateWrapper();
		sjuw.setEngine( rcSesameEngine );
		sjuw.setQuery( query );
		sjuw.execute();
		log.info( "Ran update against rc" );

		// run query on curRc
		if ( curRC != null ) {
			InMemorySesameEngine curRcSesameEngine = new InMemorySesameEngine( curRC );
			sjuw.setEngine( curRcSesameEngine );
			sjuw.setQuery( query );
			sjuw.execute();
			log.info( "Ran update against curRC" );
		}

		// run query on jenaModel
		InMemoryJenaEngine modelJenaEngine = new InMemoryJenaEngine();
		modelJenaEngine.setModel( jenaModel );
		sjuw.setEngine( modelJenaEngine );
		sjuw.setQuery( query );
		sjuw.execute();
		log.info( "Ran update against jenaModel" );

		// run query on jenaModel
		if ( curModel != null ) {
			InMemoryJenaEngine curModelJenaEngine = new InMemoryJenaEngine();
			curModelJenaEngine.setModel( curModel );
			sjuw.setEngine( curModelJenaEngine );
			sjuw.setQuery( query );
			sjuw.execute();
			log.info( "Ran update against curModel" );
		}
	}

	public void setOverlay( boolean overlay ) {
		if ( overlay ) {
			this.method = CREATION_METHOD.OVERLAY;
		}
		else {
			this.method = CREATION_METHOD.CREATE_NEW;
		}
	}

	public void setUndo( boolean undo ) {
		if ( undo ) {
			this.method = CREATION_METHOD.UNDO;
		}
		else {
			this.method = CREATION_METHOD.CREATE_NEW;
		}
	}

	public void initPropSudowlSearch() {
		prop = Boolean.parseBoolean( DIHelper.getInstance().getProperty( Constants.GPSProp ) );
		sudowl = Boolean.parseBoolean( DIHelper.getInstance().getProperty( Constants.GPSSudowl ) );
		search = Boolean.parseBoolean( DIHelper.getInstance().getProperty( Constants.GPSSearch ) );

		log.debug( "Initializing boolean properties (prop, sudowl, search) to (" + prop + ", " + sudowl + ", " + search + ")" );

		/*
		 // these calls are not yet functional
		 prop = Preferences.userNodeForPackage(PlayPane.class).getBoolean( Constants.GPSProp, true );
		 sudowl = Preferences.userNodeForPackage(PlayPane.class).getBoolean( Constants.GPSSudowl, true );
		 search = Preferences.userNodeForPackage(PlayPane.class).getBoolean( Constants.GPSSearch, true );
    
		 log.debug( "Initializing boolean properties (prop, sudowl, search) to (" + prop + ", " + sudowl + ", " + search + ")" );
		 */
	}

	/**
	 * Method processTraverseCourse.
	 */
	public void processTraverseCourse() {
		//if you're at a spot where you have forward models, extensions will reset the future, thus we need to remove all future models
		//modelCounter already added by the time it gets here so you need to -1 to modelCounter
		if ( rcStore.size() >= modelCounter - 1 ) {
			//have to start removing from teh back of the model to avoid the rcstore from resizing
			//
			for ( int modelIdx = rcStore.size() - 1; modelIdx >= modelCounter - 2; modelIdx-- ) {
				modelStore.remove( modelIdx );
				rcStore.remove( modelIdx );
			}
		}
		modelStore.add( curModel );
		rcStore.add( curRC );
		log.debug( "Extend : Total Models added = " + modelStore.size() );
	}

	public Map<String, SEMOSSVertex> getVertStore() {
		return this.vertStore;
	}

	public Map<String, SEMOSSEdge> getEdgeStore() {
		return this.edgeStore;
	}

	public Map<String, SEMOSSVertex> getIncrementalVertStore() {
		return this.incrementalVertStore;
	}

	public Map<String, SEMOSSEdge> getIncrementalEdgeStore() {
		return this.incrementalEdgeStore;
	}

	public Model getJenaModel() {
		if ( null == jenaModel ) {
			jenaModel = ModelFactory.createDefaultModel();
			try {
				RepositoryResult<Statement> rr = rc.getStatements( null, null, null, false );
				while ( rr.hasNext() ) {
					Statement st = rr.next();
					com.hp.hpl.jena.rdf.model.Resource subject
							= jenaModel.createResource( st.getSubject().stringValue() );
					com.hp.hpl.jena.rdf.model.Property props
							= jenaModel.createProperty( st.getPredicate().stringValue() );
					com.hp.hpl.jena.rdf.model.Resource object
							= jenaModel.createResource( st.getObject().stringValue() );

					com.hp.hpl.jena.rdf.model.Statement jenaSt
							= jenaModel.createStatement( subject, props, object );
					jenaModel.add( jenaSt );
				}
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}

		return jenaModel;
	}

	public void removeView( String query, IEngine engine ) {
		Collection<SesameJenaConstructStatement> sjw
				= RDFEngineHelper.runSesameConstructOrSelectQuery( engine, query );
		for ( SesameJenaConstructStatement st : sjw ) {
			org.openrdf.model.Resource subject = new URIImpl( st.getSubject() );
			org.openrdf.model.URI predicate = new URIImpl( st.getPredicate() );
			String delQuery = "DELETE DATA {";

			// figure out if this is an object later
			Object obj = st.getObject();
			delQuery = delQuery + "<" + subject + "><" + predicate + ">";

			if ( ( obj instanceof com.hp.hpl.jena.rdf.model.Literal ) || ( obj instanceof Literal ) ) {
				delQuery = delQuery + obj + ".";
			}
			else {
				delQuery = delQuery + "<" + obj + ">";
			}

			delQuery = delQuery + "}";
			Update up;

			try {
				up = rc.prepareUpdate( QueryLanguage.SPARQL, delQuery );
				rc.begin();
				up.execute();
				rc.commit();
			}
			catch ( RepositoryException | MalformedQueryException | UpdateExecutionException e ) {
				try {
					rc.rollback();
				}
				catch ( RepositoryException ignored ) {
				}

				log.error( e );
			}
			delQuery += ".";

			log.debug( delQuery );
		}

		//need to reset this
		vertStore = new HashMap<>();
		edgeStore = new HashMap<>();
	}

	/*
	 * Method dumpRC
	 *
	 * Debugging method to dump all statements currently in the rc to the console.
	 * If there is too much data in the rc this method could cause an
	 * OutOfMemoryException.
	 */
	@SuppressWarnings( { "deprecation", "unused" } )
	private void logAllStatementsInRC() {
		try {
			String allStatements = "";
			for ( Statement statement : rc.getStatements( null, null, null, true ).asList() ) {
				allStatements += statement.toString() + "\n";
			}
			log.debug( "\n\n" + allStatements + "\n\n" );
		}
		catch ( RepositoryException e ) {
		}
	}

	public Map<String, String> getBaseFilterHash() {
		return baseFilterHash;
	}

	public void setFilterOutOwlData( boolean _filterOutOwlData ) {
		filterOutOwlData = _filterOutOwlData;
	}

	public void setTypeOrSubclass( String _typeOrSubclass ) {
		typeOrSubclass = _typeOrSubclass;
	}

	public void setRC( RepositoryConnection _rc ) {
		rc = _rc;
	}

	public RepositoryConnection getRC() {
		return rc;
	}

	public int getModelCounter() {
		return modelCounter;
	}

	public int getRCStoreSize() {
		return rcStore.size();
	}
}
