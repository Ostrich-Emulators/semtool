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

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;

/**
 * This class is used to keep track of specific properties for a table.
 */
public class ControlDataTable {
	private Object[][] rows = new Object[0][4];
	private Hashtable<String, ArrayList<String>> selectedList = new Hashtable<String, ArrayList<String>>();
	private Hashtable<String, String> unselectedList = new Hashtable<String, String>();
	private Hashtable<String, String> propertyOn  = new Hashtable<String, String>();

	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;
	private ControlDataTableModel tableModel;
	
	public ControlDataTable(Hashtable<String, String> _propertyOn, String[] columnNames) {
		propertyOn = _propertyOn;
		tableModel = new ControlDataTableModel( columnNames );
	}

	/**
	 * Populates the first row.
	 * columns are: Type, Property, Boolean
	 * 
	 * @param rowCount: total number of rows
	 */
	public void populateFirstRow(int rowCount) {
		rows = new Object[rowCount + 1][4];
		rows[0][0] = "SELECT ALL";
		rows[0][1] = "";
		rows[0][2] = new Boolean(true);
		rows[0][3] = "SELECT ALL";
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
		rows[rowIndex][2] = isSelected(type, property);
		rows[rowIndex][3] = type;

		if (firstRow)
			rows[rowIndex][0] = type;

		if (propertyOn.containsKey(property) && !unselectedList.containsKey(type))
			setValue(new Boolean(true), rowIndex, 2);
	}

	/**
	 * Checks if property for a certain type is selected.
	 * 
	 * @param selectedList
	 *            List of properties.
	 * @param type
	 *            Property type.
	 * @param property
	 *            Property.
	 * 
	 * @return boolean True if a property is selected.
	 */
	private Boolean isSelected(String type, String property) {
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
		if (row == 0) {
			// if it is the header row--select all
			for (int i=1; i<rows.length; i++)
				setValue(val, i, 2);
		}
		
		String type = rows[row][3] + "";
		ArrayList<String> typePropList = new ArrayList<String>();
		if (selectedList.containsKey(type))
			typePropList = selectedList.get(type);
		
		if (val instanceof Boolean) {
			if ((Boolean) val)
				typePropList.add(rows[row][1] + "");
			else {
				typePropList.remove(rows[row][1] + "");
				unselectedList.put(type, type);
			}
		}
		
		rows[row][column] = val;
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
		return selectedList.get(type);
	}
	
	public ControlDataTableModel getTableModel() {
		return tableModel;
	}
	
	public void setViewer(VisualizationViewer<SEMOSSVertex, SEMOSSEdge> _viewer) {
		viewer = _viewer;
	}
	
	class ControlDataTableModel extends AbstractTableModel {
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
			if (getCell(0, column)==null) {
				return String.class;
			}

			Class<?> theClass = getCell(0, column).getClass();
			return theClass;
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
			if (column == 2)
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
			viewer.repaint();
		}
	}
}