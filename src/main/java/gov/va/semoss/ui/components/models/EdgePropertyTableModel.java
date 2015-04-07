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

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.ui.components.VertexFilterData;

/**
 * This class is used to create a table model for edge properties.
 */
public class EdgePropertyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -4086813413883136585L;
	private VertexFilterData data;
	private SEMOSSEdge edge;

	/**
	 * Constructor for EdgePropertyTableModel.
	 * 
	 * @param data
	 *            VertexFilterData
	 * @param edge
	 *            DBCMEdge
	 */
	public EdgePropertyTableModel(VertexFilterData _data, SEMOSSEdge _edge) {
		data = _data;
		edge = _edge;
	}

	/**
	 * Returns the column count in the model.
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
	 * Gets the property name at a particular index.
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
	 * Returns the number of rows in the model.
	 * 
	 * @return int Number of rows.
	 */
	@Override
	public int getRowCount() {
		return data.getEdgeNumRows(edge);
	}

	/**
	 * Gets the value of a cell at a particular row and column index.
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
		// get the value first
		return data.getPropValueAt(edge, row, column);
	}

	/**
	 * Sets the edge property value at a particular row and column.
	 * 
	 * @param value
	 *            Value to assign to cell.
	 * @param row
	 *            Row that value is assigned to.
	 * @param column
	 *            Column that value is assigned to.
	 */
	public void setValueAt(Object val, int row, int column) {
		data.setPropValueAt(edge, val + "", row, column);
		fireTableDataChanged();
		// sets the value
	}

	/**
	 * Gets the class at a particular column.
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
	 * Checks if the cell at a particular row and column is editable.
	 * 
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return boolean True if the cell is editable.
	 */
	public boolean isCellEditable(int row, int column) {
		if (column == 1)
			return true;
		return false;
	}
}
