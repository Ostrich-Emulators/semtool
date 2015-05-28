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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.models.VertexPropertyTableModel;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.transformer.LabelFontTransformer;
import gov.va.semoss.ui.transformer.PaintTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

/**
 * Controls what happens when a picked state occurs.
 */
public class PickedStateListener implements ItemListener {
	private static final Logger logger = Logger.getLogger( PickedStateListener.class );
	private final VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;
	private final GraphPlaySheet gps;

	public PickedStateListener( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> v, GraphPlaySheet ps ) {
		viewer = v;
		gps = ps;
	}

	/**
	 * Method itemStateChanged. Changes the state of the graph play sheet.
	 *
	 * @param e ItemEvent
	 */
	@Override
	public void itemStateChanged( ItemEvent e ) {
		logger.debug( "PickedStateListener.itemStateChanged(e) called for click event: " + e.getSource() );

		JTable table = (JTable) DIHelper.getInstance().getLocalProp( Constants.PROP_TABLE );
		table.setModel( new DefaultTableModel() );

		//need to check if there are any size resets that need to be done
		VertexShapeTransformer vst 
				= (VertexShapeTransformer) viewer.getRenderContext().getVertexShapeTransformer();
		vst.emptySelected();

		//Need vertex to highlight when click in skeleton mode... Here we need to get the already selected vertices
		//so that we can add to them
		Set<SEMOSSVertex> vertHash = new HashSet<>();
		LabelFontTransformer<SEMOSSVertex> vlft = null;
		if ( gps.getSearchPanel().isHighlightButtonSelected() ) {
			vlft = (LabelFontTransformer<SEMOSSVertex>) viewer.getRenderContext().getVertexFontTransformer();
			vertHash.addAll( vlft.getSelected() );
		}
		
		vertHash.addAll( viewer.getPickedVertexState().getPicked() );

		for ( SEMOSSVertex vertex : viewer.getPickedVertexState().getPicked() ){
			logger.debug( "Selecting vertex with label >>> " + vertex.getLabel() );
			vst.setSelected( vertex );

			VertexPropertyTableModel pm = new VertexPropertyTableModel( vertex );
			table.setModel( pm );
			pm.fireTableDataChanged();
		}
		
		if ( null != vlft ){
			vlft.setSelected( vertHash );
			PaintTransformer<SEMOSSVertex> ptx = (PaintTransformer<SEMOSSVertex>) viewer.getRenderContext().getVertexFillPaintTransformer();
			ptx.setSelected( vertHash );
		}
	}
}
