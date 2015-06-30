/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.main.listener.impl.GraphNodeListener;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class GraphMouseAdapter extends GraphNodeListener {

	private static final Logger log = Logger.getLogger( GraphMouseAdapter.class );
	private final GraphicalQueryBuilderPanel panel;

	public GraphMouseAdapter( GraphicalQueryBuilderPanel pnl ) {
		super( null );
		panel = pnl;
	}

	@Override
	protected Set<SEMOSSVertex> handleRightClick( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer,
			SEMOSSVertex clickedVertex, MouseEvent e ) {
		if ( null == clickedVertex ) {
			log.debug( "clicked in space" );
			return new HashSet<>();
		}
		else {
			log.debug( "clicked on a vertex: " + clickedVertex );
			VertexPopup pop = new VertexPopup( clickedVertex, panel );
			pop.show( viewer, e.getX(), e.getY() );

			Set<SEMOSSVertex> pickedVertices = viewer.getPickedVertexState().getPicked();
			return pickedVertices;
		}
	}

	@Override
	protected void handleEdges( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer ) {
		// do nothing (yet) on edges
	}

}
