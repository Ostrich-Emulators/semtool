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
 *****************************************************************************
 */
package gov.va.semoss.ui.components;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.PropComparator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;

/**
 * This class is used to keep track of specific properties for a table.
 */
public class ControlDataTable {

	private static Logger logger = Logger.getLogger( ControlDataTable.class );

	private Object[][] rows = new Object[0][5];
	private Class<?>[] rowClasses = { String.class, URI.class, Boolean.class, Boolean.class, String.class };
	private int rowCount = 0;

	private MultiMap<URI, URI> properties = new MultiMap<>(); // type -> properties
	private Set<URI> propertyShow;
	private Set<URI> propertyShowTT;
	private Set<URI> propertyHide;

	private MultiMap<URI, URI> labelSelectedList = new MultiMap<>(); // type->selected props (labels)
	private Set<URI> labelUnselectedList = new HashSet<>();
	private MultiMap<URI, URI> tooltipSelectedList = new MultiMap<>(); // type->selected props (tooltips)
	private Set<URI> tooltipUnselectedList = new HashSet<>();

	private VisualizationViewer<SEMOSSVertex, SEMOSSEdge> viewer;
	private ControlDataTableModel tableModel;

	public ControlDataTable( Set<URI> _propertyShow, 
			Set<URI> _propertyShowTT, Set<URI> _propertyHide, String[] columnNames ) {
		propertyShow = _propertyShow;
		propertyShowTT = _propertyShowTT;
		propertyHide = _propertyHide;
		tableModel = new ControlDataTableModel( columnNames );
	}

	/**
	 * Adds a property of a specific type to the property hashtable.
	 *
	 * @param type Type of property.
	 * @param property Property.
	 */
	public void addProperty( URI type, URI property ) {
		List<URI> propertyListByType = properties.getNN( type );

		if ( !propertyListByType.contains( property ) && !propertyHide.contains( property ) ) {
			propertyListByType.add( property );
			rowCount++;
		}
	}

	/**
	 * Generates all the rows in the control panel for the specified table and
	 * properties
	 */
	public void populateAllRows() {
		populateFirstRow();

		List<URI> types = new ArrayList<>( properties.keySet() );
		Collections.sort( types, new Comparator<URI>(){

			@Override
			public int compare( URI o1, URI o2 ) {
				return o1.stringValue().compareTo( o2.stringValue() );
			}
		} );

		int rowIndex = 1;
		for ( URI type : types ) {

			List<URI> propertiesForThisType = properties.getNN( type );
			Collections.sort( propertiesForThisType, new PropComparator() );

			boolean firstRow = true;
			for ( URI property : propertiesForThisType ) {
				if ( propertyHide.contains( property ) ) {
					continue;
				}

				populateRow( rowIndex, type, property, firstRow );

				logger.debug( "Adding Row-- " + rowIndex + "<>" + type + "<>" + property );
				firstRow = false;
				rowIndex++;
			}
		}

		tableModel.fireTableDataChanged();
	}

	/**
	 * Populates the first row. columns are: Type, Property, Boolean
	 *
	 * @param rowCount: total number of rows
	 */
	public void populateFirstRow() {
		rows = new Object[rowCount + 1][5];
		rows[0][0] = "SELECT ALL";
		rows[0][1] = "";
		rows[0][2] = new Boolean( true );
		rows[0][3] = new Boolean( true );
		rows[0][4] = "SELECT ALL";
	}

	/**
	 * Populates one row. columns are: Type, Property, Boolean
	 *
	 * @param rowIndex: the row number
	 * @param type: the type of the row
	 * @param property: the property to be shown or hidden
	 * @param firstRow:
	 */
	public void populateRow( int rowIndex, URI type, URI property, boolean firstRow ) {
		rows[rowIndex][0] = "";
		rows[rowIndex][1] = property;
		rows[rowIndex][2] = isSelected( labelSelectedList, type, property );
		rows[rowIndex][3] = isSelected( tooltipSelectedList, type, property );
		rows[rowIndex][4] = type;

		if ( firstRow ) {
			rows[rowIndex][0] = type;
		}

		if ( propertyShow.contains( property ) && !labelUnselectedList.contains( type ) ) {
			setValue(true, rowIndex, 2 );
		}

		if ( propertyShowTT.contains( property ) && !tooltipUnselectedList.contains( type ) ) {
			setValue(true, rowIndex, 3 );
		}
	}

	private static Boolean isSelected( MultiMap<URI, URI> selectedList, 
			URI type, URI property ) {
		if ( !selectedList.containsKey( type ) ) {
			return false;
		}

		for ( URI thisProp : selectedList.get( type ) ) {
			if ( thisProp != null && thisProp.equals( property ) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Sets value at a particular row and column location.
	 *
	 * @param val Label value.
	 * @param row Row number.
	 * @param column Column number.
	 */
	public void setValue( Object val, int row, int column ) {
		rows[row][column] = val;

		if ( column == 2 ) {
			setValue( labelSelectedList, labelUnselectedList, val, row, column );
		}
		else if ( column == 3 ) {
			setValue( tooltipSelectedList, tooltipUnselectedList, val, row, column );
		}
	}

	/**
	 * Sets value at a particular row and column location.
	 *
	 * @param val Label value.
	 * @param row Row number.
	 * @param column Column number.
	 */
	public void setValue( MultiMap<URI, URI> selectedList, 
			Set<URI> unselectedList, Object valueObject, int row, int column ) {
		if ( !( valueObject instanceof Boolean ) ) {
			return;
		}
		Boolean valBoolean = (Boolean) valueObject;

		if ( row == 0 ) {
			// if it is the header row--select all
			for ( int i = 1; i < rows.length; i++ ) {
				setValue( selectedList, unselectedList, valBoolean, i, column );
			}
		}

		URI type = URI.class.cast( rows[row][4] );
		URI property = URI.class.cast( rows[row][1] );

		List<URI> typePropList = selectedList.getNN( type );
		if ( typePropList == null ) {
			typePropList = new ArrayList<>();
		}

		if ( valBoolean ) {
			if ( !typePropList.contains( property ) ) {
				typePropList.add( property );
				unselectedList.remove( type );
			}
		}
		else {
			typePropList.remove( property );
			unselectedList.add( type );
		}

		rows[row][column] = valBoolean;
		tableModel.fireTableCellUpdated( row, column );
		selectedList.put( type, typePropList );
	}

	/**
	 * Gets label value from a particular row and column location.
	 *
	 * @param row Row number.
	 * @param column Column number.
	 *
	 * @return Object Label value.
	 */
	public Object getCell( int row, int column ) {
		if ( row >= rows.length || column >= rows[0].length ) {
			return null;
		}

		return rows[row][column];
	}

	/**
	 * Gets properties of a specific type.
	 *
	 * @param type Type of property to retrieve.
	 *
	 * @return Vector<String> List of properties.
	 */
	public List<URI> getSelectedProperties( URI type ) {
		return labelSelectedList.getNN( type );
	}

	/**
	 * Gets tooltip properties of a specific type.
	 *
	 * @param type Type of property to retrieve.
	 *
	 * @return Vector<String> List of properties.
	 */
	public List<URI> getSelectedPropertiesTT( URI type ) {
		return tooltipSelectedList.get( type );
	}

	public ControlDataTableModel getTableModel() {
		return tableModel;
	}

	public void setViewer( VisualizationViewer<SEMOSSVertex, SEMOSSEdge> _viewer ) {
		viewer = _viewer;
	}

	public class ControlDataTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 502758220766389041L;
		private final String[] columnNames;

		public ControlDataTableModel( String[] _columnNames ) {
			columnNames = _columnNames;
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
			return rows.length;
		}

		/**
		 * Gets the cell value at a particular row and column index.
		 *
		 * @param row Row index.
		 * @param column Column index.
		 *
		 * @return Object Cell value.
		 */
		@Override
		public Object getValueAt( int row, int column ) {
			return getCell( row, column );
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
			return rowClasses[column];
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
			return ( column == 2 || column == 3 );
		}

		/**
		 * Sets the label value at a particular row and column index.
		 *
		 * @param value Label value.
		 * @param row Row index.
		 * @param column Column index.
		 */
		@Override
		public void setValueAt( Object value, int row, int column ) {
			setValue( value, row, column );
			fireTableDataChanged();

			if ( viewer != null ) {
				viewer.repaint();
			}
		}
	}
}
