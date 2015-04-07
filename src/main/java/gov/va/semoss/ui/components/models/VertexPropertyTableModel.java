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

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.VertexFilterData;

/**
 * This class is used to create a table model for vertex properties.
 */
public class VertexPropertyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1980815818428292267L;
	private VertexFilterData data;
	private SEMOSSVertex vertex;

	/**
	 * Constructor for VertexPropertyTableModel.
	 * 
	 * @param _data
	 *            VertexFilterData
	 * @param _vertex
	 *            DBCMVertex
	 */
	public VertexPropertyTableModel(VertexFilterData _data, SEMOSSVertex _vertex) {
		data = _data;
		vertex = _vertex;
	}

	/**
	 * Returns the column count.
	 * 
	 * @return int Column count.
	 */
	@Override
	public int getColumnCount() {
		return data.getPropertyNames().length;
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
	 * 
	 * @return String Column name.
	 */
	public String getColumnName(int index) {
		return data.getPropertyNames()[index];
	}

	/**
	 * Returns the row count.
	 * 
	 * @return int Row count.
	 */
	@Override
	public int getRowCount() {
		return data.getPropertyNumRows(vertex);
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
	public Object getValueAt(int row, int column) {
		return data.getPropValueAt(vertex, row, column);
	}

	/**
	 * Sets the cell value at a particular row and column index.
	 * 
	 * @param val
	 *            Cell value.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 */
	public void setValueAt(Object val, int row, int column) {
		data.setPropValueAt(vertex, val + "", row, column);
		fireTableDataChanged();
	}

	/**
	 * Gets the column class at a particular index.
	 * 
	 * @param column
	 *            Column index.
	 * 
	 * @return Class Column class.
	 */
	public Class<?> getColumnClass(int column) {
		return data.getPropertyClassNames()[column];
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
		if (column == 1)
			return true;
		return false;
	}
}
