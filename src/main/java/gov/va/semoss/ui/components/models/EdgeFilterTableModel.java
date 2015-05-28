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

import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.ui.components.ControlData;
import gov.va.semoss.ui.components.VertexFilterData;
import gov.va.semoss.util.DIHelper;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;

/**
 * This class is used to create a table model (listeners) for edge filters.
 */
public class EdgeFilterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 53864898863707354L;
	
	private static final String[] columnNames = { "Show", "Relation", "Instance" };
	private static final Class<?>[] classNames = { Boolean.class, Object.class, Object.class };

	private VertexFilterData data;
	private ControlData controlData;

	/**
	 * Constructor for EdgeFilterTableModel.
	 * 
	 * @param data
	 *            VertexFilterData
	 */
	public EdgeFilterTableModel(VertexFilterData _data) {
		data = _data;
		
		controlData = new ControlData();
		controlData.setEngine( DIHelper.getInstance().getRdfEngine() );
		fireTableDataChanged();
	}

	/**
	 * Gets the edge column count from the data.
	 * 
	 * @return int Column count.
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
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
	 * Gets the column name at a particular cell.
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
	 * Gets the row count from the data.
	 * 
	 * @return int Row count.
	 */
	@Override
	public int getRowCount() {
		return data.getEdges().size();
	}

	/**
	 * Gets the class at a particular column.
	 * 
	 * @param column
	 *            Column index.
	 * 
	 * @return Class Column class.
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		return classNames[column];
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
	@Override
	public boolean isCellEditable(int row, int column) {
		return (column == 0);
	}

	/**
	 * Gets the edge value at a particular row and column index.
	 *
	 * @param row Row index.
	 * @param column Column index.
	 *
	 * @return Object Edge value.
	 */
	@Override
	public Object getValueAt( int row, int column ) {
		FilterRowModel thisRow = data.getEdges().get( row );
		switch ( column ) {
			case 0:
				return thisRow.show();
			case 1: {
				if ( thisRow.isHeader() )
					return controlData.getLabel( thisRow.getType() );
				return "";
			} case 2: {
				if ( thisRow.isHeader() )
					return "Select All";
				return controlData.getLabel( thisRow.getInstance() );
			} default:
				return null;
		}
	}

	/**
	 * Sets the edge value at a particular row and column index.
	 *
	 * @param value Edge value, as an object.
	 * @param row Row index.
	 * @param column Column index.
	 */
	@Override
	public void setValueAt( Object value, int row, int column ) {
		FilterRowModel thisRow = data.getEdges().get( row );
		switch ( column ) {
			case 0: {
				thisRow.setShow((Boolean) value);
				break;
			} case 1: { 
				thisRow.setType((URI) value);
				break;
			} case 2: {
				thisRow.setInstance((URI) value);
				break;
			}
		}
		
		if ( thisRow.isHeader() ) {
			List<SEMOSSEdge> edgeList = data.getEdgeTypeMap().get( thisRow.getType() );

			int latest = row + 1;
			for ( SEMOSSEdge edge : edgeList ) {
				FilterRowModel rowOfThisType = data.getEdges().get( latest++ );
				rowOfThisType.setShow( (Boolean) value);
				edge.setVisible( rowOfThisType.show() );
			}

			fireTableRowsUpdated( row, latest );
			return;
		}

		// we are only dealing with one edge
		SEMOSSEdge edge = data.getEdgeMap().get( thisRow.getInstance() );
		edge.setVisible( (Boolean) value );
		fireTableCellUpdated( row, row );
	}
}