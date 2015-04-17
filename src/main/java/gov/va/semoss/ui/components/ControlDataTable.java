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

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.PropComparator;

/**
 * This class is used to keep track of specific properties for a table.
 */
public class ControlDataTable {
	private static Logger logger = Logger.getLogger(ControlDataTable.class);
	
	private Object[][] rows = new Object[0][5];
	private Class<?>[] rowClasses = {String.class, String.class, Boolean.class, Boolean.class, String.class };
	private int rowCount = 0;
	
	private Hashtable<String, ArrayList<String>> properties = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> propertyShow    = new Hashtable<String, String>();
	private Hashtable<String, String> propertyShowTT  = new Hashtable<String, String>();
	private Hashtable<String, String> propertyHide    = new Hashtable<String, String>();

	private Hashtable<String, ArrayList<String>> labelSelectedList = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> labelUnselectedList = new Hashtable<String, String>();
	private Hashtable<String, ArrayList<String>> tooltipSelectedList = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> tooltipUnselectedList = new Hashtable<String, String>();

	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;
	private ControlDataTableModel tableModel;
	
	public ControlDataTable(Hashtable<String, String> _propertyShow, Hashtable<String, String> _propertyShowTT, Hashtable<String, String> _propertyHide, String[] columnNames) {
		propertyShow = _propertyShow;
		propertyShowTT = _propertyShowTT;
		propertyHide = _propertyHide;
		tableModel = new ControlDataTableModel( columnNames );
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
		
		if (!propertyListByType.contains(property) && !propertyHide.containsKey(property)) {
			propertyListByType.add(property);
			rowCount++;
		}
		
		properties.put(type, propertyListByType);
	}
	
	/**
	 * Generates all the rows in the control panel for the specified table and properties
	 */
	public void populateAllRows() {
		populateFirstRow();

		ArrayList<String> types = new ArrayList<String>(properties.keySet());
		Collections.sort(types);
		
		int rowIndex = 1;
		for (String type:types) {
			
			ArrayList<String> propertiesForThisType = properties.get(type);
			Collections.sort(propertiesForThisType, new PropComparator());
			
			boolean firstRow = true;
			for (String property:propertiesForThisType) {
				if (propertyHide.containsKey(property)) 
					continue;

				populateRow(rowIndex, type, property, firstRow);
				
				logger.debug("Adding Row-- " + rowIndex + "<>" + type + "<>" + property);
				firstRow = false;
				rowIndex++;
			}
		}
		
		tableModel.fireTableDataChanged();
	}
	
	/**
	 * Populates the first row.
	 * columns are: Type, Property, Boolean
	 * 
	 * @param rowCount: total number of rows
	 */
	public void populateFirstRow() {
		rows = new Object[rowCount + 1][5];
		rows[0][0] = "SELECT ALL";
		rows[0][1] = "";
		rows[0][2] = new Boolean(true);
		rows[0][3] = new Boolean(true);
		rows[0][4] = "SELECT ALL";
	}

	/**
	 * Populates one row.
	 * columns are: Type, Property, Boolean
	 * 
	 * @param rowIndex: the row number
	 * @param type: the type of the row
	 * @param property: the property to be shown or hidden
	 * @param firstRow: 
	 */
	public void populateRow(int rowIndex, String type, String property, boolean firstRow) {
		rows[rowIndex][0] = "";
		rows[rowIndex][1] = property;
		rows[rowIndex][2] = isSelected(labelSelectedList, type, property);
		rows[rowIndex][3] = isSelected(tooltipSelectedList, type, property);
		rows[rowIndex][4] = type;

		if (firstRow)
			rows[rowIndex][0] = type;

		if (propertyShow.containsKey(property) && !labelUnselectedList.containsKey(type))
			setValue(new Boolean(true), rowIndex, 2);

		if (propertyShowTT.containsKey(property) && !tooltipUnselectedList.containsKey(type))
			setValue(new Boolean(true), rowIndex, 3);
	}

	private Boolean isSelected(Hashtable<String, ArrayList<String>> selectedList, String type, String property) {
		if (!selectedList.containsKey(type)) 
			return false;
		
		for (String thisProp:selectedList.get(type)) {
			if (thisProp != null && thisProp.equalsIgnoreCase(property))
				return true;
		}

		return false;
	}

	/**
	 * Sets value at a particular row and column location.
	 * 
	 * @param val
	 *            Label value.
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 */
	public void setValue(Object val, int row, int column) {
		rows[row][column] = val;
		
		if (column == 2) {
			setValue(labelSelectedList, labelUnselectedList, val, row, column);
		} else if (column == 3) {
			setValue(tooltipSelectedList, tooltipUnselectedList, val, row, column);
		}
	}


	/**
	 * Sets value at a particular row and column location.
	 * 
	 * @param val
	 *            Label value.
	 * @param row
	 *            Row number.
	 * @param column
	 *            Column number.
	 */
	public void setValue(Hashtable<String, ArrayList<String>> selectedList, Hashtable<String, String> unselectedList, Object valueObject, int row, int column) {
		if (!(valueObject instanceof Boolean))
			return;
		Boolean valBoolean = (Boolean) valueObject;
		
		if (row == 0) {
			// if it is the header row--select all
			for (int i=1; i<rows.length; i++)
				setValue(selectedList, unselectedList, valBoolean, i, column);
		}
		
		String type = rows[row][4] + "";
		String property = rows[row][1] + "";
		
		ArrayList<String> typePropList = selectedList.get(type);
		if (typePropList == null)
			typePropList = new ArrayList<String>();
		
		if (valBoolean) {
			if (!typePropList.contains(property)) {
				typePropList.add(property);
				unselectedList.remove(type, type);
			}
		} else {
			typePropList.remove(property);
			unselectedList.put(type, type);
		}
		
		rows[row][column] = valBoolean;
		tableModel.fireTableCellUpdated(row, column);
		selectedList.put(type, typePropList);
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
	public Object getCell(int row, int column) {
		if (row >= rows.length || column >= rows[0].length)
			return null;
		
		return rows[row][column];
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
		return labelSelectedList.get(type);
	}
	
	/**
	 * Gets tooltip properties of a specific type.
	 * 
	 * @param type
	 *            Type of property to retrieve.
	 * 
	 * @return Vector<String> List of properties.
	 */
	public ArrayList<String> getSelectedPropertiesTT(String type) {
		return tooltipSelectedList.get(type);
	}
	
	public ControlDataTableModel getTableModel() {
		return tableModel;
	}
	
	public void setViewer(VisualizationViewer<SEMOSSVertex, SEMOSSEdge> _viewer) {
		viewer = _viewer;
	}
	
	public class ControlDataTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 502758220766389041L;
		private String[] columnNames;

		
		public ControlDataTableModel(String[] _columnNames) {
			columnNames = _columnNames;
		}
		
		/**
		 * Returns the column count.
		 * 
		 * @return int Column count.
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		/**
		 * Gets the column name at a particular index.
		 * 
		 * @param index
		 *            Column index.
		 * 
		 * @return String Column name.
		 */
		@Override
		public String getColumnName(int index) {
			return columnNames[index];
		}

		/**
		 * Returns the row count.
		 * 
		 * @return int Row count.
		 */
		@Override
		public int getRowCount() {
			return rows.length;
		}

		/**
		 * Gets the cell value at a particular row and column index.
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
			return getCell(row, column);
		}

		/**
		 * Gets the column class at a particular index.
		 * 
		 * @param column
		 *            Column index.
		 * 
		 * @return Class Column class.
		 */
		@Override
		public Class<?> getColumnClass(int column) {
			return rowClasses[column];
		}

		/**
		 * Checks whether the cell at a particular row and column index is editable.
		 * 
		 * @param row
		 *            Row index.
		 * @param column
		 *            Column index.
		 * 
		 * @return boolean True if cell is editable.
		 */
		public boolean isCellEditable(int row, int column) {
			if (column == 2 || column == 3)
				return true;

			return false;
		}

		/**
		 * Sets the label value at a particular row and column index.
		 * 
		 * @param value
		 *            Label value.
		 * @param row
		 *            Row index.
		 * @param column
		 *            Column index.
		 */
		public void setValueAt(Object value, int row, int column) {
			setValue(value, row, column);
			fireTableDataChanged();
			
			if (viewer != null)
				viewer.repaint();
		}
	}
}