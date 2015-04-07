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
 *****************************************************************************
 */
package gov.va.semoss.ui.main.listener.impl;

import gov.va.semoss.ui.components.models.PropertyTableModel;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Controls the OWL for a play sheet. Allows setting up of properties and
 * relationships.
 */
public class PlaySheetOWLListener implements InternalFrameListener {
	private final GraphPlaySheet ps;
	
	public PlaySheetOWLListener( GraphPlaySheet gps ){
		ps = gps;
	}
	
	@Override
	public void internalFrameActivated( InternalFrameEvent e ) {
		if ( !ps.getSudowl() )
			return;
		
//		Utility.addModelToJTable(new OPropertyTableModel( ps.getPredicateData()), Constants.OBJECT_PROP_TABLE);
		Utility.addModelToJTable( new PropertyTableModel( ps.getPredicateData()), Constants.DATA_PROP_TABLE);
	}

	@Override
	public void internalFrameClosed( InternalFrameEvent e ) {
		Utility.resetJTable(Constants.OBJECT_PROP_TABLE);
		Utility.resetJTable(Constants.DATA_PROP_TABLE);
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