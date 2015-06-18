/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.api;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;

/**
 *
 * @author ryan
 */
public interface GraphListener {

	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps );

	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout );

}
