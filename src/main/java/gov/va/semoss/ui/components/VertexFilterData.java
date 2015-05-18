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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import javax.swing.table.AbstractTableModel;

/**
 * This class is used to filter vertex data.
 */
public class VertexFilterData extends AbstractTableModel {
	private static final long serialVersionUID = 5877171695108692808L;
	private static final Logger logger = Logger.getLogger(VertexFilterData.class);

	private final Map<String, List<SEMOSSVertex>> typeHash = new HashMap<>();
	private final Map<String, List<SEMOSSEdge>> edgeTypeHash = new HashMap<>();
	private static final String[] columnNames = { "Show", "Node", "Instance" };
	private static final Class<?>[] classNames = { Boolean.class, Object.class, Object.class };

	private String[] edgeTypeNames = { "Edge Type", "Filter" };
	private String[] propertyNames = { "Name ", "Value" };
	private String[] nodeTypes;
	private String[] edgeTypes;

	// keeps the DBCM vertex to properties data
	private final Map<SEMOSSVertex, String[][]> propHash = new HashMap<>();

	// keeps the DBCM Edge to properties data
	private final Map<SEMOSSEdge, String[][]> edgeHash = new HashMap<>();

	// object because some of this can be double
	private Class<?>[] propClassNames = { Object.class, Object.class };

	// all the nodes that need to be filtered
	private final Map<String, String> filterNodes = new HashMap<>();
	private final Map<String, String> edgeFilterNodes = new HashMap<>();

	// these ensure you are not adding the same vertex more than once
	private final Map<String, SEMOSSVertex> checker = new HashMap<>();
	private final Map<String, SEMOSSEdge> edgeChecker = new HashMap<>();

	// Table rows for vertices
	private String[][] rows = null;
	// table rows for edges
	private Object[][] edgeRows = null;
	// table row for edge types and sliders
	private Object[][] edgeTypeRows = null;

	// count of number of vertices
	private int count = 0;
	private int edgeCount = 0;

	/**
	 * Gets value at a particular row and column index.
	 * 
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt(int row, int column) {
		String val = rows[row][column];
		if (val != null && val.equalsIgnoreCase("true")) {
			return true;
		} else if (val != null && val.equalsIgnoreCase("false")) {
			return false;
		} else {
			return val;
		}
	}

	public String[] getEdgeTypeNames() {
		return edgeTypeNames;
	}

	/**
	 * Gets the edge types.
	 * 
	 * @return String[] List of edge types.
	 */
	public String[] getEdgeTypes() {
		return edgeTypes;
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}

	public Class<?>[] getPropertyClassNames() {
		return propClassNames;
	}

	public int getEdgeCount() {
		return edgeCount;
	}

	public Object getEdgeVal( int r, int c ) {
		if ( edgeRows == null || 0 == edgeRows.length ) {
			return null;
		}

		return edgeRows[r][c];
	}

	/**
	 * Gets the node types.
	 * 
	 * @return String[] List of node types.
	 */
	public String[] getNodeTypes() {
		return nodeTypes;
	}

	/**
	 * Given a node type, returns the associated nodes.
	 * 
	 * @param nodeType
	 *            Node type, as a string.
	 *
	 * @return List of nodes (DBCM vertexes).
	 */
	public List<SEMOSSVertex> getNodes(String nodeType) {
		return typeHash.get(nodeType);
	}

	public Map<String, String> getFilterNodes() {
		return filterNodes;
	}

	public Map<String, String> getEdgeFilterNodes() {
		return edgeFilterNodes;
	}

	/**
	 * Adds vertex if the hashtable does not already contain it as a key.
	 * 
	 * @param vert
	 *            DBCMVertex
	 */
	public void addVertex(SEMOSSVertex vert) {
		if (checker.containsKey(vert.getURI() + ""))
			return;

		String vertType = vert.getType();
		List<SEMOSSVertex> typeVector;
		if (typeHash.containsKey(vertType))
			typeVector = typeHash.get(vertType);
		else {
			typeVector = new ArrayList<>();
			count++;
		}
		
		typeVector.add(vert);
		typeHash.put(vertType, typeVector);
		checker.put(vert.getURI(), vert);
		count++;
	}

	/**
	 * Adds edge if the hashtable does not already contain it as a key.
	 * 
	 * @param edge
	 *            DBCMEdge
	 */
	public void addEdge(SEMOSSEdge edge) {
		if (edgeChecker.containsKey(edge.getURI() + "")) 
			return;
		
		String edgeType = (String) edge.getProperty(Constants.EDGE_TYPE);
		List<SEMOSSEdge> typeVector;
		if (edgeTypeHash.containsKey(edgeType))
			typeVector = edgeTypeHash.get(edgeType);
		else {
			typeVector = new ArrayList<>();
			edgeCount++;
		}
		
		typeVector.add(edge);
		edgeTypeHash.put(edgeType, typeVector);
		edgeChecker.put(edge.getURI(), edge);
		edgeCount++;
	}

	/**
	 * Gets the number of rows.
	 * 
	 * @return int Number of rows.
	 */
	@Override
	public int getRowCount() {
		return count;
	}
	
	@Override
	public int getColumnCount(){
		return columnNames.length;
	}

	@Override
	public Class<?> getColumnClass( int columnIndex ) {
		return classNames[columnIndex];
	}

	@Override
	public String getColumnName( int column ) {
		return columnNames[column];
	}

	/**
	 * Fills the rows based on the vertex name and type.
	 * 
	 * @return String[][] Array containing information about vertex properties.
	 */
	public String[][] fillRows() {
		Map<String, Boolean> oldshowvals = new HashMap<>();
		if (null != rows) {
			for (Object[] oldvals : rows) {
				String key = (null == oldvals[1] ? oldvals[2].toString()
						: oldvals[1] + "-Select All");
				oldshowvals.put(key,
						Boolean.parseBoolean(oldvals[0].toString()));
			}
		}

		rows = new String[count][4];
		nodeTypes = new String[typeHash.size()];

		int rowCount = 0;
		int keyCount = 0;
		for (Map.Entry<String, List<SEMOSSVertex>> en : typeHash.entrySet()) {
			String vertType = en.getKey();
			nodeTypes[keyCount] = vertType;
			List<SEMOSSVertex> vertVector = en.getValue();

			boolean firstVertexOfThisType = true;
			for (SEMOSSVertex vert : vertVector) {
				String vertName = vert.getLabel();

				if (firstVertexOfThisType) {
					String key = vertType + "-Select All";
					rows[rowCount][0] = (oldshowvals.containsKey(key) ? oldshowvals
							.get(key) : true) + "";
					rows[rowCount][1] = vertType;
					rows[rowCount][2] = "Select All";
					rowCount++;
					firstVertexOfThisType = false;
				}

				rows[rowCount][0] = (oldshowvals.containsKey(vertName) ? oldshowvals
						.get(vertName) : true) + "";
				rows[rowCount][2] = vertName;
				rows[rowCount][3] = vert.getURI();
				rowCount++;
			}
			keyCount++;
		}

		return rows;
	}

	/**
	 * Sets the cell value at a particular row and column index.
	 * 
	 * @param value
	 *            Cell value.
	 * @param row
	 *            Row index (int).
	 * @param column
	 *            Column index (int).
	 */
	@Override
	public void setValueAt(Object value, int row, int column) {
		rows[row][column] = value + "";
		
		String nodeType = rows[row][1];
		if (nodeType != null && nodeType.length() > 0) {
			// this is a header row, so set all rows of this type to value
			List<SEMOSSVertex> vertVector = typeHash.get(nodeType);

			int vertIndex = 0;
			int first = Integer.MAX_VALUE;
			int last = Integer.MIN_VALUE;
			for (SEMOSSVertex vert : vertVector) {
				int idx = row + (vertIndex++) + 1;
				rows[idx][0] = value + "";
				if ((Boolean) value) {
					filterNodes.remove(vert.getURI());
				} else {
					filterNodes.put(vert.getURI(), vert.getLabel());
				}
				
				if( idx < first ){
					first = idx;
				}
				if( idx > last ){
					last = idx;
				}
			}
			
			fireTableRowsUpdated( first, last );			
			
			return;
		}

		// we are only dealing with one vertex
		String vertName = rows[row][2];
		String vertURI = rows[row][3];
		SEMOSSVertex vert = checker.get(vertURI);
		if (vert == null) 
			return;
		
		if ((Boolean) value)
			filterNodes.remove(vert.getURI());
		else
			filterNodes.put(vert.getURI(), vertName);
		
		this.fireTableCellUpdated( row, row );
	}

	/**
	 * Fills the rows based on the edge name and type.
	 * 
	 * @return String[][] Array containing information about edge properties.
	 */
	public String[][] fillEdgeRows() {
		Map<String, Boolean> oldshowvals = new HashMap<>();
		if (null != edgeRows) {
			for (Object[] oldvals : edgeRows) {
				String key = (null == oldvals[1] ? oldvals[2].toString()
						: oldvals[1] + "-Select All");
				oldshowvals.put(key,
						Boolean.parseBoolean(oldvals[0].toString()));
			}
		}

		edgeTypes = new String[edgeTypeHash.size()];
		edgeTypeRows = new Object[edgeTypeHash.size()][2];
		edgeRows = new Object[edgeCount][5];

		int rowCount = 0;
		int keyCount = 0;
		for (Map.Entry<String, List<SEMOSSEdge>> en : edgeTypeHash.entrySet()) {
			String edgeType = en.getKey();
			edgeTypes[keyCount] = edgeType;
			edgeTypeRows[keyCount][0] = edgeType;
			edgeTypeRows[keyCount][1] = 100d;

			List<SEMOSSEdge> edgeVector = en.getValue();

			boolean firstEdgeOfThisType = true;
			for (SEMOSSEdge edge : edgeVector) {
				String edgeName = edge.getName();

				logger.debug("Adding edge with details of " + edgeType + "<>"
						+ edgeName + "<> 0.0");
				if (firstEdgeOfThisType) {
					String key = edgeType + "-Select All";
					edgeRows[rowCount][0] = (oldshowvals.containsKey(key) ? oldshowvals
							.get(key) : true);
					edgeRows[rowCount][1] = edgeType;
					edgeRows[rowCount][2] = "Select All";

					rowCount++;
					firstEdgeOfThisType = false;
				}

				edgeRows[rowCount][0] = (oldshowvals.containsKey(edgeName) ? oldshowvals
						.get(edgeName) : true);
				edgeRows[rowCount][2] = edgeName;
				edgeRows[rowCount][3] = 0.0d;
				edgeRows[rowCount][4] = edge.getURI();
				rowCount++;
			}
			
			keyCount++;
		}

		return rows;
	}

	/**
	 * Gets the edge value at a particular row and column index.
	 * 
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return Object Edge value.
	 */
	public Object getEdgeValueAt(int row, int column) {
		return edgeRows[row][column];
	}

	/**
	 * Sets the edge value at a particular row and column index.
	 * 
	 * @param value
	 *            Edge value, as an object.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 */
	public void setEdgeValueAt(Object value, int row, int column) {
		edgeRows[row][column] = value;
		if (column != 0)
			return;
		
		String edgeType = (String) edgeRows[row][1];
		if (edgeType != null && edgeType.length() > 0) {
			// this is a header row, so set all edge of this type to value
			int edgeIndex = 0;
			for (SEMOSSEdge edge : edgeTypeHash.get(edgeType)) {
				edgeRows[row + (edgeIndex++) + 1][0] = value;
				if (((Boolean) value)) {
					edgeFilterNodes.remove(edge.getURI());
				} else {
					edgeFilterNodes.put(edge.getURI(), edge.getName());
				}
			}
			
			return;
		}
		
		// we are only dealing with one edge
		String edgeName = edgeRows[row][2] + "";
		String edgeURI = edgeRows[row][4] + "";
		SEMOSSEdge edge = edgeChecker.get(edgeURI);
		
		if (edge == null || !(value instanceof Boolean)) 
			return;
		
		if ((Boolean) value)
			edgeFilterNodes.remove(edge.getURI());
		else
			edgeFilterNodes.put(edge.getURI(), edgeName);
	}

	/**
	 * Slider that adjusts the edge weight at a particular row and column index.
	 * 
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return Object Edge weight.
	 */
	public Object getEdgeAdjustValueAt(int row, int column) {
		Object retVal = edgeTypeRows[row][column];
		logger.debug(row + "<>" + column + "<>" + retVal);
		return retVal;
	}

	/**
	 * Sets the value for edge adjustment at a particular row and column index.
	 * 
	 * @param val
	 *            Value for edge adjustment.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 */
	public void setEdgeAdjustValueAt(Object val, int row, int column) {
		edgeTypeRows[row][column] = val;
	}

	/**
	 * Gets the number of rows for the number of properties.
	 * 
	 * @param vertex
	 *            DBCM vertex that is used to get properties.
	 * 
	 * @return Number of properties for a certain vertex.
	 */
	public int getPropertyNumRows(SEMOSSVertex vertex) {
		if (!propHash.containsKey(vertex)) {
			fillPropRows(vertex);
		}
		return vertex.getProperties().size();
	}

	/**
	 * Fills the rows for properties given a particular node.
	 * 
	 * @param vert
	 *            DBCMVertex
	 */
	public void fillPropRows(SEMOSSVertex vert) {
		Map<String, Object> propH = vert.getProperties();
		String[][] propertyRows = new String[propH.size()][2];

		logger.debug(" Filling Property for vertex " + propH.size());
		int keyCount = 0;
		for (Map.Entry<String, Object> en : propH.entrySet()) {
			String key = en.getKey();
			Object value = en.getValue();

			propertyRows[keyCount][0] = key;
			propertyRows[keyCount][1] = value.toString();

			keyCount++;
		}
		this.propHash.put(vert, propertyRows);

	}

	/**
	 * Gets the property value for a given node at a particular row and column
	 * index.
	 * 
	 * @param vert
	 *            SEMOSSVertex Node.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return Object Property value.
	 */
	public Object getPropValueAt(SEMOSSVertex vert, int row, int column) {
		String[][] propertyRows = propHash.get(vert);
		String val = propertyRows[row][column];
		logger.debug(row + "<>" + column + "<>" + val);
		return val;
	}

	/**
	 * Sets the property value for a given node at a particular row and column
	 * index.
	 * 
	 * @param vert
	 *            DBCMVertex Node.
	 * @param val
	 *            Value to be set, in string form.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 */
	public void setPropValueAt(SEMOSSVertex vert, String val, int row,
			int column) {
		String[][] propertyRows = propHash.get(vert);
		String key = propertyRows[row][0];
		propertyRows[row][column] = val;
		propHash.put(vert, propertyRows);
		vert.putProperty(key, val + "");
	}

	/**
	 * Given a particular edge, returns the number of properties associated with
	 * it.
	 * 
	 * @param edge
	 *            SEMOSSEdge
	 * 
	 * @return int Number of properties associated with the edge.
	 */
	public int getEdgeNumRows(SEMOSSEdge edge) {
		if (!edgeHash.containsKey(edge))
			fillEdgeRows(edge);
		return edge.getProperties().size();
	}

	/**
	 * Fills rows of edges with properties given a particular edge.
	 * 
	 * @param edge
	 *            SEMOSSEdge
	 */
	public void fillEdgeRows(SEMOSSEdge edge) {
		Map<String, Object> propHash = edge.getProperties();
		logger.debug("fillEdgeRows(edge) Number of Properties: "
				+ propHash.size());

		int keyCount = 0;
		String[][] propertyRows = new String[propHash.size()][2];
		for (Map.Entry<String, Object> entry : propHash.entrySet()) {
			propertyRows[keyCount][0] = entry.getKey();
			propertyRows[keyCount][1] = entry.getValue().toString();
			keyCount++;
		}

		edgeHash.put(edge, propertyRows);
	}

	/**
	 * Gets the property value for a given edge at a particular row and column
	 * index.
	 * 
	 * @param edge
	 *            DBCMEdge Edge.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return Object Property value.
	 */
	public Object getPropValueAt(SEMOSSEdge edge, int row, int column) {
		String[][] propertyRows = edgeHash.get(edge);
		String val = propertyRows[row][column];
		logger.debug(row + "<>" + column + "<>" + val);
		return val;
	}

	/**
	 * Gets the property value for a given edge at a particular row and column
	 * index.
	 * 
	 * @param edge
	 *            DBCMEdge Edge.
	 * @param val
	 *            Property value, as a string.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 */
	public void setPropValueAt(SEMOSSEdge edge, String val, int row, int column) {
		String[][] propertyRows = edgeHash.get(edge);
		String key = propertyRows[row][0];
		propertyRows[row][column] = val;
		edgeHash.put(edge, propertyRows);
		edge.putProperty(key, val + "");
	}

	/**
	 * Gets the edge types.
	 * 
	 * @return Hashtable of edge types of the form <String, Vector>
	 */
	public Map<String, List<SEMOSSEdge>> getEdgeTypeHash() {
		return this.edgeTypeHash;
	}

	/**
	 * Returns the types in a hashtable.
	 * 
	 * @return Hashtable of the form <String, Vector>
	 */
	public Map<String, List<SEMOSSVertex>> getTypeHash() {
		return this.typeHash;
	}

	/**
	 * Gets the edges given an edge type.
	 * 
	 * @param edgeType
	 *            String
	 * 
	 * @return List of edges.
	 */
	public List<SEMOSSEdge> getEdges(String edgeType) {
		return this.edgeTypeHash.get(edgeType);
	}

	// add node to filter so that it will be filtered
	/**
	 * Adds node to filter.
	 * 
	 * @param vertex
	 *            DBCMVertex
	 */
	public void addNodeToFilter(SEMOSSVertex vertex) {
		filterNodes.put(vertex.getURI(), vertex.getLabel());
	}

	/**
	 * Unfilters all nodes.
	 */
	public void unfilterAll() {
		filterNodes.clear();
	}
}
