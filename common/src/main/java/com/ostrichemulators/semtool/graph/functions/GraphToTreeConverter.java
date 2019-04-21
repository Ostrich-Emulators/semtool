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

import com.ostrichemulators.semtool.util.Utility;
import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.eclipse.rdf4j.model.URI;

/**
 * This class extends downstream processing in order to convert the graph into
 * the tree format.
 *
 * @param <V>
 * @param <E>
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
	 * @param <V>
	 * @param <E>
	 * @param graph
	 * @param roots
	 * @param search
	 * @return
	 * @throws
	 * com.ostrichemulators.semtool.graph.functions.GraphToTreeConverter.TreeConversionException
	 */
	public static <V, E> Forest<V, E> convert(
			DirectedGraph<V, E> graph, Collection<V> roots, Search search ) throws TreeConversionException {
		return new GraphToTreeConverter( search ).convert( graph, roots );
	}

	public static <V, E> Forest<V, E> convertq(
			DirectedGraph<V, E> graph, Collection<V> roots, Search search ) {
		return new GraphToTreeConverter( search ).convertq( graph, roots );
	}

	public GraphToTreeConverter( Search search ) {
		method = search;
	}

	public GraphToTreeConverter() {
		this( Search.BFS );
	}

	/**
	 * @see #convertq(edu.uci.ics.jung.graph.DirectedGraph, java.lang.Object)
	 *
	 * @param graph
	 * @param roots
	 * @return
	 */
	public Forest<V, E> convertq( DirectedGraph<V, E> graph, Collection<V> roots ) {
		DelegateForest<V, E> newforest = new DelegateForest<>();
		for ( V root : roots ) {
			newforest.addTree( convertq( graph, root ) );
		}
		return newforest;
	}

	public Forest<V, E> convert( DirectedGraph<V, E> graph, Collection<V> roots ) throws TreeConversionException {
		DelegateForest<V, E> newforest = new DelegateForest<>();
		for ( V root : roots ) {
			newforest.addTree( convert( graph, root ) );
		}
		return newforest;
	}

	/**
	 * Converts the given graph "quietly." It will not throw exceptions, but will
	 * skip edges to vertices that already have a parent edge
	 *
	 * @param graph
	 * @param root
	 * @return
	 */
	public Tree<V, E> convertq( DirectedGraph<V, E> graph, V root ) {
		try {
			return iconvert( graph, root, false );
		}
		catch ( Exception x ) {
			log.error( "BUG: this exception should never be thrown!" );
		}

		return null;
	}

	public Tree<V, E> convert( DirectedGraph<V, E> graph, V root ) throws TreeConversionException {
		return iconvert( graph, root, true );
	}

	/**
	 * Creates a tree using random, unique URIs instead of actual vertices and
	 * nodes. The lookup maps are populated with the conversions. NOTE: the Tree
	 * can contain duplicates in the sense that different URIs might map to the
	 * same vertex/edge.
	 *
	 * @param graph the graph to convert
	 * @param root the root of the tree
	 * @param vlookup a (usually empty) map that will be populated with the
	 * uri-to-vertex lookup
	 * @param elookup a (usually empty) map that will be populated with the
	 * uri-to-edge lookup
	 * @return a valid tree, no matter what
	 */
	public Tree<URI, URI> convert( DirectedGraph<V, E> graph,
			V root, Map<URI, V> vlookup, Map<URI, E> elookup ) {

		DelegateTree<URI, URI> tree = new DelegateTree<>();
		Map<V, URI> revlkp = new HashMap<>();

		ArrayDeque<V> deque = new ArrayDeque<>();
		Queue<V> todo = ( Search.DFS == method
				? Collections.asLifoQueue( deque )
				: deque );

		URI rootu = Utility.getUniqueIri();
		vlookup.put( rootu, root );
		revlkp.put( root, rootu );
		tree.setRoot( rootu );

		// avoid cycles in the graph
		Set<E> edgesToSkip = new HashSet<>();

		todo.add( root );
		while ( !todo.isEmpty() ) {
			V v = todo.poll();
			URI srcuri = revlkp.get( v );
			// once we visit a node, we can never end
			// up there again, or we're not acyclic
			edgesToSkip.addAll( graph.getInEdges( v ) );

			Set<E> outgoings = new HashSet<>( graph.getOutEdges( v ) );
			outgoings.removeAll( edgesToSkip );

			for ( E e : outgoings ) {
				V child = graph.getOpposite( v, e );

				URI edgeuri = Utility.getUniqueIri();
				URI tgturi = Utility.getUniqueIri();

				elookup.put( edgeuri, e );
				vlookup.put( tgturi, child );
				revlkp.put( child, tgturi );

				tree.addChild( edgeuri, srcuri, tgturi );
				todo.add( child );
			}
		}

		return tree;
	}

	private Tree<V, E> iconvert( DirectedGraph<V, E> graph, V root,
			boolean throwExceptions ) throws TreeConversionException {

		ArrayDeque<V> deque = new ArrayDeque<>();
		Queue<V> todo = ( Search.DFS == method
				? Collections.asLifoQueue( deque )
				: deque );

		DelegateTree<V, E> tree = new DelegateTree<>();
		tree.setRoot( root );
		todo.add( root );

		// avoid cycles in the graph
		Set<E> edgesToSkip = new HashSet<>();

		while ( !todo.isEmpty() ) {
			V v = todo.poll();

			// once we visit a node, we can never end
			// up there again, or we're not acyclic
			edgesToSkip.addAll( graph.getInEdges( v ) );

			Set<E> outgoings = new HashSet<>( graph.getOutEdges( v ) );
			outgoings.removeAll( edgesToSkip );

			for ( E e : outgoings ) {
				V child = graph.getOpposite( v, e );

				if ( tree.containsVertex( child ) ) {
					if ( throwExceptions ) {
						throw new TreeConversionException();
					}
				}
				else {
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

	public static class TreeConversionException extends Exception {

		public TreeConversionException() {
			super( "Tree conversion failed (too many parents)" );
		}
	}
}
