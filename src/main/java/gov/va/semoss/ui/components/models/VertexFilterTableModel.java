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
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.ui.components.VertexFilterData;
import gov.va.semoss.util.DIHelper;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;

/**
 * This class is used to create a table model for the vertex filter.
 */
public class VertexFilterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 6010606033514579342L;
	private VertexFilterData data;

	private static final String[] columnNames = { "Show", "Node Type", "Instance" };
	private static final Class<?>[] classNames = { Boolean.class, Object.class, Object.class };
	
	private ControlData controlData;

	/**
	 * Constructor for VertexFilterTableModel.
	 * 
	 * @param _data
	 *            VertexFilterData
	 */
	public VertexFilterTableModel(VertexFilterData _data) {
		data = _data;
		
		controlData = new ControlData();
		controlData.setEngine( DIHelper.getInstance().getRdfEngine() );
		fireTableDataChanged();
	}

	/**
	 * Gets value at a particular row and column index.
	 *
	 * @param row Row index.
	 * @param column Column index.
	 *
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt( int row, int column ) {
		FilterRowModel vfRow = data.getNodes().get( row );
		switch ( column ) {
			case 0:
				return vfRow.show();
			case 1: {
				if ( vfRow.isHeader() )
					return vfRow.getType().getLocalName();
				return "";
			} case 2: {
				if ( vfRow.isHeader() )
					return "Select All";
				return controlData.getLabel( vfRow.getInstance() );
			} default:
				return null;
		}
	}

	/**
	 * Sets the cell value at a particular row and column index.
	 *
	 * @param value Cell value.
	 * @param row Row index (int).
	 * @param column Column index (int).
	 */
	@Override
	public void setValueAt( Object value, int row, int column ) {
		FilterRowModel vfRow = data.getNodes().get( row );
		switch ( column ) {
			case 0: {
				vfRow.setShow((Boolean) value);
				break;
			} case 1: { 
				vfRow.setType((URI) value);
				break;
			} case 2: {
				vfRow.setInstance((URI) value);
				break;
			}
		}
		
		if ( vfRow.isHeader() ) {
			List<SEMOSSVertex> vertVector = data.getNodeTypeMap().get( vfRow.getType() );

			int latest = row + 1;
			for ( SEMOSSVertex vertex : vertVector ) {
				FilterRowModel thisVfRow = data.getNodes().get( latest++ );
				thisVfRow.setShow( (Boolean) value);
				vertex.setVisible( thisVfRow.show() );
			}

			fireTableRowsUpdated( row, latest );
			return;
		}

		// we are only dealing with one vertex
		SEMOSSVertex vertex = data.getNodeMap().get( vfRow.getInstance() );
		vertex.setVisible( (Boolean) value );
		fireTableCellUpdated( row, row );
	}

	/**
	 * Returns the row count.
	 * 
	 * @return int Row count.
	 */
	@Override
	public int getRowCount() {
		return data.getNodes().size();
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
	 * Gets the column class at a particular index.
	 * 
	 * @param column
	 *            Column index.
	 * 
	 * @return Class Column class.
	 */
	@Override
	public Class<?> getColumnClass( int columnIndex ) {
		return classNames[columnIndex];
	}

	/**
	 * Gets the column name at a particular index.
	 * 
	 * @param index
	 *            Column index.
	 * @return String Column name.
	 */
	@Override
	public String getColumnName( int column ) {
		return columnNames[column];
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
}