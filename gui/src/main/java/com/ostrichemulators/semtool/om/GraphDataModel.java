package com.ostrichemulators.semtool.om;

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
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import com.ostrichemulators.semtool.util.Utility;
import org.openrdf.model.impl.URIImpl;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class GraphDataModel {

	private static final Logger log = Logger.getLogger( GraphDataModel.class );

	private final List<GraphModelListener> listenees = new ArrayList<>();
	private final Map<GraphElement, Integer> level = new HashMap<>();

	protected Map<URI, SEMOSSVertex> vertStore = new HashMap<>();
	protected Map<String, SEMOSSEdge> edgeStore = new HashMap<>();

	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> vizgraph;

	public GraphDataModel() {
		this( new DirectedSparseGraph<>() );
	}

	public GraphDataModel( DirectedGraph<SEMOSSVertex, SEMOSSEdge> g ) {
		vizgraph = g;
	}

	public void addModelListener( GraphModelListener l ) {
		listenees.add( l );
	}

	public void removeModelListener( GraphModelListener l ) {
		listenees.remove( l );
	}

	protected void fireModelChanged( int olevel ) {
		for ( GraphModelListener l : listenees ) {
			l.changed( vizgraph, olevel, this );
		}
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

	public void setGraph( DirectedGraph<SEMOSSVertex, SEMOSSEdge> f ) {
		vizgraph = f;
		fireModelChanged( 1 );
	}

	/**
	 * Gets duplicates (if they exist) of the given node. A duplicate node is one
	 * in which {@link GraphElement#getURI()} matches the given node's value. Note
	 * that each returned GraphElement will have a different
	 * {@link GraphElement#getGraphId() GraphID}
	 *
	 * @param <X>
	 * @param v
	 * @return
	 */
	public <X extends GraphElement> Set<X> getDuplicatesOf( X v ) {
		URI uri = v.getURI();

		Set<X> set = new HashSet<>();
		if ( v.isNode() ) {
			for ( SEMOSSVertex s : getGraph().getVertices() ) {
				if ( s.getURI().equals( uri ) ) {
					set.add( (X) s );
				}
			}
		}
		else {
			for ( SEMOSSEdge s : getGraph().getEdges() ) {
				if ( s.getURI().equals( uri ) ) {
					set.add( (X) s );
				}
			}
		}

		return set;
	}

	/**
	 * Adds new nodes/edges to the graph at the specified "redo" level.
	 *
	 * @param model the nodes/edges to add
	 * @param engine the engine to get other data from
	 * @param overlayLevel the level of the nodes
	 * @return the elements added to the graph (equivalent to
	 * {@link #elementsFromLevel(int)} )
	 */
	public Collection<GraphElement> addGraphLevel( Model model, IEngine engine,
			int overlayLevel ) {

		try {
			Map<Value, URI> nonUriIds = new HashMap<>();

			Set<URI> needProps = new HashSet<>();
			for ( Resource r : model.subjects() ) {
				needProps.add( URI.class.cast( r ) );
			}

			for ( Statement s : model ) {
				URI sub = URI.class.cast( s.getSubject() );
				URI pred = s.getPredicate();
				Value obj = s.getObject();

				if ( obj instanceof URI ) {
					needProps.add( URI.class.cast( obj ) );
				}

				SEMOSSVertex src = createOrRetrieveVertex( sub, overlayLevel );
				SEMOSSVertex dst;
				if ( obj instanceof URI ) {
					dst = createOrRetrieveVertex( URI.class.cast( obj ), overlayLevel );
				}
				else {
					URI uri = Utility.getUniqueUri();
					dst = createOrRetrieveVertex( uri, overlayLevel );
					dst.setLabel( obj.stringValue() );
					URI type = RDFDatatypeTools.getDatatype( obj );
					dst.setType( type );
					nonUriIds.put( obj, uri );
				}

				vizgraph.addVertex( src );
				vizgraph.addVertex( dst );

				SEMOSSEdge edge = createOrRetrieveEdge( pred, src, dst, overlayLevel );

				try {
					vizgraph.addEdge( edge, src, dst, EdgeType.DIRECTED );
				}
				catch ( Exception t ) {
					log.error( t, t );
				}
			}

			Map<URI, String> edgelabels
					= Utility.getInstanceLabels( model.predicates(), engine );
			for ( Statement s : model ) {

				String edgekey = s.getPredicate().stringValue()
						+ s.getSubject().stringValue()
						+ ( nonUriIds.containsKey( s.getObject() )
								? nonUriIds.get( s.getObject() )
								: s.getObject() ).stringValue();
				SEMOSSEdge edge = edgeStore.get( edgekey );
				String elabel = edgelabels.get( s.getPredicate() );
				edge.setLabel( elabel );
			}

			fetchProperties( needProps, model.predicates(), engine, overlayLevel );
		}
		catch ( RepositoryException | QueryEvaluationException e ) {
			log.error( e, e );
		}

		fireModelChanged( overlayLevel );
		return elementsFromLevel( overlayLevel );
	}

	public Collection<GraphElement> addGraphLevel( Collection<URI> nodes,
			IEngine engine, int overlayLevel ) {
		try {
			for ( URI sub : nodes ) {
				SEMOSSVertex vert1 = createOrRetrieveVertex( sub, overlayLevel );
				vizgraph.addVertex( vert1 );
			}

			fetchProperties( nodes, null, engine, overlayLevel );
		}
		catch ( RepositoryException | QueryEvaluationException e ) {
			log.error( e, e );
		}

		fireModelChanged( overlayLevel );
		return elementsFromLevel( overlayLevel );
	}

	public List<GraphElement> elementsFromLevel( int overlayLevel ) {
		List<GraphElement> list = new ArrayList<>();
		for ( Map.Entry<GraphElement, Integer> en : level.entrySet() ) {
			if ( overlayLevel == en.getValue() ) {
				list.add( en.getKey() );
			}
		}

		return list;
	}

	/**
	 * Removes elements that are "undone" when the history tree branches
	 *
	 * @param overlayLevel
	 * @return the removed elements
	 */
	public Collection<GraphElement> removeElementsSinceLevel( int overlayLevel ) {
		// if we've undone some data and now want to add 
		// something else, get rid of the future redo data
		List<SEMOSSVertex> nodesToRemove = new ArrayList<>();
		for ( SEMOSSVertex v : vizgraph.getVertices() ) {
			if ( getLevel( v ) > overlayLevel ) {
				nodesToRemove.add( vertStore.remove( v.getURI() ) );
			}
		}

		List<GraphElement> removers = new ArrayList<>();
		for ( SEMOSSVertex v : nodesToRemove ) {
			// edges will be removed automatically...but sync our level mapping
			Collection<SEMOSSEdge> edges = vizgraph.getIncidentEdges( v );
			removers.addAll( edges );

			for ( SEMOSSEdge e : edges ) {
				level.remove( e );

				SEMOSSVertex src = vizgraph.getSource( e );
				SEMOSSVertex dst = vizgraph.getDest( e );
				String edgekey = getEdgeKey( e.getURI(), src, dst );
				edgeStore.remove( edgekey );
			}

			vizgraph.removeVertex( v );
			level.remove( v );
		}

		removers.addAll( nodesToRemove );
		fireModelChanged( overlayLevel );
		return removers;
	}

	public int getLevel( GraphElement check ) {
		if ( level.containsKey( check ) ) {
			return level.get( check );
		}
		return 0;
	}

	/**
	 * Is this node present at the given level (is it's level &gt;?= the given
	 * level)
	 *
	 * @param check the element to check
	 * @param level is it present at this level?
	 * @return
	 */
	public boolean presentAtLevel( GraphElement check, int level ) {
		return getLevel( check ) <= level;
	}

	protected SEMOSSVertex createOrRetrieveVertex( URI vertexKey, int overlayLevel ) {
		URI uri = new URIImpl( vertexKey.stringValue() );
		if ( !vertStore.containsKey( uri ) ) {
			SEMOSSVertex vertex = createVertex( uri );
			level.put( vertex, overlayLevel );
			vertStore.put( uri, vertex );
		}

		return vertStore.get( uri );
	}

	protected SEMOSSVertex createVertex( URI uri ) {
		SEMOSSVertexImpl impl = new SEMOSSVertexImpl( uri );
		impl.setGraphId( Utility.getUniqueUri() );
		return impl;
	}

	protected final String getEdgeKey( URI edge, SEMOSSVertex src, SEMOSSVertex dst ) {
		return edge.stringValue() + src.getURI() + dst.getURI();
	}

	protected SEMOSSEdge createOrRetrieveEdge( URI edgeKey, SEMOSSVertex src,
			SEMOSSVertex dst, int overlayLevel ) {
		URI uri = new URIImpl( edgeKey.stringValue() );
		String key = getEdgeKey( edgeKey, src, dst );

		if ( !edgeStore.containsKey( key ) ) {
			SEMOSSEdge edge = createEdge( src, dst, uri );
			level.put( edge, overlayLevel );
			edgeStore.put( key, edge );
		}

		return edgeStore.get( key );
	}

	protected SEMOSSEdge createEdge( SEMOSSVertex src, SEMOSSVertex dst, URI uri ) {
		SEMOSSEdge edge = new SEMOSSEdgeImpl( uri );
		edge.setGraphId( Utility.getUniqueUri() );
		edge.setType( uri );
		return edge;

	}

	private void fetchProperties( Collection<URI> concepts, Collection<URI> preds,
			IEngine engine, int overlayLevel ) throws RepositoryException, QueryEvaluationException {

		String conceptprops
				= "SELECT ?s ?p ?o ?type WHERE {"
				+ " ?s ?p ?o . "
				+ " ?s a ?type ."
				+ " FILTER ( isLiteral( ?o ) ) }"
				+ "VALUES ?s { " + Utility.implode( concepts, "<", ">", " " ) + " }";

		try {
			VoidQueryAdapter cqa = new VoidQueryAdapter( conceptprops ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI prop = URI.class.cast( set.getValue( "p" ) );
					Value val = set.getValue( "o" );
					URI type = URI.class.cast( set.getValue( "type" ) );

					SEMOSSVertex v = createOrRetrieveVertex( s, overlayLevel );
					v.setValue( prop, val );
					v.setType( type );
				}
			};

			if ( null != concepts ) {
				cqa.useInferred( false );
				engine.query( cqa );
			}

			// do the same thing, but for edges
			String specificEdgeProps
					= "SELECT ?s ?rel ?o ?prop ?literal ?superrel WHERE {"
					+ "  ?rel ?prop ?literal ; a ?superrel ."
					+ "  ?s ?rel ?o ."
					+ "  FILTER ( isLiteral( ?literal ) )"
					+ "}"
					+ "VALUES ?superrel { " + Utility.implode( preds, "<", ">", " " ) + " }";

			VoidQueryAdapter specifics = new VoidQueryAdapter( specificEdgeProps ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					// ?s ?rel ?o ?prop ?literal
					URI s = URI.class.cast( set.getValue( "s" ) );
					URI rel = URI.class.cast( set.getValue( "rel" ) );
					URI prop = URI.class.cast( set.getValue( "prop" ) );
					URI o = URI.class.cast( set.getValue( "o" ) );
					Value propval = set.getValue( "literal" );
					URI superrel = URI.class.cast( set.getValue( "superrel" ) );

					if ( concepts.contains( s ) && concepts.contains( o ) ) {
						SEMOSSEdge edge = createOrRetrieveEdge(
								superrel,
								createOrRetrieveVertex( s, overlayLevel ),
								createOrRetrieveVertex( o, overlayLevel ),
								overlayLevel );
						edge.setValue( prop, propval );
						edge.setURI( rel );
						edge.setType( superrel );
					}
				}
			};

			if ( null != preds ) {
				specifics.useInferred( false );
				specifics.bind( "semrel", engine.getSchemaBuilder().getRelationUri().build() );
				log.debug( specifics.bindAndGetSparql() );
				engine.query( specifics );
			}
		}
		catch ( MalformedQueryException ex ) {
			log.error( "BUG!", ex );
		}
	}
}
