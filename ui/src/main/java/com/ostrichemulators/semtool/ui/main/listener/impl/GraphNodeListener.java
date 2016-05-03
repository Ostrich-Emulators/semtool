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
package com.ostrichemulators.semtool.ui.main.listener.impl;

import com.ostrichemulators.semtool.om.GraphElement;
import java.awt.event.MouseEvent;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;
import com.ostrichemulators.semtool.ui.components.GraphNodePopup;
import com.ostrichemulators.semtool.ui.components.NodePropertiesPopup;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.SemossGraphVisualization;
import com.ostrichemulators.semtool.ui.components.playsheets.TreeGraphPlaySheet;

import java.util.Arrays;
import java.util.HashSet;
import javax.swing.SwingUtilities;

/**
 * Controls what happens when a user clicks on a node in a graph.
 */
public class GraphNodeListener extends ModalLensGraphMouse {

	private static final Logger logger = Logger.getLogger( GraphNodeListener.class );
	private final GraphPlaySheet gps;
	private final boolean forTree;

	public GraphNodeListener( GraphPlaySheet _gps ) {
		gps = _gps;
		forTree = false;
	}

	public GraphNodeListener( TreeGraphPlaySheet _gps ) {
		gps = _gps;
		forTree = true;
	}

	/**
	 * Method mousePressed. Controls what happens when the mouse is pressed.
	 *
	 * @param e MouseEvent
	 */
	@Override
	public void mousePressed( MouseEvent e ) {
		super.mousePressed( e );

		if ( !( e.getSource() instanceof VisualizationViewer<?, ?> ) ) {
			logger.warn( "Unknown mouse event type: " + e.getSource() );
			return;
		}

		SemossGraphVisualization viewer = gps.getView();
		GraphElement ele = getClickedElement( viewer, e.getX(), e.getY() );
		
		if( null == ele ){
			// clicked in space, so unhighlight everything
			viewer.clearHighlighting();
			viewer.setSkeletonMode( false );
		}

		if ( ele != null ) {
			checkForDoubleClick( viewer, ele, e );
		}

		if ( SwingUtilities.isRightMouseButton( e ) ) {
			handleRightClick( viewer, ele, e );
		}
	}

	long lastTimeClicked = 0;

	private GraphElement getClickedElement( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			double x, double y ) {
		GraphElementAccessor<SEMOSSVertex, SEMOSSEdge> pickSupport
				= viewer.getPickSupport();

		SEMOSSVertex vert = pickSupport.getVertex( viewer.getGraphLayout(), x, y );
		if ( null != vert ) {
			return vert;
		}

		SEMOSSEdge edge = pickSupport.getEdge( viewer.getGraphLayout(), x, y );
		if ( null != edge ) {
			return edge;
		}

		return null;
	}

	private void checkForDoubleClick( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			GraphElement clickedVertex, MouseEvent e ) {

		long thisTimeClicked = System.currentTimeMillis();
		if ( ( thisTimeClicked - lastTimeClicked ) < 250 ) {
			new NodePropertiesPopup( gps, Arrays.asList( clickedVertex ) ).showPropertiesView();
		}

		lastTimeClicked = thisTimeClicked;
	}

	/*
	 * Method handleRightClick Gather highlights vertices and clicked vertex and
	 * send to right click menu
	 * @param viewer The viewer to use to get the edges
	 * @param clickedVertex The vertex which was clicked
	 * @param e
	 * @return all the currently selected nodes
	 */
	protected Set<SEMOSSVertex> handleRightClick(
			VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			GraphElement ele, MouseEvent e ) {
		logger.debug( "The user right clicked." );

		Set<SEMOSSVertex> vertHash = new HashSet<>();

		Set<SEMOSSVertex> pickedVertices = viewer.getPickedVertexState().getPicked();
		vertHash.addAll( pickedVertices );

		SEMOSSVertex[] vertices
				= pickedVertices.toArray( new SEMOSSVertex[pickedVertices.size()] );

		// for now...
		new GraphNodePopup( gps, ele, vertices, forTree ).show( e );

		return vertHash;
	}
}
