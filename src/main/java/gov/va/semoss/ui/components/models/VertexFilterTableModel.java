/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.models;

import edu.uci.ics.jung.graph.Graph;
import gov.va.semoss.om.NodeEdgeBase;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;

import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Map;
import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;

/**
 * This class is used to create a table model for the vertex filter.
 */
public class VertexFilterTableModel<T extends NodeEdgeBase> extends AbstractTableModel {

	private static final long serialVersionUID = 6010606033514579342L;
	private static final Class<?>[] classNames = { Boolean.class, URI.class, URI.class };

	private final String[] columnNames = { "Show", "Node Type", "Instance" };
	private final Graph<SEMOSSVertex, SEMOSSEdge> graph;
	private final List<FilterRow> data = new ArrayList<>();

	/**
	 * Constructor for VertexFilterTableModel.
	 *
	 * @param _data VertexFilterData
	 */
	public VertexFilterTableModel( Graph<SEMOSSVertex, SEMOSSEdge> graph,
			Collection<T> instances, String middleColumnName ) {
		this.graph = graph;

		columnNames[1] = middleColumnName;

		MultiMap<URI, T> typeToInstances = new MultiMap<>();
		for ( T t : instances ) {
			typeToInstances.add( t.getType(), t );
		}
		for ( Map.Entry<URI, List<T>> en : typeToInstances.entrySet() ) {
			data.add( new FilterRow( en.getKey(), null ) );

			for ( T instance : en.getValue() ) {
				data.add( new FilterRow( en.getKey(), instance ) );
			}
		}
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
		FilterRow vfRow = data.get( row );
		switch ( column ) {
			case 0:
				return ( vfRow.isHeader() || vfRow.instance.isVisible() );
			case 1:
				return ( vfRow.isHeader() ? vfRow.type : null );
			case 2:
				return vfRow.instance;
			default:
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
		FilterRow vfRow = data.get( row );

		if ( vfRow.isHeader() ) {
			for ( FilterRow fr : data ) {
				if ( fr.type.equals( vfRow.type ) ) {
					vfRow.instance.setVisible( (Boolean) value );
				}
			}
			this.fireTableDataChanged();
			return;
		}

		// we are only dealing with one vertex
		vfRow.instance.setVisible( (Boolean) value );
		fireTableCellUpdated( row, row );
	}

	/**
	 * Returns the row count.
	 *
	 * @return int Row count.
	 */
	@Override
	public int getRowCount() {
		return data.size();
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
	 * @param column Column index.
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
	 * @param index Column index.
	 * @return String Column name.
	 */
	@Override
	public String getColumnName( int column ) {
		return columnNames[column];
	}

	/**
	 * Checks whether the cell at a particular row and column index is editable.
	 *
	 * @param row Row index.
	 * @param column Column index.
	 *
	 * @return boolean True if cell is editable.
	 */
	@Override
	public boolean isCellEditable( int row, int column ) {
		return ( 0 == column );
	}
}
