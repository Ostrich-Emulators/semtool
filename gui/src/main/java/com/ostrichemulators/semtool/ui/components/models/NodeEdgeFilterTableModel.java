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
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.SemossGraphVisualization;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.MultiMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import javax.swing.table.AbstractTableModel;

import org.eclipse.rdf4j.model.URI;

/**
 * This class is used to create a table model for the vertex filter.
 * @param <T>
 */
public class NodeEdgeFilterTableModel<T extends GraphElement> extends AbstractTableModel {

	private static final long serialVersionUID = 6010606033514579342L;
	private static final Class<?>[] classNames = { Boolean.class, URI.class, URI.class };

	private final String[] columnNames = { "Show", "", "Instance" };
	private final List<FilterRow> data = new ArrayList<>();
	private SemossGraphVisualization viz;

	public NodeEdgeFilterTableModel( String middleColumnName ) {
		columnNames[1] = middleColumnName;
	}

	public void clear() {
		data.clear();
		fireTableDataChanged();
	}

	public void refresh( Collection<T> instances, SemossGraphVisualization v ) {
		data.clear();
		viz = v;

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

		Collections.sort( data );
		fireTableDataChanged();
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
				return !( vfRow.isHeader()
						? typeIsHidden( vfRow.type )
						: viz.isHidden( vfRow.instance ) );
			case 1:
				return ( vfRow.isHeader() ? vfRow.type : null );
			case 2:
				return ( vfRow.isHeader() ? null : vfRow.instance.getIRI() );
			default:
				return null;
		}
	}

	private boolean typeIsHidden( URI type ) {
		int visible = 0;

		for ( FilterRow fr : data ) {
			if ( fr.type.equals( type ) ) {
				if ( !fr.isHeader() ) {
					if ( !viz.isHidden( fr.instance ) ) {
						visible++;
					}
				}
			}
		}

		return ( 0 == visible );
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
		boolean hideit = !Boolean.class.cast( value );

		if ( vfRow.isHeader() ) {
			// all vertices/edges of this type
			// viz.hide( vfRow.type, hideit );
			List<GraphElement> tochange = new ArrayList<>();
			for ( FilterRow fr : data ) {
				if ( fr.type.equals( vfRow.type ) && !fr.isHeader() ) {
					tochange.add( fr.instance );
				}
			}

			viz.hide( tochange, hideit );
			fireTableDataChanged();
		}
		else {
			// we are only dealing with one vertex
			//viz.hide( vfRow.instance.getIRI(), hideit );
			viz.hide( Arrays.asList( vfRow.instance ), hideit );
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

	@Override
	public Class<?> getColumnClass( int columnIndex ) {
		return classNames[columnIndex];
	}

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

	public class FilterRow<T extends GraphElement> implements Comparable<FilterRow> {

		public final URI type;
		public final T instance;

		public FilterRow( URI type, T nodeedge ) {
			this.type = type;
			this.instance = nodeedge;
		}

		public boolean isHeader() {
			return ( null == instance || Constants.ANYNODE.equals( instance ) );
		}

		@Override
		public int compareTo( FilterRow o ) {
		// if we have the same type, sort on the instance label
			// if we don't have the same type, sort by type

			if ( type.equals( o.type ) ) {
				if ( isHeader() ) {
					return -1;
				}
				if ( o.isHeader() ) {
					return 1;
				}
				return instance.getLabel().compareTo( o.instance.getLabel() );
			}

			// types aren't the same, so just worry about sorting on type
			return type.stringValue().compareTo( o.type.stringValue() );
		}
	}
}
