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
import gov.va.semoss.om.SEMOSSEdgeImpl;
import gov.va.semoss.om.SEMOSSVertexImpl;
import gov.va.semoss.util.UriBuilder;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

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
	private final boolean makeDupes;

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
	public static Forest<SEMOSSVertex, SEMOSSEdge> convert( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> roots, Search search ) {
		return convert( graph, roots, search, false );
	}

	public static Forest<SEMOSSVertex, SEMOSSEdge> convert( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<SEMOSSVertex> roots, Search s, boolean makeDupes ) {
		return new GraphToTreeConverter( s, makeDupes ).convert( graph, roots );
	}

	public GraphToTreeConverter( Search search, boolean duplicateNodes ) {
		method = search;
		makeDupes = duplicateNodes;
	}

	public GraphToTreeConverter( Search search ) {
		this( search, false );
	}

	public GraphToTreeConverter( boolean duplicateNodes ) {
		this( Search.BFS, duplicateNodes );
	}

	public GraphToTreeConverter() {
		this( false );
	}

	public Forest<SEMOSSVertex, SEMOSSEdge> convert( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Collection<SEMOSSVertex> roots ) {
		DelegateForest<SEMOSSVertex, SEMOSSEdge> newforest = new DelegateForest<>();

		Set<SEMOSSVertex> seen = new HashSet<>();
		for ( SEMOSSVertex v : roots ) {
			if ( Search.DFS == method ) {
				newforest.addTree( dfs( v, graph, seen ) );
			}
			else {
				newforest.addTree( bfs( v, graph, seen ) );
			}
		}

		return newforest;
	}

	private Tree<SEMOSSVertex, SEMOSSEdge> dfs( SEMOSSVertex root, DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Set<SEMOSSVertex> seen ) {
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

	private Tree<SEMOSSVertex, SEMOSSEdge> bfs( SEMOSSVertex root, DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, Set<SEMOSSVertex> seen ) {
		DelegateTree<SEMOSSVertex, SEMOSSEdge> tree = new DelegateTree<>();
		tree.setRoot( root );
		Deque<SEMOSSVertex> todo = new ArrayDeque<>();
		todo.add( root );

		while ( !todo.isEmpty() ) {
			SEMOSSVertex v = todo.poll();
			seen.add( v );

			boolean isdupe = false;
			for ( SEMOSSEdge e : graph.getOutEdges( v ) ) {
				SEMOSSVertex child = graph.getOpposite( v, e );

//				if ( seen.contains( child ) ) {
//					isdupe=true;
//					child = duplicate( child );
//					e = duplicate( e, v, child );
//				}

				if ( !tree.containsVertex( child ) ) {
					tree.addChild( e, v, child );
					if( !isdupe ){
						todo.add( child );
					}
				}
			}
		}

		return tree;
	}

	private static SEMOSSVertex duplicate( SEMOSSVertex old ) {
		URI uri = UriBuilder.getBuilder( old.getURI().getNamespace() ).uniqueUri();
		SEMOSSVertex c2 = new SEMOSSVertexImpl( uri, old.getType(), old.getLabel() );
		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}
		for ( PropertyChangeListener pcl : old.getPropertyChangeListeners() ) {
			c2.addPropertyChangeListener( pcl );
		}

		return c2;
	}

	private static SEMOSSEdge duplicate( SEMOSSEdge old, SEMOSSVertex src, SEMOSSVertex dst ) {
		SEMOSSEdge c2 = new SEMOSSEdgeImpl( src, dst, old.getURI() );
		for ( Map.Entry<URI, Value> en : old.getValues().entrySet() ) {
			c2.setValue( en.getKey(), en.getValue() );
		}
		for ( PropertyChangeListener pcl : old.getPropertyChangeListeners() ) {
			c2.addPropertyChangeListener( pcl );
		}

		return c2;
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
