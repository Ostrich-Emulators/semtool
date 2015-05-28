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

import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.ui.helpers.TypeColorShapeTable;
import gov.va.semoss.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * This class is used primarily for vertex filtering.
 */
public class VertexColorShapeData extends AbstractTableModel {
	private static final long serialVersionUID = -8530913683566271008L;
	private final static Logger logger = Logger.getLogger( VertexColorShapeData.class );
	
	private Map<URI, List<SEMOSSVertex>> typeHash = new HashMap<>();
	private List<ColorShapeRow> data = new ArrayList<ColorShapeRow>();
	private static final String[] scColumnNames = { "Node", "Instance", "Shape", "Color" };
	private int numRows = 0;

	@Override
	public Object getValueAt( int row, int column ) {
		ColorShapeRow csRow = data.get( row );
		switch ( column ) {
			case 0: {
				if ( csRow.type != null )
					return csRow.type.getLocalName();
				return "";
			} case 1:
				return csRow.name;
			case 2:
				return csRow.shape;
			case 3:
				return csRow.color;
			default:
				return null;
		}
	}

	/**
	 * Gets the number of rows.
	 *
	 * @return int Number of rows.
	 */
	@Override
	public int getRowCount() {
		numRows = 0;
		if ( typeHash == null ) {
			return numRows;
		}

		for ( Map.Entry<URI, List<SEMOSSVertex>> entry : typeHash.entrySet() ) {
			numRows++;
			numRows += entry.getValue().size();
		}

		return numRows;
	}

	/**
	 * Gets the number of columns.
	 *
	 * @return int Number of columns.
	 */
	@Override
	public int getColumnCount() {
		return scColumnNames.length;
	}

	/**
	 * Gets the column name at a particular index.
	 *
	 * @param index Column index.
	 * @return String Column name.
	 */
	@Override
	public String getColumnName( int index ) {
		return scColumnNames[index];
	}

	/**
	 * Gets the column class at a particular index.
	 *
	 * @param column
	 * @return Class<?> Column class.
	 */
	@Override
	public Class<?> getColumnClass( int column ) {
		return String.class;
	}

	/**
	 * Fills the rows of vertex colors and shapes based on the vertex name and
	 * type.
	 *
	 * @param _typeHash
	 */
	public void generateAllRows( Map<URI, List<SEMOSSVertex>> _typeHash ) {
		logger.debug( "Populating rows of the table in Graph Cosmetics tab." );
		typeHash = _typeHash;

		numRows = 0;
		for ( Map.Entry<URI, List<SEMOSSVertex>> entry : typeHash.entrySet() ) {
			data.add( new ColorShapeRow(entry.getKey(), "Set for All", "", "") );
			numRows++;

			for ( SEMOSSVertex vertex : entry.getValue() ) {
				data.add( new ColorShapeRow(null, vertex.getLabel(), vertex.getShapeString(), vertex.getColorString()) );
				numRows++;
			}
		}

		fireTableDataChanged();
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
		
		URI nodeType = data.get(row).type;
		
		if ( nodeType == null ) {
			// find the node type by scanning up the first column
			int numRowsUp = 0;
			while ( nodeType == null ) {
				numRowsUp++;
				nodeType = data.get(row - numRowsUp).type;
			}
			
			List<SEMOSSVertex> vertexList = typeHash.get( nodeType );

			SEMOSSVertex vertex = vertexList.get( numRowsUp - 1 );
			setColorOrShape( row, column, vertex, value + "" );
			Utility.repaintActiveGraphPlaysheet();

			return;
		}

		//set the color or shape for all vertices of this type
		List<SEMOSSVertex> vertexList = typeHash.get( nodeType );
		for ( int vertIndex = 0; vertIndex < vertexList.size(); vertIndex++ ) {
			SEMOSSVertex vertex = vertexList.get( vertIndex );
			setColorOrShape( row + vertIndex + 1, column, vertex, value + "" );
		}

		fireTableCellUpdated( row, column );

		Utility.repaintActiveGraphPlaysheet();
	}

	/**
	 * Adds the colors and shapes to the table.
	 *
	 * @param column Column index.
	 * @param vertName Name of the vertex, in string form.
	 * @param value Value associated with the vertex name, in string form.
	 */
	private void setColorOrShape( int row, int column, SEMOSSVertex vertex, String value ) {
		if ( column == 2 ) {
			setShape( vertex, value, row );
		}
		else if ( column == 3 ) {
			setColor( vertex, value, row );
		}
	}

	public void setShape( SEMOSSVertex vertex, String shape ) {
		setShape( vertex, shape, getRowForVertex( vertex.getLabel() ) );
	}

	public void setShape( SEMOSSVertex vertex, String shape, int row ) {
		if ( row < 0 ) {
			return;
		}

		data.get(row).shape = shape;
		TypeColorShapeTable.getInstance().setShape( shape, vertex );
	}

	public void setColor( SEMOSSVertex vertex, String color ) {
		setColor( vertex, color, getRowForVertex( vertex.getLabel() ) );
	}

	public void setColor( SEMOSSVertex vertex, String color, int row ) {
		if ( row < 0 ) {
			return;
		}

		data.get(row).color = color;
		TypeColorShapeTable.getInstance().setColor( color, vertex );
	}
	
	private int getRowForVertex( String vertexName ) {
		int rowNum = -1;		
		for ( int i=0; i<data.size(); i++ )
			if ( data.get(i).name.equals( vertexName ) )
				rowNum = i;

		return rowNum;
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex ) {
		return columnIndex > 1;
	}
	
	public class ColorShapeRow {
		URI type;
		String name, shape, color;

		public ColorShapeRow( URI type, String name, String shape, String color ) {
			this.type = type;
			this.name = name;
			this.shape = shape;
			this.color = color;
		}
	}
}
