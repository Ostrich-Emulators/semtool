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

import java.awt.event.ActionEvent;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.om.TreeGraphDataModel;
import gov.va.semoss.ui.components.playsheets.TreeGraphPlaySheet;
import gov.va.semoss.util.Utility;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Controls converting the graph to a tree layout.
 */
public class TreeConverterListener extends AbstractAction {
	
	private GraphPlaySheet gps;
	
	public TreeConverterListener() {
		super( "Convert to Tree", Utility.loadImageIcon( "tree.png" ) );
		
		putValue( Action.SHORT_DESCRIPTION,
				"<html><b>Convert to Tree</b><br>Convert graph to tree by"
				+ " duplicating nodes with multiple in-edges (in new tab)</html>" );
	}

	/**
	 * Method setPlaySheet. Sets the play sheet that the listener will access.
	 *
	 * @param ps GraphPlaySheet
	 */
	public void setPlaySheet( GraphPlaySheet ps ) {
		gps = ps;
		setEnabled( false );
		
		gps.getView().getPickedVertexState().addItemListener( new ItemListener() {
			
			@Override
			public void itemStateChanged( ItemEvent e ) {
				Collection<? extends SEMOSSVertex> picks
						= gps.getView().getPickedVertexState().getPicked();
				
				if ( !isEnabled() ) {
					setEnabled( !picks.isEmpty() );
				}
			}
		} );
	}
	
	@Override
	public void actionPerformed( ActionEvent e ) {
		TreeGraphDataModel model = new TreeGraphDataModel( gps.getGraphData().getGraph(),
				gps.getView().getPickedVertexState().getPicked() );
		TreeGraphPlaySheet tgps = new TreeGraphPlaySheet( model );
		tgps.setTitle( "Tree Conversion" );
		gps.addSibling( tgps );
		tgps.fireGraphUpdated();
	}
}
