package com.ostrichemulators.semtool.om;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import com.ostrichemulators.semtool.graph.functions.GraphToTreeConverter;
import com.ostrichemulators.semtool.util.MultiSetMap;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Color;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/*
 * This contains all data that is fundamental to a SEMOSS Graph This data
 * mainly consists of the edgeStore and vertStore as well as models/repository
 * connections
 */
public class TreeGraphDataModel extends GraphDataModel implements PropertyChangeListener {

	private static final Logger log = Logger.getLogger( TreeGraphDataModel.class );
	public static final URI DUPLICATE_OF = Utility.makeInternalUri( "duplicate-of" );
	public static final URI DUPLICATES_SIZE = Utility.makeInternalUri( "num-duplicates" );
	private final Map<Value, GraphElement> trueElements = new HashMap<>();
	private final MultiSetMap<URI, GraphElement> trueUriToDupes = new MultiSetMap<>();

	public TreeGraphDataModel() {
		super( new DelegateForest<>() );
	}

	public TreeGraphDataModel( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> roots ) {
		setGraph( ( DirectedGraph.class.cast( makeDuplicateForest( graph, roots,
				new GraphToTreeConverter() ) ) ) );
	}

	public Forest<SEMOSSVertex, SEMOSSEdge> getForest() {
		return Forest.class.cast( getGraph() );
	}

	public Set<? extends GraphElement> getDuplicatesOf( GraphElement v ) {
		// if our element has a duplicateof property, then figure out 
		// which other elements share the property
		// if there is no duplicateof property, then assume we're looking at
		// the "true" element
		GraphElement trueEle = ( v.hasProperty( DUPLICATE_OF )
				? trueElements.get( v.getValue( DUPLICATE_OF ) )
				: v );
		return trueUriToDupes.getNN( trueEle.getURI() );
	}

	public URI getRealUri( GraphElement v ) {
		// if our element has a duplicateof property, then figure out
		// which other elements share the property
		// if there is no duplicateof property, then assume we're looking at
		// the "true" element
		return ( v.hasProperty( DUPLICATE_OF )
				? URI.class.cast( v.getValue( DUPLICATE_OF ) )
				: v.getURI() );
	}

	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> makeDuplicateForest(
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Collection<SEMOSSVertex> roots,
			GraphToTreeConverter gttc ) {

		DelegateForest<SEMOSSVertex, SEMOSSEdge> newforest = new DelegateForest<>();
		for ( SEMOSSVertex root : roots ) {
			Map<URI, SEMOSSVertex> vlkp = new HashMap<>();
			Map<URI, SEMOSSEdge> elkp = new HashMap<>();

			Tree<URI, URI> tree = gttc.convert( graph, root, vlkp, elkp );

			// convert the URI tree to our node/edge tree
			DelegateTree<SEMOSSVertex, SEMOSSEdge> dupetree = new DelegateTree<>();

			// make duplicate vertices and edges with the new URIs
			Map<URI, SEMOSSVertex> dupesV = new HashMap<>();
			Map<URI, SEMOSSEdge> dupesE = new HashMap<>();
			for ( Map.Entry<URI, SEMOSSVertex> en : vlkp.entrySet() ) {
				dupesV.put( en.getKey(), duplicate( en.getKey(), vlkp ) );
			}
			for ( Map.Entry<URI, SEMOSSEdge> en : elkp.entrySet() ) {
				dupesE.put( en.getKey(), duplicate( en.getKey(), elkp ) );
			}

			dupetree.setRoot( dupesV.get( tree.getRoot() ) );

			Deque<URI> todo = new ArrayDeque<>( tree.getChildren( tree.getRoot() ) );
			while ( !todo.isEmpty() ) {
				URI child = todo.poll();
				URI edge = tree.getParentEdge( child );
				URI parent = tree.getParent( child );

				dupetree.addChild( dupesE.get( edge ),
						dupesV.get( parent ), dupesV.get( child ) );

				todo.addAll( tree.getChildren( child ) );
			}

			newforest.addTree( dupetree );
		}

		ValueFactory vf = new ValueFactoryImpl();
		for ( SEMOSSVertex v : newforest.getVertices() ) {
			v.setValue( DUPLICATES_SIZE,
					vf.createLiteral( getDuplicatesOf( v ).size() ) );
		}
		for ( SEMOSSEdge v : newforest.getEdges() ) {
			v.setValue( DUPLICATES_SIZE,
					vf.createLiteral( getDuplicatesOf( v ).size() ) );
		}

		return newforest;
	}

	private <X extends GraphElement> X duplicate( URI uri, Map<URI, X> lkp ) {
		X old = lkp.get( uri );
		X newer = old.duplicate();
		newer.setURI( uri );
		newer.setValue( DUPLICATE_OF, old.getURI() );

		trueUriToDupes.add( old.getURI(), newer );
		trueElements.put( old.getURI(), old );

		newer.addPropertyChangeListener( this );
		return newer;
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt ) {
		// propagate changes from the "real" vertices to our duplicate vertices
		Object src = evt.getSource();
		String propname = evt.getPropertyName();
		Object newval = evt.getNewValue();

		GraphElement ge = GraphElement.class.cast( src );
		Set<? extends GraphElement> dupes = getDuplicatesOf( ge );

		if ( AbstractGraphElement.CHANGE_COLOR.equals( propname ) ) {
			for ( GraphElement e : dupes ) {
				e.setColor( Color.class.cast( newval ) );
			}
		}
		else if ( SEMOSSVertex.CHANGE_SHAPE.equals( propname ) ) {
			for ( GraphElement e : dupes ) {
				SEMOSSVertex.class.cast( e ).setShape( Shape.class.cast( newval ) );
			}
		}
		else if ( null != propname ) {
			for ( GraphElement e : dupes ) {
				e.setValue( new URIImpl( propname ), Value.class.cast( newval ) );
			}
		}
	}
}
