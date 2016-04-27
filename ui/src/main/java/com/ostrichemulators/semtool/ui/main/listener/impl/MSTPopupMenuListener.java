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
package com.ostrichemulators.semtool.ui.main.listener.impl;

import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;

import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.transformer.EdgeStrokeTransformer;
import com.ostrichemulators.semtool.util.GuiUtility;
import javax.swing.AbstractAction;
import javax.swing.Action;

// implements the minimum spanning tree
/**
 */
public class MSTPopupMenuListener extends AbstractAction {

	private final GraphPlaySheet ps;

	private static final Logger logger = Logger.getLogger( MSTPopupMenuListener.class );

	/**
	 * Method setPlaysheet.
	 *
	 * @param ps IPlaySheet
	 */
	public MSTPopupMenuListener( GraphPlaySheet ps ) {
		super( "Highlight Minimum Spanning Tree" );
		putValue( Action.SHORT_DESCRIPTION,
				"Highlight subgraph that contains all the vertices and is a tree" );
		this.ps = ps;
	}

	/**
	 * Method actionPerformed.
	 *
	 * @param e ActionEvent
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {

		// gets the view from the playsheet
		// gets the jGraphT graph
		// runs the kruskal on it
		// Creates the edges and sets it on the edge painter
		// repaints it
		// I cannot add this to the interface because not all of them will be forced to have it
		// yes, which means the menu cannot be generic too - I understand
		logger.debug( "Getting the base graph" );
		Graph graph = ps.asSimpleGraph();
		KruskalMinimumSpanningTree<SEMOSSVertex, SEMOSSEdge> kmst
				= new KruskalMinimumSpanningTree<>( graph );

		EdgeStrokeTransformer tx = (EdgeStrokeTransformer) ps.getView().getRenderContext()
				.getEdgeStrokeTransformer();
		tx.setSelected( kmst.getEdgeSet() );

		// repaint it
		ps.getView().repaint();
		int originalSize = ps.asForest().getEdgeCount();
		int shortestPathSize = kmst.getEdgeSet().size();
		GuiUtility.showMessage( "Minimum Spanning Tree uses " + shortestPathSize
				+ " edges out of " + originalSize + " original edges" );
	}
}
