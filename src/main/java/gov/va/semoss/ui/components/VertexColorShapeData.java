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
package gov.va.semoss.ui.components;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.components.api.GraphListener;
import gov.va.semoss.ui.components.models.FilterRow;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.openrdf.model.URI;

/**
 * This class is used primarily for vertex filtering.
 */
public class VertexColorShapeData extends AbstractTableModel implements GraphListener {

	private static final long serialVersionUID = -8530913683566271008L;

	private static final String[] columnNames = { "Node", "Instance", "Shape", "Color" };
	private static final Class<?>[] columnClasses = { String.class, String.class,
		Shape.class, Color.class };
	private List<FilterRow<SEMOSSVertex>> data = new ArrayList<>();

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
		generateAllRows( gps.getVerticesByType() );
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout,
			GraphPlaySheet gps ) {
		// nothing to update in this case
	}

	/**
	 * Fills the rows of vertex colors and shapes based on the vertex node and
	 * type.
	 *
	 * @param _typeHash
	 */
	private void generateAllRows( Map<URI, List<SEMOSSVertex>> nodeMap ) {
		data = new ArrayList<>();
		for ( Map.Entry<URI, List<SEMOSSVertex>> entry : nodeMap.entrySet() ) {
			data.add( new FilterRow( entry.getKey(), null ) );

			for ( SEMOSSVertex vertex : entry.getValue() ) {
				data.add( new FilterRow( entry.getKey(), vertex ) );
			}
		}

		fireTableDataChanged();
	}

	@Override
	public Object getValueAt( int row, int column ) {
		FilterRow<SEMOSSVertex> csRow = data.get( row );
		switch ( column ) {
			case 0:
				return ( csRow.isHeader() ? csRow.type.getLocalName() : "" );
			case 1:
				return ( csRow.isHeader() ? "Set For All" : csRow.instance.getLabel() );
			case 2:
				return ( csRow.isHeader() ? null : csRow.instance.getShape() );
			case 3:
				return ( csRow.isHeader() ? null : csRow.instance.getColor() );
			default:
				return null;
		}
	}

	/**
	 * Sets the cell color or shape at a particular row and column index.
	 *
	 * @param value Cell value.
	 * @param row Row index (int).
	 * @param column Column index (int).
	 */
	@Override
	public void setValueAt( Object value, int row, int column ) {
		FilterRow<SEMOSSVertex> csrow = data.get( row );

		if ( csrow.isHeader() ) {
			for ( FilterRow<SEMOSSVertex> cs : data ) {
				if ( cs.type.equals( csrow.type ) && !cs.isHeader() ) {
					if ( 2 == column ) {
						cs.instance.setShape( Shape.class.cast( value ) );
					}
					else {
						cs.instance.setColor( Color.class.cast( value ) );
					}
				}
			}

			this.fireTableDataChanged();
			return;
		}

		if ( 2 == column ) {
			csrow.instance.setShape( Shape.class.cast( value ) );
		}
		else {
			csrow.instance.setColor( Color.class.cast( value ) );
		}
		
		fireTableCellUpdated( row, column );
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex ) {
		return columnIndex > 1;
	}

	/**
	 * Gets the number of rows.
	 *
	 * @return int Number of rows.
	 */
	@Override
	public int getRowCount() {
		return data.size();
	}

	/**
	 * Gets the number of columns.
	 *
	 * @return int Number of columns.
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Gets the column node at a particular index.
	 *
	 * @param index Column index.
	 * @return String Column node.
	 */
	@Override
	public String getColumnName( int index ) {
		return columnNames[index];
	}

	/**
	 * Gets the column class at a particular index.
	 *
	 * @param column
	 * @return Class<?> Column class.
	 */
	@Override
	public Class<?> getColumnClass( int column ) {
		return columnClasses[column];
	}
}
