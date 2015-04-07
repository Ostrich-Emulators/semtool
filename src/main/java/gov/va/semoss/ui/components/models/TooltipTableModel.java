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
package gov.va.semoss.ui.components.models;

import gov.va.semoss.ui.components.ControlData;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

/**
 * This class is used to create a table model for the tooltip table.
 */
public class TooltipTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -9048648520556613766L;

	private ControlData data = null;
	private static final Logger logger = Logger
			.getLogger(TooltipTableModel.class);
	private String[] columnNames = { "Type", "Property", "Select" };

	/**
	 * Constructor for TooltipTableModel.
	 * 
	 * @param data
	 *            ControlData
	 */
	public TooltipTableModel(ControlData data) {
		this.data = data;
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
	 * Sets the control data.
	 * 
	 * @param data
	 *            ControlData
	 */
	public void setControlData(ControlData data) {
		this.data = data;
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
		return data.getRowCount() + 1;
	}

	/**
	 * Gets the cell value at a particular row and column index.
	 * 
	 * @param arg0
	 *            Row index.
	 * @param arg1
	 *            Column index.
	 * 
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt(int arg0, int arg1) {
		return data.getToolTip(arg0, arg1);
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
		if (data.getToolTip(0, column)==null) {
			logger.debug("This cell is null[][][][][][][][][][][][][][][]");
			return String.class;
		}

		Class<?> theClass = data.getToolTip(0, column).getClass();
		logger.debug("Column " + column + " has " + theClass);
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
		logger.debug("Calling the edge filter set value at");
		data.setTooltipValueAt(value, row, column);
		fireTableDataChanged();
	}
}