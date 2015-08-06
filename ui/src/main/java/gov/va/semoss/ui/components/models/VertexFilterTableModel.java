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


import gov.va.semoss.om.GraphElement;
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;

/**
 * This class is used to create a table model for the vertex filter.
 */
public class VertexFilterTableModel<T extends GraphElement> extends AbstractTableModel {

	private static final long serialVersionUID = 6010606033514579342L;
	private static final Class<?>[] classNames = { Boolean.class, URI.class, URI.class };

	private final String[] columnNames = { "Show", "Node Type", "Instance" };
	private final List<FilterRow> data = new ArrayList<>();
	private final Set<URI> visibleTypes = new HashSet<>();

	public VertexFilterTableModel( String middleColumnName ) {
		columnNames[1] = middleColumnName;
	}

	/**
	 * Constructor for VertexFilterTableModel.
	 *
	 * @param _data VertexFilterData
	 */
	public VertexFilterTableModel( Collection<T> instances, String middleColumnName ) {
		this( middleColumnName );
		refresh( instances );
	}
	
	public void clear(){
		data.clear();
		fireTableDataChanged();
	}

	public final void refresh( Collection<T> instances ) {
		data.clear();

		MultiMap<URI, T> typeToInstances = new MultiMap<>();
		for ( T t : instances ) {
			typeToInstances.add( t.getType(), t );
		}
		for ( Map.Entry<URI, List<T>> en : typeToInstances.entrySet() ) {
			data.add( new FilterRow( en.getKey(), null ) );

			for ( T instance : en.getValue() ) {
				data.add( new FilterRow( en.getKey(), instance ) );
			}

			populateVisibleTypes( en.getKey(), en.getValue() );
		}

		Collections.sort( data );
		fireTableDataChanged();
	}

	private void populateVisibleTypes( URI type, Collection<T> items ) {
		int visibles = 0;
		for ( T t : items ) {
			if ( t.isVisible() ) {
				visibles++;
			}
		}

		if ( visibles > 0 ) {
			visibleTypes.add( type );
		}
	}

	public FilterRow<T> getRawRow( int row ) {
		return data.get( row );
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
				return ( vfRow.isHeader()
						? typeIsVisible( vfRow.type )
						: vfRow.instance.isVisible() );
			case 1:
				return ( vfRow.isHeader() ? vfRow.type : null );
			case 2:
				return ( vfRow.isHeader() ? null : vfRow.instance.getURI() );
			default:
				return null;
		}
	}

	private boolean typeIsVisible( URI type ) {
		return visibleTypes.contains( type );
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
		boolean visible = Boolean.class.cast( value );

		if ( vfRow.isHeader() ) {
			List<FilterRow> tochange = new ArrayList<>();

			for ( FilterRow fr : data ) {
				if ( fr.type.equals( vfRow.type ) && !fr.isHeader() ) {
					tochange.add( fr );
				}
			}

			for ( FilterRow fr : tochange ) {
				fr.instance.setVisible( visible );
			}

			if ( visible ) {
				visibleTypes.add( vfRow.type );
			}
			else {
				visibleTypes.remove( vfRow.type );
			}
			fireTableDataChanged();
		}
		else {
			// we are only dealing with one vertex
			vfRow.instance.setVisible( visible );
			fireTableCellUpdated( row, row );
		}
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
