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
package com.ostrichemulators.semtool.ui.main.listener.impl;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.transformer.VertexShapeTransformer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Controls the re-sizing of nodes in the graph play sheet.
 */
public class GraphVertexSizeListener extends AbstractAction implements ActionListener {

	private static final long serialVersionUID = 5827085455653750499L;

	private VertexShapeTransformer transformerV;
	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;

	/**
	 * Method setViewer. Sets the viewer that the listener will access.
	 *
	 * @param v VisualizationViewer
	 */
	public void setViewer( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> v ) {
		viewer = v;
		transformerV = (VertexShapeTransformer) viewer.getRenderContext().getVertexShapeTransformer();
	}

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param e ActionEvent - The event that triggers the actions in the method.
	 */
	@Override
	public void actionPerformed( ActionEvent e ) {
		boolean increase = true;
		if ( ( (JButton) e.getSource() ).getName().contains( "Decrease" ) ) {
			increase = false;
		}

		//if no vertices are selected, perform action on all vertices
		if ( viewer.getPickedVertexState().getPicked().isEmpty() ) {
			if ( increase ) {
				transformerV.increaseSize();
			}
			else {
				transformerV.decreaseSize();
			}

			viewer.repaint();
			return;
		}

		//else if vertices have been selected, apply action only to those vertices
		for ( SEMOSSVertex vertex : viewer.getPickedVertexState().getPicked() ) {
			if ( increase ) {
				transformerV.increaseSize( vertex );
			}
			else {
				transformerV.decreaseSize( vertex );
			}
		}

		viewer.repaint();
	}
}
