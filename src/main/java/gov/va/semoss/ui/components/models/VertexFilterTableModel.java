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

import javax.swing.table.AbstractTableModel;
import gov.va.semoss.ui.components.VertexFilterData;

/**
 * This class is used to create a table model for the vertex filter.
 */
public class VertexFilterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 6010606033514579342L;
	private VertexFilterData data;

	/**
	 * Constructor for VertexFilterTableModel.
	 * 
	 * @param _data
	 *            VertexFilterData
	 */
	public VertexFilterTableModel(VertexFilterData _data) {
		data = _data;
	}

	/**
	 * Returns the column count.
	 * 
	 * @return int Column count.
	 */
	@Override
	public int getColumnCount() {
		return data.getColumnCount();
	}

	/**
	 * Sets the vertex filter data.
	 * 
	 * @param data
	 *            VertexFilterData
	 */
	public void setVertexFilterData(VertexFilterData data) {
		this.data = data;
	}

	/**
	 * Gets the column name at a particular index.
	 * 
	 * @param index
	 *            Column index.
	 * @return String Column name.
	 */
	@Override
	public String getColumnName(int index) {
		return data.getColumnName( index );
	}

	/**
	 * Returns the row count.
	 * 
	 * @return int Row count.
	 */
	@Override
	public int getRowCount() {
		return data.getRowCount();
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
		return data.getValueAt(arg0, arg1);
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
		return data.getColumnClass( column );
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
	@Override
	public boolean isCellEditable(int row, int column) {
		return (0 == column);
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
	@Override
	public void setValueAt(Object value, int row, int column) {
		data.setValueAt(value, row, column);
		data.fireTableCellUpdated( row, column );
	}
}