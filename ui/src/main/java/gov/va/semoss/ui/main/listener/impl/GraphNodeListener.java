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

import java.awt.event.MouseEvent;
import java.util.Set;


import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.GraphNodePopup;
import gov.va.semoss.ui.components.NodePropertiesPopup;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.components.playsheets.TreeGraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelFontTransformer;

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

		@SuppressWarnings( "unchecked" )
		VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer
				= (VisualizationViewer<SEMOSSVertex, SEMOSSEdge>) e.getSource();

		SEMOSSVertex clickedVertex = checkIfVertexWasClicked( viewer, e.getX(),
				e.getY() );

		if ( clickedVertex != null ) {
			checkForDoubleClick( viewer, clickedVertex, e );
		}

		Set<SEMOSSVertex> vertHash = new HashSet<>();
		if ( SwingUtilities.isRightMouseButton( e ) ) {
			vertHash = handleRightClick( viewer, clickedVertex, e );
		}

		if ( null != gps ) {
			if ( gps.getSearchPanel().isHighlightButtonSelected() ) {
				handleHighlightVertexInSkeletonMode( viewer, vertHash );
			}
		}
	}

	long lastTimeClicked = 0;

	private void checkForDoubleClick( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			SEMOSSVertex clickedVertex, MouseEvent e ) {

		long thisTimeClicked = System.currentTimeMillis();
		if ( ( thisTimeClicked - lastTimeClicked ) < 250 ) {
			new NodePropertiesPopup( gps, viewer.getPickedVertexState().getPicked() ).showPropertiesView();
		}

		lastTimeClicked = thisTimeClicked;
	}

	/*
	 * Method checkIfVertexWasClicked Check to see if an edge or a vertex was
	 * selected so we know which to show in the property table model
	 * 
	 * @param VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer - The viewer
	 * to use to get the vertex
	 */
	private SEMOSSVertex checkIfVertexWasClicked(
			VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer, int x, int y ) {

		GraphElementAccessor<SEMOSSVertex, SEMOSSEdge> pickSupport = viewer
				.getPickSupport();
		Object clickedObject = pickSupport.getVertex( viewer.getGraphLayout(),
				x, y );
		if ( clickedObject instanceof SEMOSSVertex ) {
			logger.debug( "The user clicked a SEMOSSVertex." );
			return (SEMOSSVertex) clickedObject;
		}

		return null;
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
			SEMOSSVertex clickedVertex, MouseEvent e ) {
		logger.debug( "The user right clicked." );

		Set<SEMOSSVertex> vertHash = new HashSet<>();

		Set<SEMOSSVertex> pickedVertices = viewer.getPickedVertexState().getPicked();
		vertHash.addAll( pickedVertices );

		SEMOSSVertex[] vertices
				= pickedVertices.toArray( new SEMOSSVertex[pickedVertices.size()] );
		new GraphNodePopup( gps, clickedVertex, vertices, forTree ).show( e );

		return vertHash;
	}

	/*
	 * Method handleHighlightVertexInSkeletonMode Need vertex to highlight when
	 * click in skeleton mode. Here we need to get the already selected vertices
	 * so that we can add to them.
	 * 
	 * @param VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer - The viewer
	 * to use to get the edges
	 * 
	 * @param Map<String, String> vertHash - The vertixes which were highlighted
	 */
	private void handleHighlightVertexInSkeletonMode(
			VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			Set<SEMOSSVertex> verts ) {

		LabelFontTransformer<SEMOSSVertex> vlft = gps.getVertexLabelFontTransformer();
		vlft.select( verts );
		viewer.repaint();
	}
}
