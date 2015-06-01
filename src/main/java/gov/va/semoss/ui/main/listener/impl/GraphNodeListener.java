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
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.GraphNodePopup;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.components.models.EdgePropertyTableModel;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.util.HashSet;

/**
 * Controls what happens when a user clicks on a node in a graph.
 */
public class GraphNodeListener extends ModalLensGraphMouse implements IChakraListener {

	private static final Logger logger = Logger.getLogger( GraphNodeListener.class );
	private GraphPlaySheet gps;

	public GraphNodeListener( GraphPlaySheet _gps ) {
		gps = _gps;
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

		if ( clickedVertex == null ) {
			handleEdges( viewer );
		}

		Set<SEMOSSVertex> vertHash = new HashSet<>();
		if ( e.getButton() == MouseEvent.BUTTON3 ) {
			vertHash = handleRightClick( viewer, clickedVertex, e );
		}

		if ( gps.getSearchPanel().isHighlightButtonSelected() ) {
			handleHighlightVertexInSkeletonMode( viewer, vertHash );
		}
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
	 * Method handleEdges only need to work with the edges if an edge was
	 * clicked directly
	 * 
	 * @param VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer - The viewer
	 * to use to get the edges
	 */
	private void handleEdges(
			VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer ) {
		JTable table = (JTable) DIHelper.getInstance().getLocalProp(
				Constants.PROP_TABLE );

		Set<SEMOSSEdge> pickedEdges = viewer.getPickedEdgeState().getPicked();
		for ( SEMOSSEdge edge : pickedEdges ) {
			EdgePropertyTableModel pm = new EdgePropertyTableModel( edge );
			table.setModel( pm );
			pm.fireTableDataChanged();
		}
	}

	/*
	 * Method handleRightClick Gather highlights vertices and clicked vertex and
	 * send to right click menu
	 * 
	 * @param VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer - The viewer
	 * to use to get the edges
	 * 
	 * @param SEMOSSVertex clickedVertex - The vertex which was clicked
	 * 
	 * @param MouseEvent e - the mouse event which we are handling, used as the
	 * display container.
	 */
	private Set<SEMOSSVertex> handleRightClick( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			SEMOSSVertex clickedVertex, MouseEvent e ) {
		logger.debug( "The user right clicked." );

		Set<SEMOSSVertex> vertHash = new HashSet<>();

		Set<SEMOSSVertex> pickedVertices = viewer.getPickedVertexState().getPicked();
		vertHash.addAll( pickedVertices );

		SEMOSSVertex[] vertices = pickedVertices
				.toArray( new SEMOSSVertex[pickedVertices.size()] );
		new GraphNodePopup( gps, clickedVertex, vertices ).show( e );

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

	/**
	 * Method actionPerformed. Dictates what actions to take when an Action Event
	 * is performed.
	 *
	 * @param arg0 ActionEvent - The event that triggers the actions in the
	 * method.
	 */
	@Override
	public void actionPerformed( ActionEvent arg0 ) {
	}

	/**
	 * Method setView. Sets a JComponent that the listener will access and/or
	 * modify when an action event occurs.
	 *
	 * @param view the component that the listener will access
	 */
	@Override
	public void setView( JComponent view ) {
	}
}
