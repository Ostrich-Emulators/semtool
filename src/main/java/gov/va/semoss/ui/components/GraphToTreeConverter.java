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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Queue;

/**
 * This class extends downstream processing in order to convert the graph into
 * the tree format.
 */
public class GraphToTreeConverter {

	public static enum Search {

		DFS, BFS
	};

	private static final Logger log = Logger.getLogger( GraphToTreeConverter.class );

	/**
	 * Converts the given graph to a tree using a DFS
	 *
	 * @param <V>
	 * @param <E>
	 * @param graph
	 * @param roots
	 * @return
	 */
	public static <V, E> Forest<V, E> convert( DirectedGraph<V, E> graph,
			Collection<V> roots, Search s ) {
		DelegateForest<V, E> newforest = new DelegateForest<>();
		for ( V v : roots ) {
			if ( Search.DFS == s ) {
				newforest.addTree( dfs( v, graph ) );
			}
			else {
				newforest.addTree( bfs( v, graph ) );
			}
		}

		return newforest;
	}

	/**
	 * A convenience function to {@link
	 * #convert(edu.uci.ics.jung.graph.DirectedGraph, java.util.Collection,
	 * gov.va.semoss.ui.components.GraphToTreeConverter.Search) } using
	 * {@link Search#BFS}
	 *
	 * @param <V>
	 * @param <E>
	 * @param graph
	 * @param roots
	 * @return
	 */
	public static <V, E> Forest<V, E> convert( DirectedGraph<V, E> graph,
			Collection<V> roots ) {
		return convert( graph, roots, Search.BFS );
	}

	private static <V, E> Tree<V, E> dfs( V root, DirectedGraph<V, E> graph ) {
		DelegateTree<V, E> tree = new DelegateTree<>();
		tree.setRoot( root );
		Queue<V> todo = Collections.asLifoQueue( new ArrayDeque<>() );
		todo.add( root );

		while ( !todo.isEmpty() ) {
			V v = todo.poll();

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

	private static <V, E> Tree<V, E> bfs( V root, DirectedGraph<V, E> graph ) {
		DelegateTree<V, E> tree = new DelegateTree<>();
		tree.setRoot( root );
		Deque<V> todo = new ArrayDeque<>();
		todo.add( root );

		while ( !todo.isEmpty() ) {
			V v = todo.poll();

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

	public static void printForest( Forest<SEMOSSVertex, SEMOSSEdge> forest ) {
		for ( Tree<SEMOSSVertex, SEMOSSEdge> tree : forest.getTrees() ) {
			printTree( tree );
		}
	}

	public static void printTree( Tree<SEMOSSVertex, SEMOSSEdge> tree ) {
		printTree( tree.getRoot(), tree, 0 );
	}

	public static void printTree( SEMOSSVertex root,
			Tree<SEMOSSVertex, SEMOSSEdge> tree, int depth ) {
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < depth; i++ ) {
			sb.append( "  " );
		}
		sb.append( root );
		log.debug( sb.toString() );

		for ( SEMOSSVertex child : tree.getChildren( root ) ) {
			printTree( child, tree, depth + 1 );
		}
	}
}
