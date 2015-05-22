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
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.om.SEMOSSVertex;
import java.awt.event.ActionEvent;

import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import javax.swing.AbstractAction;

/**
 * Controls the un-hiding of the vertex pop up menu.
 */
public class UnHideVertexPopupMenuListener extends AbstractAction {

	GraphPlaySheet ps;

	public UnHideVertexPopupMenuListener( GraphPlaySheet gps ) {
		super( "Unhide Nodes" );
		ps = gps;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		for ( SEMOSSVertex v : ps.getGraphData().getGraph().getVertices() ) {
			v.setVisible( true );
		}
		ps.getFilterData().unfilterAll();
		ps.updateLayout();
	}
}
