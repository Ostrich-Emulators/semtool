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

import edu.uci.ics.jung.graph.DirectedGraph;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import gov.va.semoss.ui.components.GraphToTreeConverter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import edu.uci.ics.jung.graph.Forest;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.util.Utility;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.log4j.Logger;

/**
 * Controls converting the graph to a tree layout.
 */
public class TreeConverterListener extends AbstractAction {

	private GraphPlaySheet gps;
	private DirectedGraph<SEMOSSVertex, SEMOSSEdge> oldgraph;

	public TreeConverterListener() {
		super( "Convert to Tree", Utility.loadImageIcon( "tree.png" ) );

		putValue( Action.SHORT_DESCRIPTION,
				"<html><b>Convert to Tree</b><br>Convert current graph to tree by"
				+ " duplicating nodes with multiple in-edges</html>" );
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
				Collection<SEMOSSVertex> picks
						= gps.getView().getPickedVertexState().getPicked();

				setEnabled( !picks.isEmpty() );
			}
		} );
	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		JToggleButton button = (JToggleButton) e.getSource();

		//if the button is selected run converter
		if ( button.isSelected() ) {
			oldgraph = gps.getGraphData().getGraph();

			Collection<SEMOSSVertex> nodes
					= gps.getView().getPickedVertexState().getPicked();
			if ( nodes.isEmpty() ) {
				nodes = gps.getVisibleGraph().getVertices();
			}

			Forest<SEMOSSVertex, SEMOSSEdge> newforest
					= GraphToTreeConverter.convert( gps.getVisibleGraph(), nodes );
			gps.setForest( newforest );
		}
		//if the button is unselected, revert to old forest
		else {
			gps.getGraphData().setGraph( oldgraph );
		}

		Logger.getLogger( getClass() ).warn( "this function probably doesn't work anymore" );
		boolean success = true; //playSheet.createLayout();
		if ( !success ) {
			int response = showOptionPopup();
			if ( response == 1 ) {
				gps.setLayoutName( Constants.FR );
			}
		}

		gps.updateGraph(); // totally unnecessary, I think

	}

	/**
	 * Method showOptionPopup.
	 *
	 * @return int
	 */
	private int showOptionPopup() {
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp( Constants.MAIN_FRAME );
		Object[] buttons = { "Cancel Graph Modification", "Continue With " + Constants.FR };
		int response = JOptionPane.showOptionDialog( playPane, "This layout requires the graph to be in the format of a tree.\nWould you like to revert the layout to " + Constants.FR + "?",
				"Convert to Tree", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[1] );
		return response;
	}
}
