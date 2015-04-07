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

import gov.va.semoss.ui.components.models.LabelTableModel;
import gov.va.semoss.ui.components.models.TooltipTableModel;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Controls the play sheet.
 */
public class PlaySheetControlListener implements InternalFrameListener {
	private final GraphPlaySheet ps;

	public PlaySheetControlListener( GraphPlaySheet gps ) {
		ps = gps;
	}

	/**
	 * TODO unused method Method internalFrameActivated.
	 *
	 * @param e InternalFrameEvent
	 */
	@Override
	public void internalFrameActivated( InternalFrameEvent e ) {
		Utility.addModelToJTable( new LabelTableModel(   ps.getControlData()), Constants.LABEL_TABLE);
		Utility.addModelToJTable( new TooltipTableModel( ps.getControlData()), Constants.TOOLTIP_TABLE);
	}

	/**
	 * Clears out the Label and tooltip tables when the frame is closed.
	 *
	 * @param e InternalFrameEvent
	 */
	@Override
	public void internalFrameClosed( InternalFrameEvent e ) {
		Utility.resetJTable(Constants.LABEL_TABLE);
		Utility.resetJTable(Constants.TOOLTIP_TABLE);
	}

	@Override
	public void internalFrameOpened( InternalFrameEvent e ) {}

	@Override
	public void internalFrameClosing( InternalFrameEvent e ) {}

	@Override
	public void internalFrameIconified( InternalFrameEvent e ) {}

	@Override
	public void internalFrameDeiconified( InternalFrameEvent e ) {}

	@Override
	public void internalFrameDeactivated( InternalFrameEvent e ) {}
}