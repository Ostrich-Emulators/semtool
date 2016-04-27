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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import java.awt.event.ActionEvent;

import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import javax.swing.AbstractAction;

/**
 * Controls the un-hiding of the vertex pop up menu.
 */
public class UnHideVertexPopupMenuListener extends AbstractAction {
	private static final long serialVersionUID = 2098465418944831050L;	
	private final GraphPlaySheet gps;

	public UnHideVertexPopupMenuListener( GraphPlaySheet gps ) {
		super( "Unhide Nodes" );
		this.gps = gps;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		Graph<SEMOSSVertex, SEMOSSEdge> realg = gps.getGraphData().getGraph();

		for ( SEMOSSVertex v : realg.getVertices() ) {
			if( !v.isVisible() ){
				v.setVisible( true );
			}
		}
	}
}
