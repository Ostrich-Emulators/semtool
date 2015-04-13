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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.PropComparator;

/**
 * This class is used to keep track of all the properties within the tool.
 */
public class ControlData {
	private static Logger logger = Logger.getLogger(ControlData.class);
	
	private ControlDataTable labels, tooltips;
	private Hashtable<String, ArrayList<String>> properties = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> propHide = new Hashtable<String, String>();
	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;
	private int rowCount = 0;
	
	/**
	 * Constructor for ControlData.
	 */
	public ControlData() {
		// put what we want to show first in all of these things
		Hashtable<String, String> propOn  = new Hashtable<String, String>();
		propOn.put(Constants.VERTEX_NAME, Constants.VERTEX_NAME);
		labels = new ControlDataTable(propOn, new String[] {"Type", "Property", "Show" });

		Hashtable<String, String> propOnT = new Hashtable<String, String>();
		propOnT.put(Constants.EDGE_NAME, Constants.EDGE_NAME);
		propOnT.put(Constants.EDGE_TYPE, Constants.EDGE_TYPE);
		propOnT.put(Constants.URI_KEY, Constants.URI_KEY);
		propOnT.put(Constants.VERTEX_NAME, Constants.VERTEX_NAME);
		propOnT.put(Constants.VERTEX_TYPE, Constants.VERTEX_TYPE);
		tooltips = new ControlDataTable(propOnT, new String[] {"Type", "Property", "Show" });
		
		propHide.put(Constants.VERTEX_COLOR, Constants.VERTEX_COLOR);
	}

	/**
	 * Generates all the rows in the control panel.
	 */
	public void generateAllRows() {
		labels.populateFirstRow(rowCount);
		tooltips.populateFirstRow(rowCount);

		ArrayList<String> types = new ArrayList<String>(properties.keySet());
		Collections.sort(types);
		
		int rowIndex = 1;
		for (String type:types) {
			
			ArrayList<String> propertiesForThisType = properties.get(type);
			Collections.sort(propertiesForThisType, new PropComparator());
			
			boolean firstRow = true;
			for (String property:propertiesForThisType) {
				if (propHide.containsKey(property)) 
					continue;

				labels.populateRow(rowIndex, type, property, firstRow);
				tooltips.populateRow(rowIndex, type, property, firstRow);
				
				logger.debug("Adding Rows -- " + rowIndex + "<>" + type + "<>" + property);
				firstRow = false;
				rowIndex++;
			}
		}
	}
	
	/**
	 * Sets label value at a particular row and column location.
	 * 
	 * @param val
	 *            Label value.
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 */
	public void setLabelValueAt(Object val, int row, int column) {
		labels.setValue(val, row, column);
		viewer.repaint();
	}

	/**
	 * Sets tooltip value at a particular row and column location.
	 * 
	 * @param val
	 *            Tooltip value.
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 */
	public void setTooltipValueAt(Object val, int row, int column) {
		tooltips.setValue(val, row, column);
		viewer.repaint();
	}
	
	/**
	 * Adds a property of a specific type to the property hashtable.
	 * 
	 * @param type
	 *            Type of property.
	 * @param property
	 *            Property.
	 */
	public void addProperty(String type, String property) {
		ArrayList<String> propertyListByType = properties.get(type);
		if (propertyListByType == null)
			propertyListByType = new ArrayList<String>();
		
		if (!propertyListByType.contains(property) && !propHide.containsKey(property)) {
			propertyListByType.add(property);
			rowCount++;
		}
		
		properties.put(type, propertyListByType);
	}

	public int getRowCount() {
		return rowCount;
	}

	/**
	 * Gets label value from a particular row and column location.
	 * 
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 * 
	 * @return Object Label value.
	 */
	public Object getLabel(int row, int column) {
		return labels.getCell(row, column);
	}

	/**
	 * Gets tooltip value at a particular row and column location.
	 * 
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 * 
	 * @return Object Tooltip value.
	 */
	public Object getToolTip(int row, int column) {
		return tooltips.getCell(row, column);
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
		return labels.getSelectedProperties(type);
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
		return tooltips.getSelectedProperties(type);
	}
	
	public void setViewer(VisualizationViewer<SEMOSSVertex, SEMOSSEdge> _viewer) {
		viewer = _viewer;
		
		labels.setViewer(viewer);
		tooltips.setViewer(viewer);
	}

	public ControlDataTable getLabels() {
		return labels;
	}
	
	public ControlDataTable getTooltips() {
		return tooltips;
	}
}