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
import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.ControlPanel;
import gov.va.semoss.ui.components.models.VertexPropertyTableModel;
import gov.va.semoss.ui.components.GraphPlaySheetFrame;
import gov.va.semoss.ui.transformer.VertexLabelFontTransformer;
import gov.va.semoss.ui.transformer.VertexPaintTransformer;
import gov.va.semoss.ui.transformer.VertexShapeTransformer;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.QuestionPlaySheetStore;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;

import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * Controls what happens when a picked state occurs.
 */
public class PickedStateListener implements ItemListener {

	private static final Logger logger = Logger.getLogger( PickedStateListener.class );
	VisualizationViewer viewer;
	private GraphPlaySheet gps;

	public PickedStateListener( VisualizationViewer v, GraphPlaySheet ps ) {
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
		logger.debug( " Clicked" + e.getSource() );

		JTable table = (JTable) DIHelper.getInstance().getLocalProp( Constants.PROP_TABLE );
		TableModel tm = new DefaultTableModel();
		table.setModel( tm );

		//need to check if there are any size resets that need to be done
		VertexShapeTransformer vst = (VertexShapeTransformer) viewer.getRenderContext().getVertexShapeTransformer();
		vst.emptySelected();

		// handle the vertices
		PickedState<SEMOSSVertex> ps = viewer.getPickedVertexState();
		Iterator<SEMOSSVertex> it = ps.getPicked().iterator();

		SEMOSSVertex[] vertices = new SEMOSSVertex[ps.getPicked().size()];

		//Need vertex to highlight when click in skeleton mode... Here we need to get the already selected vertices
		//so that we can add to them
		Map<String, String> vertHash = new HashMap<>();
		VertexLabelFontTransformer vlft = null;
		if ( gps.getSearchPanel().isHighlightButtonSelected() ) {
			vlft = (VertexLabelFontTransformer) viewer.getRenderContext().getVertexFontTransformer();
			vertHash = vlft.getVertHash();
		}

		for ( int vertIndex = 0; it.hasNext(); vertIndex++ ) {
			SEMOSSVertex v = it.next();
			vertices[vertIndex] = v;
			//add selected vertices
			vertHash.put( v.getProperty( RDF.SUBJECT ).toString(), v.getProperty( RDF.SUBJECT ).toString() );

			logger.debug( " Name  >>> " + v.getProperty( RDFS.LABEL ) );
			vst.setSelected( v.getURI() );
			// this needs to invoke the property table model stuff

			VertexPropertyTableModel pm = new VertexPropertyTableModel( gps.getFilterData(), v );
			table.setModel( pm );
			//table.repaint();
			pm.fireTableDataChanged();
			logger.debug( "Add this in - Prop Table" );
		}
		if ( gps.getSearchPanel().isHighlightButtonSelected() ) {
			vlft.setVertHash( vertHash );
			VertexPaintTransformer ptx = (VertexPaintTransformer) viewer.getRenderContext().getVertexFillPaintTransformer();
			ptx.setVertHash( vertHash );
		}

	}

}
