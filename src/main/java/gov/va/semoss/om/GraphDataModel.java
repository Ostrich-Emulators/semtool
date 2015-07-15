package gov.va.semoss.om;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleGraph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class GraphDataModel {

	private static final Logger log = Logger.getLogger( GraphDataModel.class );

	private final Set<String> baseFilterSet = new HashSet<>();
	private final Map<NodeEdgeBase, Integer> level = new HashMap<>();
	protected Map<Resource, String> labelcache = new HashMap<>();

	private boolean search, prop, sudowl;

	protected Map<URI, SEMOSSVertex> vertStore = new HashMap<>();
	protected Map<URI, SEMOSSEdge> edgeStore = new HashMap<>();

	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> vizgraph = new DirectedSparseGraph<>();

	public GraphDataModel() {
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

	public SimpleGraph<SEMOSSVertex, SEMOSSEdge> asSimpleGraph() {
		SimpleGraph<SEMOSSVertex, SEMOSSEdge> graph
				= new SimpleGraph<>( SEMOSSEdge.class );

		for ( SEMOSSVertex v : vizgraph.getVertices() ) {
			graph.addVertex( v );
		}
		for ( SEMOSSEdge e : vizgraph.getEdges() ) {
			graph.addEdge( vizgraph.getSource( e ), vizgraph.getDest( e ), e );
		}

		return graph;
	}

	public DelegateForest<SEMOSSVertex, SEMOSSEdge> asForest() {
		DelegateForest<SEMOSSVertex, SEMOSSEdge> forest = new DelegateForest<>( vizgraph );
		return forest;
	}

	public void setGraph( DirectedGraph<SEMOSSVertex, SEMOSSEdge> f ) {
		vizgraph = f;
	}

	/**
	 * Adds new nodes/edges to the graph at the specified "redo" level.
	 *
	 * @param model the nodes/edges to add
	 * @param engine the engine to get other data from
	 * @param overlayLevel the level of the nodes
	 */
	public void addGraphLevel( Model model, IEngine engine, int overlayLevel ) {
		try {
			Set<Resource> needProps = new HashSet<>( model.subjects() );

			for ( Statement s : model ) {
				Resource sub = s.getSubject();
				URI pred = s.getPredicate();
				Value obj = s.getObject();

				if ( obj instanceof Resource ) {
					needProps.add( Resource.class.cast( obj ) );
				}

				SEMOSSVertex vert1 = createOrRetrieveVertex( URI.class.cast( sub ), overlayLevel );
				SEMOSSVertex vert2;
				if ( obj instanceof URI ) {
					vert2 = createOrRetrieveVertex( URI.class.cast( obj ), overlayLevel );
				}
				else {
					URI uri = UriBuilder.getBuilder( Constants.ANYNODE ).uniqueUri();
					vert2 = createOrRetrieveVertex( uri, overlayLevel );
					vert2.setLabel( obj.stringValue() );
				}

				vizgraph.addVertex( vert1 );
				vizgraph.addVertex( vert2 );

				SEMOSSEdge edge = new SEMOSSEdge( vert1, vert2, pred );
				level.put( edge, overlayLevel );
				edge.setType( pred );
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

			fetchProperties( needProps, model.predicates(), engine, overlayLevel );
		}
		catch ( RepositoryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	public void addGraphLevel( Collection<Resource> nodes, IEngine engine, int overlayLevel ) {
		try {
			for ( Resource sub : nodes ) {
				SEMOSSVertex vert1 = createOrRetrieveVertex( URI.class.cast( sub ), overlayLevel );
				vizgraph.addVertex( vert1 );
			}

			fetchProperties( nodes, null, engine, overlayLevel );
		}
		catch ( RepositoryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	public void removeElementsSinceLevel( int overlayLevel ) {
		// if we've undone some data and now want to add 
		// something else, get rid of the future redo data
		List<SEMOSSVertex> nodesToRemove = new ArrayList<>();
		for ( SEMOSSVertex v : vizgraph.getVertices() ) {
			if ( getLevel( v ) > overlayLevel ) {
				nodesToRemove.add( vertStore.remove( v.getURI() ) );
			}
		}

		for ( SEMOSSVertex v : nodesToRemove ) {
			// edges will be removed automatically...but sync our level mapping
			Collection<SEMOSSEdge> edges = vizgraph.getIncidentEdges( v );
			for ( SEMOSSEdge e : edges ) {
				level.remove( e );
			}

			vizgraph.removeVertex( v );
			level.remove( v );
		}
	}

	public int getLevel( NodeEdgeBase check ) {
		if ( level.containsKey( check ) ) {
			return level.get( check );
		}
		return 0;
	}

	/**
	 * Is this node present at the given level (is it's level <= the given level?)
	 * @param check
	 * @param level
	 * @return 
	 */
	public boolean presentAtLevel( NodeEdgeBase check, int level ) {
		return getLevel( check ) <= level;
	}

	public SEMOSSVertex createOrRetrieveVertex( URI vertexKey, int overlayLevel ) {
		if ( !vertStore.containsKey( vertexKey ) ) {
			SEMOSSVertex vertex = new SEMOSSVertex( vertexKey );
			level.put( vertex, overlayLevel );
			storeVertex( vertex );
		}

		return vertStore.get( vertexKey );
	}

	public void storeVertex( SEMOSSVertex vert ) {
		URI key = vert.getURI();
		vertStore.put( key, vert );
	}

	public void storeEdge( SEMOSSEdge edge ) {
		URI key = edge.getURI();
		edgeStore.put( key, edge );
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

	public Set<String> getBaseFilterSet() {
		return baseFilterSet;
	}

	private void fetchProperties( Collection<Resource> concepts, Collection<URI> preds,
			IEngine engine, int overlayLevel ) throws RepositoryException, QueryEvaluationException {

		String conceptprops
				= "SELECT ?s ?p ?o ?type WHERE {"
				+ " ?s ?p ?o . "
				+ " ?s a ?type ."
				+ " FILTER ( isLiteral( ?o ) ) }"
				+ "VALUES ?s { " + Utility.implode( concepts, "<", ">", " " ) + " }";

		// we can't be sure if our predicates are the base relation or the 
		// specific relation, so query for both just in case
		String specificEdgeProps
				= "SELECT ?s ?rel ?o ?prop ?literal ?superrel WHERE {"
				+ "  ?rel ?prop ?literal ."
				+ "  ?rel a ?semossrel ."
				+ "  ?rel rdf:predicate ?superrel ."
				+ "  ?s ?rel ?o ."
				+ "  FILTER ( isLiteral( ?literal ) )"
				+ "}"
				+ "VALUES ?superrel { " + Utility.implode( preds, "<", ">", " " ) + " }";
		String baseEdgeProps
				= "SELECT ?s ?rel ?o ?prop ?literal ?superrel WHERE {"
				+ "  ?rel ?prop ?literal ."
				+ "  ?rel a ?semossrel ."
				+ "  ?rel rdf:predicate ?superrel ."
				+ "  ?s ?rel ?o ."
				+ "  FILTER ( isLiteral( ?literal ) )"
				+ "}"
				+ "VALUES ?rel { " + Utility.implode( preds, "<", ">", " " ) + " }";
		try {
			VoidQueryAdapter cqa = new VoidQueryAdapter( conceptprops ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI prop = URI.class.cast( set.getValue( "p" ) );
					String val = set.getValue( "o" ).stringValue();
					URI type = URI.class.cast( set.getValue( "type" ) );

					SEMOSSVertex v = createOrRetrieveVertex( s, overlayLevel );
					v.setProperty( prop, val );
					v.setType( type );
				}
			};

			if ( null != concepts ) {
				cqa.useInferred( false );
				engine.query( cqa );
			}

			// do the same thing, but for edges
			VoidQueryAdapter specifics = new VoidQueryAdapter( specificEdgeProps ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					// ?s ?rel ?o ?prop ?literal
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI rel = URI.class.cast( set.getValue( "rel" ) );
					URI prop = URI.class.cast( set.getValue( "prop" ) );
					URI o = URI.class.cast( set.getValue( "o" ) );
					String propval = set.getValue( "literal" ).stringValue();
					URI superrel = URI.class.cast( set.getValue( "superrel" ) );

					if ( concepts.contains( s ) && concepts.contains( o ) ) {
						if ( !edgeStore.containsKey( rel ) ) {
							SEMOSSVertex v1 = createOrRetrieveVertex( s, overlayLevel );
							SEMOSSVertex v2 = createOrRetrieveVertex( o, overlayLevel );
							SEMOSSEdge edge = new SEMOSSEdge( v1, v2, rel );
							storeEdge( edge );
						}

						SEMOSSEdge edge = edgeStore.get( rel );
						edge.setProperty( prop, propval );
						edge.setType( superrel );
					}
				}
			};

			VoidQueryAdapter baseedge = new VoidQueryAdapter( baseEdgeProps ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					// ?s ?rel ?o ?prop ?literal
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI rel = URI.class.cast( set.getValue( "rel" ) );
					URI prop = URI.class.cast( set.getValue( "prop" ) );
					URI o = URI.class.cast( set.getValue( "o" ) );
					String propval = set.getValue( "literal" ).stringValue();
					URI superrel = URI.class.cast( set.getValue( "superrel" ) );

					if ( concepts.contains( s ) && concepts.contains( o ) ) {
						if ( !edgeStore.containsKey( rel ) ) {
							SEMOSSVertex v1 = createOrRetrieveVertex( s, overlayLevel );
							SEMOSSVertex v2 = createOrRetrieveVertex( o, overlayLevel );
							SEMOSSEdge edge = new SEMOSSEdge( v1, v2, rel );
							storeEdge( edge );
						}

						SEMOSSEdge edge = edgeStore.get( rel );
						edge.setProperty( prop, propval );
						edge.setType( superrel );
					}
				}
			};
			if ( null != preds ) {
				specifics.useInferred( false );
				engine.query( specifics );

				baseedge.useInferred( false );
				engine.query( baseedge );
			}
		}
		catch ( MalformedQueryException ex ) {
			log.error( "BUG!", ex );
		}
	}
}
