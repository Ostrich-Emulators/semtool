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
package com.ostrichemulators.semtool.ui.components.playsheets.graphsupport;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.ActionEvent;

import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Changes mouse based on what is selected.
 */
public class MouseTransformPickPopupMenuListener extends AbstractAction {

	private final VisualizationViewer ps;
	private final Mode mode;

	/**
	 * Method setPlaysheet. Sets the playsheet that the listener will access.
	 *
	 * @param ps IPlaySheet
	 */
	public MouseTransformPickPopupMenuListener( VisualizationViewer ps, Mode m ) {
		super( ( Mode.TRANSFORMING == m ? "Move" : "Pick" ) + " Graph" );

		String desc = ( Mode.TRANSFORMING == m
				? "Move entire graph as a single unit" : "Pick specific nodes" );
		putValue( Action.SHORT_DESCRIPTION, desc );
		this.ps = ps;
		mode = m;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		( (ModalGraphMouse) ps.getGraphMouse() ).setMode( mode );
	}

}
