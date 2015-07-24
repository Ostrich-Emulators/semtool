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
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import gov.va.semoss.ui.helpers.GraphColorRepository;
import gov.va.semoss.util.Utility;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
		Shape.class, String.class };
	private Map<URI, List<SEMOSSVertex>> nodeMap = new HashMap<>();
	private List<ColorShapeRow> data = new ArrayList<>();

	@Override
	public void graphUpdated( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph, GraphPlaySheet gps ) {
		generateAllRows( gps.getFilterData().getNodeTypeMap() );
	}

	@Override
	public void layoutChanged( DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph,
			String oldlayout, Layout<SEMOSSVertex, SEMOSSEdge> newlayout ) {
		// nothing to update in this case
	}

	/**
	 * Fills the rows of vertex colors and shapes based on the vertex node and
	 * type.
	 *
	 * @param _typeHash
	 */
	private void generateAllRows( Map<URI, List<SEMOSSVertex>> _nodeMap ) {
		nodeMap = _nodeMap;

		data = new ArrayList<>();
		for ( Map.Entry<URI, List<SEMOSSVertex>> entry : nodeMap.entrySet() ) {
			data.add( new ColorShapeRow( entry.getKey(), null, null, null ) );

			for ( SEMOSSVertex vertex : entry.getValue() ) {
				data.add( new ColorShapeRow( null, vertex,
						vertex.getShape(), vertex.getColor() ) );
			}
		}

		fireTableDataChanged();
	}

	@Override
	public Object getValueAt( int row, int column ) {
		ColorShapeRow csRow = data.get( row );
		switch ( column ) {
			case 0: {
				if ( csRow.type != null ) {
					return csRow.type.getLocalName();
				}
				return "";
			}
			case 1:
				return ( null == csRow.node ? "Set For All"
						: csRow.node.getLabel() );
			case 2:
				return csRow.shape;
			case 3:
				return csRow.color;
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
		// the first column will either be empty, or will be the nodeType

		URI nodeType = data.get( row ).type;

		if ( nodeType == null ) {

			SEMOSSVertex vertex = data.get( row ).node;
			setColorOrShape( row, column, vertex, value );
			Utility.repaintActiveGraphPlaysheet();

			return;
		}

		//set the color or shape for all vertices of this type
		List<SEMOSSVertex> vertexList = nodeMap.get( nodeType );
		int vertIndex = row;
		for ( SEMOSSVertex vertex : vertexList ) {
			setColorOrShape( ++vertIndex, column, vertex, value );
		}

		fireTableCellUpdated( row, column );

		Utility.repaintActiveGraphPlaysheet();
	}

	/**
	 * Adds the colors and shapes to the table.
	 *
	 * @param column Column index.
	 * @param vertName Name of the vertex, in string form.
	 * @param value Value associated with the vertex node, in string form.
	 */
	private void setColorOrShape( int row, int column, SEMOSSVertex vertex, Object value ) {
		if ( column == 2 ) {
			setShape( vertex, Shape.class.cast( value ), row );
		}
		else if ( column == 3 ) {
			setColor( vertex, Color.class.cast( value ), row );
		}
	}

	public void setShapes( Collection<SEMOSSVertex> nodes, Shape shape ) {
		for ( SEMOSSVertex node : nodes ) {
			setShape( node, shape, getRowForVertex( node ) );
		}
	}

	public void setShape( SEMOSSVertex vertex, Shape shape, int row ) {
		if ( row < 0 ) {
			return;
		}
		data.get( row ).shape = shape;
		vertex.setShape( shape );
	}

	public void setColors( Collection<SEMOSSVertex> nodes, Color color ) {
		for ( SEMOSSVertex node : nodes ) {
			setColor( node, color, getRowForVertex( node ) );
		}
	}

	public void setColor( SEMOSSVertex vertex, Color color, int row ) {
		if ( row < 0 ) {
			return;
		}
		data.get( row ).color = color;
		vertex.setColor( color );
	}

	private int getRowForVertex( SEMOSSVertex vert ) {
		int rowNum = -1;
		for ( int i = 0; i < data.size(); i++ ) {
			if ( data.get( i ).node.equals( vert ) ) {
				rowNum = i;
			}
		}

		return rowNum;
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

	public class ColorShapeRow {

		URI type;
		SEMOSSVertex node;
		Color color;
		Shape shape;

		public ColorShapeRow( URI type, SEMOSSVertex name, Shape shape, Color color ) {
			this.type = type;
			this.node = name;
			this.shape = shape;
			this.color = color;
		}
	}
}
