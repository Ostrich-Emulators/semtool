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
package com.ostrichemulators.semtool.graph.functions;

import org.apache.log4j.Logger;

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
public class GraphToTreeConverter<V, E> {

	public static enum Search {

		DFS, BFS
	};

	private static final Logger log = Logger.getLogger( GraphToTreeConverter.class );
	private final Search method;

	/**
	 * Converts the given graph to a tree
	 *
	 * @param <SEMOSSVertex>
	 * @param <E>
	 * @param graph
	 * @param roots
	 * @param search
	 * @return
	 */
	public static <V, E> Forest<V, E> convert(
			DirectedGraph<V, E> graph, Collection<V> roots, Search search ) {
		return new GraphToTreeConverter( search ).convert( graph, roots );
	}

	public GraphToTreeConverter( Search search ) {
		method = search;
	}

	public GraphToTreeConverter() {
		this( Search.BFS );
	}

	public Forest<V, E> convert( DirectedGraph<V, E> graph, Collection<V> roots ) {
		DelegateForest<V, E> newforest = new DelegateForest<>();
		for ( V root : roots ) {
			newforest.addTree( convert( graph, root ) );
		}
		return newforest;
	}

	public Tree<V, E> convert( DirectedGraph<V, E> graph, V root ) {
		if ( Search.DFS == method ) {
			return dfs( root, graph );
		}
		else {
			return bfs( root, graph );
		}
	}

	private Tree<V, E> dfs( V root, DirectedGraph<V, E> graph ) {
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

	private Tree<V, E> bfs( V root, DirectedGraph<V, E> graph ) {
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

	public static <V, E> void printForest( Forest<V, E> forest ) {
		for ( Tree<V, E> tree : forest.getTrees() ) {
			printTree( tree );
		}
	}

	public static <V, E> void printTree( Tree<V, E> tree ) {
		printTree( tree.getRoot(), tree, 0 );
	}

	public static <V, E> void printTree( V root,
			Tree<V, E> tree, int depth ) {
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < depth; i++ ) {
			sb.append( "  " );
		}
		sb.append( root );
		log.debug( sb.toString() );

		for ( V child : tree.getChildren( root ) ) {
			printTree( child, tree, depth + 1 );
		}
	}
}
