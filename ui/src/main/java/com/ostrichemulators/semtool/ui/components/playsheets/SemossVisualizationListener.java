/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.playsheets;

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import edu.uci.ics.jung.graph.Graph;

/**
 *
 * @author ryan
 */
public interface SemossVisualizationListener {

	public void nodesUpdated( Graph<SEMOSSVertex, SEMOSSEdge> graph,
			SemossGraphVisualization viz );
}
