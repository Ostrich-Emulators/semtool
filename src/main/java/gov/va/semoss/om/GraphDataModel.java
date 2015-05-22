package gov.va.semoss.om;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jgrapht.graph.SimpleGraph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class GraphDataModel {

	private static final Logger log = Logger.getLogger( GraphDataModel.class );

	private final Set<String> baseFilterSet = new HashSet<>();
	protected Map<Resource, String> labelcache = new HashMap<>();

	private boolean search, prop, sudowl;

	protected Map<URI, SEMOSSVertex> vertStore = new HashMap<>();
	protected Map<URI, SEMOSSEdge> edgeStore = new HashMap<>();

	private boolean filterOutOwlData = true;
	private URI typeOrSubclass = RDF.TYPE;
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
		for ( SEMOSSEdge v : vizgraph.getEdges() ) {
			graph.addEdge( v.getInVertex(), v.getOutVertex(), v );
		}

		return graph;
	}

	public void setGraph( DirectedGraph<SEMOSSVertex, SEMOSSEdge> f ) {
		vizgraph = f;
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
	public void addGraphLevel( Model model, IEngine engine, int overlayLevel ) {
		if ( model.isEmpty() ) {
			return;
		}

		removeElementsSinceLevel( overlayLevel );

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
				edge.setLevel( overlayLevel );
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

			fetchProperties( needProps, model.predicates(), engine, overlayLevel );
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
			if ( v.getLevel() > overlayLevel ) {
				nodesToRemove.add( vertStore.remove( v.getURI() ) );
			}
		}

		for ( SEMOSSVertex v : nodesToRemove ) {
			vizgraph.removeVertex( v );
		}

		// edges are removed automatically...
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

	public SEMOSSVertex createOrRetrieveVertex( URI vertexKey, int overlayLevel ) {
		if ( !vertStore.containsKey( vertexKey ) ) {
			SEMOSSVertex vertex = new SEMOSSVertex( vertexKey );
			vertex.setLevel( overlayLevel );
			storeVertex( vertex );
		}

		return vertStore.get( vertexKey );
	}

	public void storeVertex( SEMOSSVertex vert ) {
		URI key = vert.getURI();
		setLabel( vert );
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

	public Map<URI, SEMOSSVertex> getVertStore() {
		return this.vertStore;
	}

	public Map<URI, SEMOSSEdge> getEdgeStore() {
		return this.edgeStore;
	}

	public void removeView( String query, IEngine engine ) {
		throw new UnsupportedOperationException( "Not yet implemented!" );
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

	private void fetchProperties( Collection<Resource> concepts, Collection<URI> preds,
			IEngine engine, int overlayLevel ) throws RepositoryException, QueryEvaluationException {

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

					SEMOSSVertex v = createOrRetrieveVertex( s, overlayLevel );
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
			eqa.useInferred( false );
			engine.query( eqa );
		}
		catch ( MalformedQueryException ex ) {
			log.error( "BUG!", ex );
		}
	}
}
