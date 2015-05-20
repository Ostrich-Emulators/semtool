package gov.va.semoss.om;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructStatement;
import gov.va.semoss.ui.components.RDFEngineHelper;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import gov.va.semoss.rdf.engine.impl.InMemoryJenaEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaUpdateWrapper;
import gov.va.semoss.rdf.query.util.impl.ModelQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
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

	private final Set<String> baseFilterSet = new HashSet<>();
	protected Map<Resource, String> labelcache = new HashMap<>();

	private com.hp.hpl.jena.rdf.model.Model curModel;
	private List<com.hp.hpl.jena.rdf.model.Model> modelStore = new ArrayList<>();
	private int modelCounter = 0;

	private boolean search, prop, sudowl;

	protected Map<URI, SEMOSSVertex> vertStore = new HashMap<>();
	protected Map<URI, SEMOSSEdge> edgeStore = new HashMap<>();

	private boolean filterOutOwlData = true;
	private URI typeOrSubclass = RDF.TYPE;
	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> vizgraph = new DirectedSparseGraph<>();

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
			for ( Map.Entry<String, String> ns : Utility.DEFAULTNAMESPACES.entrySet() ) {
				rc.setNamespace( ns.getKey(), ns.getValue() );
			}
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

	public DirectedGraph<SEMOSSVertex, SEMOSSEdge> getGraph() {
		return vizgraph;
	}

	public void setGraph( DirectedGraph<SEMOSSVertex, SEMOSSEdge> f ) {
		vizgraph = f;
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
		log.warn( "this function has been refactored away" );
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

		try {
			rc.begin();

			ModelQueryAdapter mqa = new ModelQueryAdapter( query );
			mqa.useInferred( false );
			Model model = engine.construct( mqa );
			Set<Resource> needProps = new HashSet<>( model.subjects() );

			for ( Statement s : model ) {
				Resource sub = s.getSubject();
				URI pred = s.getPredicate();
				Value obj = s.getObject();

				if ( obj instanceof Resource ) {
					needProps.add( Resource.class.cast( obj ) );
				}

				SEMOSSVertex vert1 = createOrRetrieveVertex( URI.class.cast( sub ) );
				SEMOSSVertex vert2;
				if ( obj instanceof URI ) {
					vert2 = createOrRetrieveVertex( URI.class.cast( obj ) );
				}
				else {
					URI uri = UriBuilder.getBuilder( Constants.ANYNODE ).uniqueUri();
					vert2 = createOrRetrieveVertex( uri );
					vert2.setLabel( obj.stringValue() );
				}

				vizgraph.addVertex( vert1 );
				vizgraph.addVertex( vert2 );

				SEMOSSEdge edge = new SEMOSSEdge( vert1, vert2, pred );
				edge.setEdgeType( pred );
				storeEdge( edge );

				try {
					vizgraph.addEdge( edge, vert1, vert2, EdgeType.DIRECTED );
				}
				catch ( Exception t ) {
					log.error( t, t );
				}
			}

			Map<URI, String> edgelabels
					= Utility.getInstanceLabels( model.predicates(), engine );
			for ( URI u : model.predicates() ) {
				SEMOSSEdge edge = edgeStore.get( u );
				edge.setProperty( RDFS.LABEL, edgelabels.get( u ) );
			}

			fetchProperties( needProps, model.predicates(), engine );

			rc.commit();
			print( "graph" );
			modelCounter++;
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
			try {
				rc.rollback();
			}
			catch ( Exception ex ) {
				log.warn( "cannot rollback transaction", ex );
			}
		}
	}

	private void print( String fname ) {
		try ( FileWriter fw = new FileWriter( new File( FileUtils.getTempDirectory(),
				fname + ".ttl" ) ) ) {
			TurtleWriter tw = new TurtleWriter( fw );
			List<Statement> stmts = Iterations.asList( rc.getStatements( null, null, null, true ) );
			Collections.sort( stmts, new Comparator<Statement>() {

				@Override
				public int compare( Statement o1, Statement o2 ) {
					int diff = o1.getSubject().stringValue().compareTo( o2.getSubject().stringValue() );
					if ( 0 == diff ) {
						diff = o1.getPredicate().stringValue().compareTo( o2.getPredicate().stringValue() );

						if ( 0 == diff ) {
							diff = o1.getObject().stringValue().compareTo( o2.getObject().stringValue() );
						}
					}

					return diff;
				}
			} );

			tw.startRDF();
			RepositoryResult<Namespace> en = rc.getNamespaces();
			while ( en.hasNext() ) {
				Namespace ns = en.next();
				tw.handleNamespace( ns.getPrefix(), ns.getName() );
			}
			en.close();

			for ( Statement s : stmts ) {
				tw.handleStatement( s );
			}
			tw.endRDF();
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
	private void loadOwlData( Collection<Resource> subjects, IEngine engine ) throws RepositoryException {
		if ( loadedOWLS.contains( engine ) ) {
			return;
		}

		loadedOWLS.add( engine );

		for ( Statement statement : engine.getOwlData() ) {
			if ( subjects.contains( statement.getSubject() ) ) {
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

			baseFilterSet.add( s );
			baseFilterSet.add( p );
			baseFilterSet.add( o );
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
			object = new ValueFactoryImpl().createLiteral( st.getObject().toString() );
		}

		try {
			if ( !rc.hasStatement( subject, predicate, object, true ) ) {
				if ( CREATION_METHOD.OVERLAY == method ) {
					curRC.add( subject, predicate, object );
				}

				rc.add( subject, predicate, object );
			}
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
			URI uri = v.getURI();
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

	public SEMOSSVertex createOrRetrieveVertex( URI vertexKey ) {
		if ( !vertStore.containsKey( vertexKey ) ) {
			SEMOSSVertex vertex = new SEMOSSVertex( vertexKey );
			storeVertex( vertex );
		}

		return vertStore.get( vertexKey );
	}

	public void storeVertex( SEMOSSVertex vert ) {
		URI key = vert.getURI();
		setLabel( vert );
		vertStore.put( key, vert );

		if ( method == CREATION_METHOD.OVERLAY && incrementalVertStore != null ) {
			incrementalVertStore.put( key.stringValue(), vert );
		}

		else if ( method == CREATION_METHOD.UNDO && incrementalVertStore != null ) {
			incrementalVertStore.remove( key.stringValue() );
		}
	}

	public void storeEdge( SEMOSSEdge edge ) {
		URI key = edge.getURI();
		edgeStore.put( key, edge );

		if ( method == CREATION_METHOD.OVERLAY && incrementalEdgeStore != null ) {
			incrementalEdgeStore.put( key.stringValue(), edge );
		}

		if ( method == CREATION_METHOD.UNDO && incrementalEdgeStore != null ) {
			incrementalEdgeStore.remove( key.stringValue() );
		}
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
		for ( Map.Entry<URI, SEMOSSVertex> en : vertStore.entrySet() ) {
			incrementalVertStore.put( en.getKey().stringValue(), en.getValue() );
		}

		incrementalEdgeStore.clear();
		for ( Map.Entry<URI, SEMOSSEdge> en : edgeStore.entrySet() ) {
			incrementalEdgeStore.put( en.getKey().stringValue(), en.getValue() );
		}
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

	public Map<URI, SEMOSSVertex> getVertStore() {
		return this.vertStore;
	}

	public Map<URI, SEMOSSEdge> getEdgeStore() {
		return this.edgeStore;
	}

	public Map<String, SEMOSSVertex> getIncrementalVertStore() {
		return this.incrementalVertStore;
	}

	public Map<String, SEMOSSEdge> getIncrementalEdgeStore() {
		return this.incrementalEdgeStore;
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

	public Set<String> getBaseFilterSet() {
		return baseFilterSet;
	}

	public void setFilterOutOwlData( boolean _filterOutOwlData ) {
		filterOutOwlData = _filterOutOwlData;
	}

	public void setTypeOrSubclass( URI _typeOrSubclass ) {
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

	private void fetchProperties( Collection<Resource> concepts, Collection<URI> preds,
			IEngine engine ) throws RepositoryException, QueryEvaluationException {

		String conceptprops
				= "SELECT ?s ?p ?o ?type WHERE {"
				+ " ?s ?p ?o . "
				+ " ?s a ?type ."
				+ " FILTER ( isLiteral( ?o ) ) }"
				+ "VALUES ?s { " + Utility.implode( concepts, "<", ">", " " ) + " }";
		String edgeprops
				= "SELECT ?s ?rel ?o ?prop ?literal ?superrel"
				+ "WHERE {"
				+ "  ?rel ?prop ?literal ."
				+ "  ?rel a ?semossrel ."
				+ "  ?rel rdf:predicate ?superrel ."
				+ "  ?s ?rel ?o ."
				+ "  FILTER ( isLiteral( ?literal ) )"
				+ "}"
				+ "VALUES ?superrel { " + Utility.implode( preds, "<", ">", " " ) + " }";
		try {
			VoidQueryAdapter cqa = new VoidQueryAdapter( conceptprops ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI prop = URI.class.cast( set.getValue( "p" ) );
					String val = set.getValue( "o" ).stringValue();
					URI type = URI.class.cast( set.getValue( "type" ) );

					SEMOSSVertex v = createOrRetrieveVertex( s );
					v.setProperty( prop, val );
					v.setType( type );
				}
			};
			cqa.useInferred( false );
			engine.query( cqa );

			// do the same thing, but for edges
			VoidQueryAdapter eqa = new VoidQueryAdapter( edgeprops ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					// ?s ?rel ?o ?prop ?literal
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI rel = URI.class.cast( set.getValue( "rel" ) );
					URI prop = URI.class.cast( set.getValue( "prop" ) );
					URI o = URI.class.cast( set.getValue( "o" ) );
					String propval = set.getValue( "literal" ).stringValue();
					URI superrel = URI.class.cast( set.getValue( "superrel" ) );

					if ( !edgeStore.containsKey( rel ) ) {
						SEMOSSVertex v1 = createOrRetrieveVertex( s );
						SEMOSSVertex v2 = createOrRetrieveVertex( o );
						SEMOSSEdge edge = new SEMOSSEdge( v1, v2, rel );
						storeEdge( edge );
					}

					SEMOSSEdge edge = edgeStore.get( rel );
					edge.setProperty( prop, propval );
					edge.setType( superrel );
				}
			};
			eqa.useInferred( false );
			engine.query( eqa );
		}
		catch ( MalformedQueryException ex ) {
			log.error( "BUG!", ex );
		}
	}
}
