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
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.playsheets.PlaySheetCentralComponent;
import com.ostrichemulators.semtool.ui.components.playsheets.PropertyEditorPlaySheet;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * This class is used to display information about a node in a popup window.
 */
public class NodePropertiesPopup extends AbstractAction {

	private static final long serialVersionUID = -1859278887122010885L;
	private final PlaySheetCentralComponent gps;
	private final Collection<SEMOSSVertex> pickedVertexList;

	public NodePropertiesPopup( PlaySheetCentralComponent _gps,
			Collection<SEMOSSVertex> _pickedVertexList ) {
		super( "Edit Properties for Node(s)" );
		putValue( Action.SHORT_DESCRIPTION, "Edit the properties of this node" );
		gps = _gps;

		pickedVertexList = _pickedVertexList;
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		showPropertiesView();
	}

	public void showPropertiesView() {
		gps.addSibling( new PropertyEditorPlaySheet( pickedVertexList,
				"Selected Node Properties", gps.getEngine() ) );
	}
}
