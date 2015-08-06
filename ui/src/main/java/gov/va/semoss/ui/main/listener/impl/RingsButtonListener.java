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
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;

import gov.va.semoss.ui.transformer.BalloonLayoutRings;
import gov.va.semoss.ui.transformer.RadialTreeLayoutRings;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

/**
 * Controls the rendering of rings on a graph if the layout is a balloon or a
 * radial tree.
 */
public class RingsButtonListener implements ActionListener {

	private final BalloonLayoutRings rings = new BalloonLayoutRings();
	private final RadialTreeLayoutRings treeRings = new RadialTreeLayoutRings();
	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view;
	private Layout lay;

	/**
	 * Method setViewer. Sets the view that the listener will access.
	 *
	 * @param view VisualizationViewer
	 */
	public void setViewer( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> view ) {
		this.view = view;
		this.rings.setViewer( view );
		this.treeRings.setViewer( view );
	}

	/**
	 * Method setGraph. Sets the graph that the listener will access.
	 *
	 * @param forest Forest
	 */
	public void setGraph( Forest<SEMOSSVertex, SEMOSSEdge> forest ) {
		treeRings.setForest( forest );
	}

	/**
	 * Method setLayout. Sets the layout that the listener will access.
	 *
	 * @param lay Layout
	 */
	public void setLayout( Layout lay ) {
		this.lay = lay;
		if ( lay instanceof BalloonLayout ) {
			this.rings.setLayout( BalloonLayout.class.cast( lay ) );
		}
		else if ( lay instanceof RadialTreeLayout ) {
			this.treeRings.setLayout( RadialTreeLayout.class.cast( lay ) );
		}
	}

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param e ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		// Get if the button is selected
		JToggleButton button = JToggleButton.class.cast( e.getSource() );

		if ( !button.isSelected() ) {
			if ( lay instanceof BalloonLayout ) {
				view.removePreRenderPaintable( rings );
			}
			else if ( lay instanceof RadialTreeLayout ) {
				view.removePreRenderPaintable( treeRings );
			}
		}
		else {
			if ( lay instanceof BalloonLayout ) {
				view.addPreRenderPaintable( rings );
			}
			else if ( lay instanceof RadialTreeLayout ) {
				view.addPreRenderPaintable( treeRings );
			}
		}
		view.repaint();

	}

}
