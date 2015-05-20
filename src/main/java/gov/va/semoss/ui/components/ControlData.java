/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.components;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This class is used to keep track of all the properties within the tool.
 */
public class ControlData {
	private ControlDataTable vertexCDT, edgeCDT;
	
	/**
	 * Constructor for ControlData.
	 */
	public ControlData() {
		// put what we want to show first in all of these things
		Hashtable<String, String> propertyHide  = new Hashtable<String, String>();
		propertyHide.put(Constants.VERTEX_COLOR, Constants.VERTEX_COLOR);
		
		Hashtable<String, String> propertyShow  = new Hashtable<String, String>();
		propertyShow.put(Constants.VERTEX_NAME.stringValue(), Constants.VERTEX_NAME.stringValue());

		Hashtable<String, String> propertyShowTT = new Hashtable<String, String>();
		propertyShowTT.put(Constants.EDGE_NAME.stringValue(), Constants.EDGE_NAME.stringValue());
		propertyShowTT.put(Constants.EDGE_TYPE.stringValue(), Constants.EDGE_TYPE.stringValue());
		propertyShowTT.put(Constants.URI_KEY.stringValue(), Constants.URI_KEY.stringValue());
		propertyShowTT.put(Constants.VERTEX_NAME.stringValue(), Constants.VERTEX_NAME.stringValue());
		propertyShowTT.put(Constants.VERTEX_TYPE.stringValue(), Constants.VERTEX_TYPE.stringValue());
		
		vertexCDT = new ControlDataTable(propertyShow, propertyShowTT, propertyHide, new String[] {"Vertex Type", "Property", "Label", "Tooltip" });
		edgeCDT   = new ControlDataTable(propertyShow, propertyShowTT, propertyHide, new String[] {"Edge Type", "Property", "Label", "Tooltip" });
	}

	/**
	 * Generates all the rows in the control panel.
	 */
	public void generateAllRows() {
		vertexCDT.populateAllRows();
		edgeCDT.populateAllRows();
	}
	
	/**
	 * Adds a property of a specific type to the edge ControlDataTable.
	 * 
	 * @param type
	 *            Type of property.
	 * @param property
	 *            Property.
	 */
	public void addEdgeProperty(String type, String property) {
		edgeCDT.addProperty(type, property);
	}
	
	/**
	 * Adds a property of a specific type to the vertex ControlDataTable.
	 * 
	 * @param type
	 *            Type of property.
	 * @param property
	 *            Property.
	 */
	public void addVertexProperty(String type, String property) {
		vertexCDT.addProperty(type, property);
	}

	/**
	 * Gets properties of a specific type.
	 * 
	 * @param type
	 *            Type of property to retrieve.
	 * 
	 * @return Vector<String> List of properties.
	 */
	public ArrayList<String> getSelectedProperties(String type) {
		ArrayList<String> vertexProperties = vertexCDT.getSelectedProperties(type);
		ArrayList<String> edgeProperties = edgeCDT.getSelectedProperties(type);
		
		if (vertexProperties == null && edgeProperties == null)
			return new ArrayList<>();
		
		if (vertexProperties == null || vertexProperties.isEmpty())
			return edgeProperties;
		
		if (edgeProperties == null || edgeProperties.isEmpty())
			return vertexProperties;
		
		vertexProperties.addAll(edgeProperties);
		
		return vertexProperties;
	}

	/**
	 * Gets all of the tooltip specific properties.
	 * 
	 * @param type
	 *            String Type of property to retrieve.
	 * 
	 * @return Vector<String> List of properties.
	 */
	public ArrayList<String> getSelectedPropertiesTT(String type) {
		ArrayList<String> vertexProperties = vertexCDT.getSelectedPropertiesTT(type);
		ArrayList<String> edgeProperties = edgeCDT.getSelectedPropertiesTT(type);
		
		if (vertexProperties == null && edgeProperties == null)
			return new ArrayList<String>();
		
		if (vertexProperties == null || vertexProperties.size() == 0)
			return edgeProperties;
		
		if (edgeProperties == null || edgeProperties.size() == 0)
			return vertexProperties;
		
		vertexProperties.addAll(edgeProperties);
		
		return vertexProperties;
	}
	
	public void setViewer(VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer) {
		vertexCDT.setViewer(viewer);
		edgeCDT.setViewer(viewer);
	}

	public ControlDataTable.ControlDataTableModel getVertexTableModel() {
		return vertexCDT.getTableModel();
	}
	
	public ControlDataTable.ControlDataTableModel getEdgeTableModel() {
		return edgeCDT.getTableModel();
	}
}