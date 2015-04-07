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

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.ActionEvent;


import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Changes mouse based on what is selected.
 */
public class MouseTransformPopupMenuListener extends AbstractAction {

	private final VisualizationViewer ps;

	/**
	 * Method setPlaysheet. Sets the playsheet that the listener will access.
	 *
	 * @param ps IPlaySheet
	 */
	public MouseTransformPopupMenuListener( VisualizationViewer ps ) {
		super( "Move Graph" );
		putValue( Action.SHORT_DESCRIPTION, "Move entire graph as a single unit" );
		this.ps = ps;
	}
	
	@Override
	public void actionPerformed( ActionEvent e ) {
		( (ModalGraphMouse) ps.getGraphMouse() ).setMode( Mode.TRANSFORMING );
	}

}
