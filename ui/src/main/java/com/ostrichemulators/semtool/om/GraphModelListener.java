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

	public void changed( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			GraphDataModel gdm );
}
