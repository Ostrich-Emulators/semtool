/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 *
 * @author ryan
 */
public interface GraphModelListener {

	/**
	 * Notifies that the given graph changed at the given level
	 *
	 * @param graph the graph
	 * @param level the level of changes (undefined: -1)
	 * @param gdm the source of the change
	 */
	public void changed( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, int level,
			GraphDataModel gdm );
}
