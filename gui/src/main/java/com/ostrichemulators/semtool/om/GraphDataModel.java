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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryException;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManager;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManagerFactory;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import com.ostrichemulators.semtool.util.Utility;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class GraphDataModel {

	private static final Logger log = Logger.getLogger( GraphDataModel.class );

	private final List<GraphModelListener> listenees = new ArrayList<>();
	private final Map<GraphElement, Integer> level = new HashMap<>();

	protected Map<IRI, SEMOSSVertex> vertStore = new HashMap<>();
	protected Map<String, SEMOSSEdge> edgeStore = new HashMap<>();

	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> vizgraph;
  private final ValueFactory vf = SimpleValueFactory.getInstance();

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
	 * in which {@link GraphElement#getIRI()} matches the given node's value. Note
	 * that each returned GraphElement will have a different
	 * {@link GraphElement#getGraphId() GraphID}
	 *
	 * @param <X>
	 * @param v
	 * @return
	 */
	public <X extends GraphElement> Set<X> getDuplicatesOf( X v ) {
		IRI IRI = v.getIRI();

		Set<X> set = new HashSet<>();
		if ( v.isNode() ) {
			for ( SEMOSSVertex s : getGraph().getVertices() ) {
				if ( s.getIRI().equals( IRI ) ) {
					set.add( (X) s );
				}
			}
		}
		else {
			for ( SEMOSSEdge s : getGraph().getEdges() ) {
				if ( s.getIRI().equals( IRI ) ) {
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
			Map<Value, IRI> nonIRIIds = new HashMap<>();

			Set<IRI> needProps = new HashSet<>();
			for ( Resource r : model.subjects() ) {
				needProps.add( IRI.class.cast( r ) );
			}

			for ( Statement s : model ) {
				IRI sub = IRI.class.cast( s.getSubject() );
				IRI pred = s.getPredicate();
				Value obj = s.getObject();

				if ( obj instanceof IRI ) {
					needProps.add( IRI.class.cast( obj ) );
				}

				SEMOSSVertex src = createOrRetrieveVertex( sub, overlayLevel );
				SEMOSSVertex dst;
				if ( obj instanceof IRI ) {
					dst = createOrRetrieveVertex( IRI.class.cast( obj ), overlayLevel );
				}
				else {
					IRI IRI = Utility.getUniqueIri();
					dst = createOrRetrieveVertex( IRI, overlayLevel );
					dst.setLabel( obj.stringValue() );
					IRI type = RDFDatatypeTools.getDatatype( obj );
					dst.setType( type );
					nonIRIIds.put( obj, IRI );
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

			Map<IRI, String> edgelabels
					= Utility.getInstanceLabels( model.predicates(), engine );
			for ( Statement s : model ) {

				String edgekey = s.getPredicate().stringValue()
						+ s.getSubject().stringValue()
						+ ( nonIRIIds.containsKey( s.getObject() )
								? nonIRIIds.get( s.getObject() )
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

	public Collection<GraphElement> addGraphLevel( Collection<IRI> nodes,
			IEngine engine, int overlayLevel ) {
		try {
			for ( IRI sub : nodes ) {
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
				nodesToRemove.add(vertStore.remove(v.getIRI() ) );
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
				String edgekey = getEdgeKey(e.getIRI(), src, dst );
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

	protected SEMOSSVertex createOrRetrieveVertex( IRI vertexKey, int overlayLevel ) {
		IRI IRI = vf.createIRI( vertexKey.stringValue() );
		if ( !vertStore.containsKey( IRI ) ) {
			SEMOSSVertex vertex = createVertex( IRI );
			level.put( vertex, overlayLevel );
			vertStore.put( IRI, vertex );
		}

		return vertStore.get( IRI );
	}

	protected SEMOSSVertex createVertex( IRI IRI ) {
		SEMOSSVertexImpl impl = new SEMOSSVertexImpl( IRI );
		impl.setGraphId( Utility.getUniqueIri() );
		return impl;
	}

	protected final String getEdgeKey( IRI edge, SEMOSSVertex src, SEMOSSVertex dst ) {
		return getEdgeKey(edge, src.getIRI(), dst.getIRI() );
	}

	protected final String getEdgeKey( IRI edge, IRI src, IRI dst ) {
		return edge.stringValue() + src + dst;
	}

	protected SEMOSSEdge createOrRetrieveEdge( IRI edgeKey, SEMOSSVertex src,
			SEMOSSVertex dst, int overlayLevel ) {
		IRI IRI = vf.createIRI( edgeKey.stringValue() );
		String key = getEdgeKey( edgeKey, src, dst );

		if ( !edgeStore.containsKey( key ) ) {
			SEMOSSEdge edge = createEdge( src, dst, IRI );
			level.put( edge, overlayLevel );
			edgeStore.put( key, edge );
		}

		return edgeStore.get( key );
	}

	protected SEMOSSEdge createEdge( SEMOSSVertex src, SEMOSSVertex dst, IRI IRI ) {
		SEMOSSEdge edge = new SEMOSSEdgeImpl( IRI );
		edge.setGraphId( Utility.getUniqueIri() );
		edge.setType( IRI );
		return edge;

	}

	private void fetchProperties( Collection<IRI> concepts, Collection<IRI> preds,
			IEngine engine, int overlayLevel ) throws RepositoryException, QueryEvaluationException {

		StructureManager sm = StructureManagerFactory.getStructureManager( engine );
		String conceptimplosion = Utility.implode( concepts );
		String conceptprops
				= "SELECT ?s ?p ?o ?type WHERE {"
				+ " ?s ?p ?o . "
				+ " ?s a ?type ."
				+ " FILTER ( isLiteral( ?o ) ) }"
				+ "VALUES ?s { " + conceptimplosion + " }";

		try {
			VoidQueryAdapter cqa = new VoidQueryAdapter( conceptprops ) {

				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					IRI s = IRI.class.cast( set.getValue( "s" ) );
					IRI prop = IRI.class.cast( set.getValue( "p" ) );
					Value val = set.getValue( "o" );
					IRI type = IRI.class.cast( set.getValue( "type" ) );

					SEMOSSVertex v = createOrRetrieveVertex( s, overlayLevel );
					v.setValue( prop, val );
					v.setType( type );
				}
			};

			if ( null != concepts ) {
				cqa.useInferred( false );
				engine.query( cqa );
			}

			if ( null != preds ) {
				preds = sm.getTopLevelRelations( preds );

				// do the same thing, but for edges
				String specificEdgeProps
						= "SELECT ?s ?rel ?o ?prop ?literal ?superrel WHERE {\n"
						+ "  ?rel ?prop ?literal ; rdfs:subPropertyOf ?superrel .\n"
						+ "  ?s ?rel ?o .\n"
						+ "  VALUES ?s { " + conceptimplosion + " } .\n"
						+ "  VALUES ?o { " + conceptimplosion + " } .\n"
						+ "  FILTER ( isLiteral( ?literal ) )\n"
						+ "}\n"
						+ "VALUES ?superrel { " + Utility.implode( preds ) + " }";

				VoidQueryAdapter specifics = new VoidQueryAdapter( specificEdgeProps ) {

					@Override
					public void handleTuple( BindingSet set, ValueFactory fac ) {
						// ?s ?rel ?o ?prop ?literal
						IRI s = IRI.class.cast( set.getValue( "s" ) );
						IRI rel = IRI.class.cast( set.getValue( "rel" ) );
						IRI prop = IRI.class.cast( set.getValue( "prop" ) );
						IRI o = IRI.class.cast( set.getValue( "o" ) );
						Value propval = set.getValue( "literal" );
						IRI superrel = IRI.class.cast( set.getValue( "superrel" ) );

						SEMOSSVertex src = createOrRetrieveVertex( s, overlayLevel );
						SEMOSSVertex dst = createOrRetrieveVertex( o, overlayLevel );

						// we don't know if our edge is stored as the generic or specific
						// version (if it's been stored at all). Regardless, set the IRI and
						// type of the edge as best as we can
						String key = getEdgeKey( superrel, src, dst );
						IRI fetchkey = ( edgeStore.containsKey( key ) ? superrel : rel );

						SEMOSSEdge edge = createOrRetrieveEdge( fetchkey, src, dst, overlayLevel );
						edge.setValue( prop, propval );
						edge.setIRI( rel );
						edge.setType( superrel );
					}
				};

				specifics.useInferred( false );
				specifics.bind( "semrel", engine.getSchemaBuilder().getRelationIri().build() );
				log.debug( specifics.bindAndGetSparql() );
				engine.query( specifics );
			}
		}
		catch ( MalformedQueryException ex ) {
			log.error( "BUG!", ex );
		}
	}
}
