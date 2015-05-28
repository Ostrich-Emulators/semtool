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

import gov.va.semoss.om.SEMOSSVertex;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;

/**
 * This class is used to create a table model for vertex properties.
 */
public class VertexPropertyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1980815818428292267L;

	private static final String[] columnNames = { "Name ", "Value" };
	private static final Class<?>[] classNames = { Object.class, Object.class };
	private ArrayList<PropertyRow> rows = new ArrayList<PropertyRow>();
	private SEMOSSVertex vertex;
	
	/**
	 * Constructor for VertexPropertyTableModel.
	 * 
	 * @param vertex
	 *            SEMOSSVertex
	 */
	public VertexPropertyTableModel(SEMOSSVertex vertex) {
		this.vertex = vertex;
		for ( Map.Entry<URI, Object> entry : vertex.getProperties().entrySet() )
			rows.add( new PropertyRow(entry.getKey(), entry.getValue()));
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
		PropertyRow pRow = rows.get(row);
		switch ( column ) {
			case 0: {
				return pRow.name.getLocalName();
			} case 1: { 
				return pRow.value;
			} default:
				return null;
		}
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
		PropertyRow pRow = rows.get(row);
			switch ( column ) {
			case 0: {
				pRow.name = (URI) val;
				break;
			} case 1: { 
				pRow.value = val;
				break;
			}
		}
		
		vertex.setProperty(pRow.name, pRow.value);
		//JPM 2015/05/27 is the intention here to save this back to the db?
		fireTableDataChanged();
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
		return rows.size();
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
		return classNames[column];
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

	public class PropertyRow {
		URI name;
		Object value;

		public PropertyRow( URI name, Object value ) {
			this.name = name;
			this.value = value;
		}
	}
}