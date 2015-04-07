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

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import javax.swing.AbstractAction;

/**
 * Changes the mouse based on what is selected.
 */
public class MousePickingPopupMenuListener extends AbstractAction {

	private VisualizationViewer vv;

	public MousePickingPopupMenuListener( VisualizationViewer vv ) {
		super( "Pick Graph" );
		this.vv = vv;
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		( (ModalGraphMouse) vv.getGraphMouse() ).setMode( Mode.PICKING );
	}
}
