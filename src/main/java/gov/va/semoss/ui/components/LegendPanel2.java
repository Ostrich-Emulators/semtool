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
package gov.va.semoss.ui.components;

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.DIHelper;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.openrdf.model.URI;

/**
 * This class is used to create the legend for visualizations.
 */
public class LegendPanel2 extends JPanel {
	private static final long serialVersionUID = -2364666196260002413L;
	private ControlData controlData;

	/**
	 * Create the panel.
	 */
	public LegendPanel2() {
		setLayout( new WrapLayout( WrapLayout.LEFT, 15, 15 ) );
		setToolTipText( "You can adjust the shape and color by going to the cosmetics tab on the navigation panel" );
		
		controlData = new ControlData();
		controlData.setEngine( DIHelper.getInstance().getRdfEngine() );
	}

	/**
	 * This method will draw the legend for visualizations.
	 */
	public void drawLegend(VertexFilterData data) {
		removeAll();
		
		for ( Map.Entry<URI, List<SEMOSSVertex>> entry : data.getNodeTypeMap().entrySet() ) {
			String label = controlData.getLabel( entry.getKey() );
			List<SEMOSSVertex> vertexList = entry.getValue();
			SEMOSSVertex vertex = vertexList.get( 0 );

			String text = label + " (" + vertexList.size() + ")";
			add( new PaintLabel( text, vertex.getShapeLegend(), vertex.getColor() ) );
		}
		
		updateUI();
		repaint();
	}
}