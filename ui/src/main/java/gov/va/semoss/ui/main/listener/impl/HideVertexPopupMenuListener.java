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
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;

/**
 * Controls hiding the pop up menu for nodes on the graph play sheet.
 */
public class HideVertexPopupMenuListener extends AbstractAction {

	private static final long serialVersionUID = -2864866456286018607L;

	private final List<SEMOSSVertex> highlightedVertices = new ArrayList<>();

	public HideVertexPopupMenuListener( Collection<SEMOSSVertex> highlights ) {
		super( "Hide Nodes" );
		highlightedVertices.addAll( highlights );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		for ( SEMOSSVertex vertex : highlightedVertices ) {
			vertex.setVisible( false );
		}
	}
}
