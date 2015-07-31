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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 * This class is used to create a table model for vertex properties.
 */
public class NodeEdgePropertyTableModel<T extends NodeEdgeBase> extends AbstractTableModel {

	private static final long serialVersionUID = -1980815818428292267L;

	private static final String[] columnNames = { "Name ", "Value" };
	private static final Class<?>[] classNames = { URI.class, Value.class };
	private final List<PropertyRow> rows = new ArrayList<>();
	private final T vertex;

	/**
	 * Constructor for VertexPropertyTableModel.
	 *
	 * @param vertex SEMOSSVertex
	 */
	public NodeEdgePropertyTableModel( T vertex,
			Graph<SEMOSSVertex, SEMOSSEdge> graph ) {
		this.vertex = vertex;
		for ( Map.Entry<URI, Value> entry : vertex.getValues().entrySet() ) {
			if ( !RDF.SUBJECT.equals( entry.getKey() ) ) {
				rows.add( new PropertyRow( entry.getKey(), entry.getValue() ) );
			}
		}
	}

	protected void addRow( PropertyRow pr ) {
		rows.add( pr );
	}

	/**
	 * Gets the cell value at a particular row and column index.
	 *
	 * @param arg0 Row index.
	 * @param arg1 Column index.
	 *
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt( int row, int column ) {
		PropertyRow pRow = rows.get( row );
		if ( 0 == column ) {
			return pRow.name;
		}
		else {
			return pRow.value;
		}
	}

	/**
	 * Sets the cell value at a particular row and column index.
	 *
	 * @param val Cell value.
	 * @param row Row index.
	 * @param column Column index.
	 */
	@Override
	public void setValueAt( Object val, int row, int column ) {
		PropertyRow pRow = rows.get( row );
		if ( 0 == column ) {
			pRow.name = URI.class.cast( val );
		}
		else {
			pRow.value = Value.class.cast( val );
		}

		vertex.setValue( pRow.name, pRow.value );
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
	 * @param index Column index.
	 *
	 * @return String Column name.
	 */
	@Override
	public String getColumnName( int index ) {
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
	 * @param column Column index.
	 *
	 * @return Class Column class.
	 */
	@Override
	public Class<?> getColumnClass( int column ) {
		return classNames[column];
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
		PropertyRow pr = rows.get( row );
		boolean ok = ( 1 == column && !pr.ro );
		return ok;
	}

	protected class PropertyRow {

		public URI name;
		public Value value;
		public final boolean ro;

		public PropertyRow( URI name, Value value, boolean readonly ) {
			this.name = name;
			this.value = value;
			this.ro = readonly;
		}

		public PropertyRow( URI name, Value value ) {
			this( name, value, value instanceof URI );
		}
	}
}
