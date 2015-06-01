/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

/**
 * This class extends downstream processing in order to convert the graph into
 * the tree format.
 */
public class GraphToTreeConverter extends AbstractAction {

	private static final Logger logger = Logger.getLogger( GraphToTreeConverter.class );
	private final GraphPlaySheet gps;
	private final Set<SEMOSSVertex> roots = new HashSet<>();
	private DelegateForest<SEMOSSVertex, SEMOSSEdge> newForest;

	/**
	 * Constructor for GraphToTreeConverter.
	 *
	 * @param p Graph playsheet to be set.
	 */
	public GraphToTreeConverter( GraphPlaySheet p ) {
		gps = p;
		roots.addAll( p.getView().getPickedVertexState().getPicked() );
	}

	public static <V, E> Forest<V, E> convert( DirectedGraph<V, E> graph, Set<V> roots ) {
		DelegateForest<V, E> newforest = new DelegateForest<>();
		for ( V v : roots ) {
			newforest.addTree( makeTree( v, graph ) );
		}

		return newforest;
	}

	private static <V, E> Tree<V, E> makeTree( V root, DirectedGraph<V, E> graph ) {
		DelegateTree<V, E> tree = new DelegateTree<>();
		tree.setRoot( root );
		Deque<V> todo = new ArrayDeque<>();
		todo.add( root );
		
		while ( !todo.isEmpty() ) {
			V v = todo.pop();

			for ( E e : graph.getOutEdges( v ) ) {
				V child = graph.getOpposite( v, e );
				if ( !tree.containsVertex( child ) ) {
					tree.addChild( e, v, child );
					todo.add( child );
				}
			}
		}

		return tree;
	}

	/**
	 * Resets the hashtables containing URIs of the vertices and selected
	 * vertices.
	 */
	private void resetConverter() {
		newForest = new DelegateForest<>();
	}

	/**
	 * Resets the converters, sets the forest and selected nodes, performs
	 * downstream processing on the current nodes, and sets the forest.
	 */
	@Override
	public void actionPerformed( ActionEvent ae ) {
		resetConverter();
		Forest<SEMOSSVertex, SEMOSSEdge> forest = gps.getGraphData().asForest();
		DelegateForest<SEMOSSVertex, SEMOSSEdge> newforest = new DelegateForest<>();

		for ( SEMOSSVertex v : roots ) {
			newforest.addTree( makeTree( v, forest, new HashSet<>() ) );
		}

		GraphPlaySheet.printForest( newforest );

		gps.setForest( forest );
	}

	public Tree<SEMOSSVertex, SEMOSSEdge> makeTree( SEMOSSVertex root,
			Forest<SEMOSSVertex, SEMOSSEdge> forest, Set<SEMOSSVertex> seen ) {

		DelegateTree<SEMOSSVertex, SEMOSSEdge> tree = new DelegateTree<>();
		tree.setRoot( root );

		Deque<SEMOSSVertex> todo = new ArrayDeque<>();
		todo.add( root );

		while ( !todo.isEmpty() ) {
			SEMOSSVertex v = todo.pop();

			for ( SEMOSSEdge e : forest.getOutEdges( v ) ) {
				SEMOSSVertex child = forest.getOpposite( v, e );
				if ( !seen.contains( child ) ) {
					tree.addChild( e, v, child );
					todo.add( child );
				}
			}
		}

		return tree;
	}
}
