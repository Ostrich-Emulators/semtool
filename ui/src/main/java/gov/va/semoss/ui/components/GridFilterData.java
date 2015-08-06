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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;

/**
 * This class is the primary ones used for vertex filtering.
 */
public class GridFilterData extends AbstractTableModel {

	// need to have vertex and type information
	// everytime a vertex is added here
	// need to figure out a type so that it can show
	// the types are not needed after or may be it is
	// we need a structure which keeps types with vector
	// the vector will have all of the vertex specific to the type
	// additionally, there needs to be another structure so that when I select or deselect something
	// it marks it on the matrix
	// need to come back and solve this one
	Hashtable<String, Vector> typeHash = new Hashtable<String, Vector>();
	private final List<String> columnNames = new ArrayList<>();

	private final List<Object[]> dataList = new ArrayList<>();

	private static final Logger logger = Logger.getLogger( GridFilterData.class );

	public GridFilterData() {
	}

	public GridFilterData( String[] cnames, Collection<Object[]> data ) {
		columnNames.addAll( Arrays.asList( cnames ) );
		dataList.addAll( data );
	}

	/**
	 * Gets the value at a particular row and column index.
	 *
	 * @param row Row index.
	 * @param column Column index.
	 *
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt( int row, int column ) {
		Object[] val = dataList.get( row );
		Object retVal = val[column];
		return retVal;
	}

	/**
	 * Sets the data list.
	 *
	 * @param newdata List of data.
	 */
	public void setDataList( Collection<Object[]> newdata ) {
		dataList.clear();
		dataList.addAll( newdata );
		fireTableDataChanged();
	}

	/**
	 * Sets the column names.
	 *
	 * @param cnames Column names.
	 */
	public void setColumnNames( String[] cnames ) {
		columnNames.clear();
		columnNames.addAll( Arrays.asList( cnames ) );
		fireTableStructureChanged();
	}

	@Override
	public int getRowCount() {
		return dataList.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.size();
	}

	public List<Object[]> getDataList() {
		return dataList;
	}

	@Override
	public Class<?> getColumnClass( int column ) {
		Class returnValue = null;
		if ( ( column >= 0 ) && ( column < getColumnCount() ) ) {

			boolean exit = false;
			int rowCount = 0;
			while ( !exit && rowCount < getRowCount() ) {
				if ( getValueAt( rowCount, column ) != null ) {
					exit = true;
					returnValue = getValueAt( rowCount, column ).getClass();
				}
				rowCount++;
			}
			if ( !exit ) {
				returnValue = String.class;
			}

		}
		else {
			returnValue = Object.class;
		}
		return returnValue;
	}

	@Override
	public String getColumnName( int index ) {
		return columnNames.get( index );
	}
}
