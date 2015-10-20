/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.graph.functions;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ryan
 */
public class IslandProcessor<V, E> {

	private final Graph<V, E> graph;

	public IslandProcessor( Graph<V, E> g ) {
		graph = g;
	}

	/**
	 * Gets the island containing the given node
	 *
	 * @param node
	 * @return
	 */
	public Graph<V, E> getIsland( V node ) {
		// just do a depth-first search of everything this node connects to
		SparseGraph<V, E> island = new SparseGraph<>();
		Deque<V> todo = new ArrayDeque<>();
		todo.add( node );

		while ( !todo.isEmpty() ) {
			V vert = todo.pop();
			island.addVertex( vert );

			for ( E ed : graph.getIncidentEdges( vert ) ) {
				if ( !island.containsEdge( ed ) ) {
					V v2 = graph.getOpposite( vert, ed );
					if ( !island.containsVertex( v2 ) ) {
						todo.push( v2 );
					}

					island.addEdge( ed, vert, v2 );
				}
			}
		}

		return island;
	}

	public Set<Graph<V, E>> getIslands() {
		Map<Set<V>, Graph<V, E>> islands = new HashMap<>();

		for ( V v : graph.getVertices() ) {
			Graph<V, E> island = getIsland( v, graph );
			Set<V> islandverts = new HashSet<>( island.getVertices() );

			if ( !islands.containsKey( islandverts ) ) {
				islands.put( islandverts, island );
			}
		}

		return new HashSet<>( islands.values() );
	}

	public Set<Graph<V, E>> getIslands( Collection<V> selectedVerts ) {
		Map<Set<V>, Graph<V, E>> islands = new HashMap<>();

		for ( V v : selectedVerts ) {
			Graph<V, E> island = getIsland( v );
			Set<V> islandverts = new HashSet<>( island.getVertices() );

			if ( !islands.containsKey( islandverts ) ) {
				islands.put( islandverts, island );
			}
		}

		return new HashSet<>( islands.values() );
	}

	/**
	 * Gets the island in the given graph that contains the given node.
	 *
	 * @param <V>
	 * @param <E>
	 * @param node the node whose island to find
	 * @param graph the graph containing the node
	 * @return the island containing the node
	 */
	public static <V, E> Graph<V, E> getIsland( V node, Graph<V, E> graph ) {
		return new IslandProcessor( graph ).getIsland( node );
	}
}
