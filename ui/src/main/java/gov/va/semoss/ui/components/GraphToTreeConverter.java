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
	public static Forest<SEMOSSVertex, SEMOSSEdge> convert(
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> roots, Search search ) {
		return new GraphToTreeConverter( search ).convert( graph, roots );
	}

	public GraphToTreeConverter( Search search ) {
		method = search;
	}

	public GraphToTreeConverter() {
		this( Search.BFS );
	}

	public Forest<SEMOSSVertex, SEMOSSEdge> convert(
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Collection<SEMOSSVertex> roots ) {
		DelegateForest<SEMOSSVertex, SEMOSSEdge> newforest = new DelegateForest<>();
		for ( SEMOSSVertex root : roots ) {
			newforest.addTree( convert( graph, root ) );
		}
		return newforest;
	}

	public Tree<SEMOSSVertex, SEMOSSEdge> convert( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			SEMOSSVertex root ) {
		if ( Search.DFS == method ) {
			return dfs( root, graph );
		}
		else {
			return bfs( root, graph );
		}
	}

	private Tree<SEMOSSVertex, SEMOSSEdge> dfs( SEMOSSVertex root,
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		DelegateTree<SEMOSSVertex, SEMOSSEdge> tree = new DelegateTree<>();
		tree.setRoot( root );
		Queue<SEMOSSVertex> todo = Collections.asLifoQueue( new ArrayDeque<>() );
		todo.add( root );

		while ( !todo.isEmpty() ) {
			SEMOSSVertex v = todo.poll();

			for ( SEMOSSEdge e : graph.getOutEdges( v ) ) {
				SEMOSSVertex child = graph.getOpposite( v, e );
				if ( !tree.containsVertex( child ) ) {
					tree.addChild( e, v, child );
					todo.add( child );
				}
			}
		}

		return tree;
	}

	private Tree<SEMOSSVertex, SEMOSSEdge> bfs( SEMOSSVertex root,
			DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph ) {
		DelegateTree<SEMOSSVertex, SEMOSSEdge> tree = new DelegateTree<>();
		tree.setRoot( root );
		Deque<SEMOSSVertex> todo = new ArrayDeque<>();
		todo.add( root );

		while ( !todo.isEmpty() ) {
			SEMOSSVertex v = todo.poll();

			for ( SEMOSSEdge e : graph.getOutEdges( v ) ) {
				SEMOSSVertex child = graph.getOpposite( v, e );

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
