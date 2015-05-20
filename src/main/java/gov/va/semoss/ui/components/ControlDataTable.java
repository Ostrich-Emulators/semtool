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
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import gov.va.semoss.om.SEMOSSEdge;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.MultiMap;
import gov.va.semoss.util.PropComparator;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;

/**
 * This class is used to keep track of specific properties for a table.
 */
public class ControlDataTable {

	private static Logger logger = Logger.getLogger( ControlDataTable.class );

	private List<ControlDataRow> data = new ArrayList<>();
	private Class<?>[] rowClasses = { URI.class, URI.class, Boolean.class,
		Boolean.class, String.class };

	private MultiMap<URI, URI> properties = new MultiMap<>(); // type -> properties
	private Set<URI> propertyShow;
	private Set<URI> propertyShowTT;
	private Set<URI> propertyHide;

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
			properties.add( type, property );
		}
	}

	/**
	 * Generates all the rows in the control panel for the specified table and
	 * properties
	 */
	public void populateAllRows() {
		data.clear();

		List<URI> types = new ArrayList<>( properties.keySet() );
		Collections.sort( types, new PropComparator() );

		for ( URI type : types ) {
			ControlDataRow header
					= new ControlDataRow( type, Constants.ANYNODE, true, true );
			data.add( header );

			List<URI> propertiesForThisType = properties.getNN( type );
			Collections.sort( propertiesForThisType, new PropComparator() );

			for ( URI property : propertiesForThisType ) {
				if ( propertyHide.contains( property ) ) {
					continue;
				}

				ControlDataRow cdr = new ControlDataRow( type, property,
						propertyShow.contains( property ),
						propertyShowTT.contains( property ) );

				data.add( cdr );
			}
		}

		tableModel.fireTableDataChanged();
	}

	/**
	 * Sets value at a particular row and column location.
	 *
	 * @param val Label value.
	 * @param row Row number.
	 * @param column Column number.
	 */
	public void setValue( Object val, int row, int column ) {
		List<ControlDataRow> tochange = new ArrayList<>();

		ControlDataRow myrow = data.get( row );
		if ( Constants.ANYNODE.equals( myrow.prop ) ) {
			// this is the "SELECT ALL" row, so select all the properties of this type
			for ( ControlDataRow cc : data ) {
				if ( cc.type.equals( myrow.type ) ) {
					tochange.add( cc );
				}
			}
		}
		else {
			tochange.add( myrow );
		}

		if ( 2 == column ) {
			for ( ControlDataRow cdr : tochange ) {
				cdr.label = Boolean.class.cast( val );
			}
		}
		else if ( 3 == column ) {
			for ( ControlDataRow cdr : tochange ) {
				cdr.tooltip = Boolean.class.cast( val );
			}
		}
	}

	/**
	 * Gets properties of a specific type.
	 *
	 * @param type Type of property to retrieve.
	 *
	 * @return Vector<String> List of properties.
	 */
	public List<URI> getSelectedProperties( URI type ) {
		List<URI> selecteds = new ArrayList<>();
		for ( ControlDataRow cdr : data ) {
			if ( cdr.type.equals( type ) && cdr.label ) {
				selecteds.add( cdr.prop );
			}
		}

		return selecteds;
	}

	/**
	 * Gets tooltip properties of a specific type.
	 *
	 * @param type Type of property to retrieve.
	 *
	 * @return Vector<String> List of properties.
	 */
	public List<URI> getSelectedPropertiesTT( URI type ) {
		List<URI> selecteds = new ArrayList<>();
		for ( ControlDataRow cdr : data ) {
			if ( cdr.type.equals( type ) && cdr.tooltip ) {
				selecteds.add( cdr.prop );
			}
		}

		return selecteds;
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
			return data.size();
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
			ControlDataRow cdr = data.get( row );
			switch ( column ) {
				case 0:
					return cdr.type;
				case 1:
					return cdr.prop;
				case 2:
					return cdr.label;
				case 3:
					return cdr.tooltip;
				default:
					return null;
			}
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

	public class ControlDataRow {

		URI type;
		URI prop;
		boolean label;
		boolean tooltip;
		String other;

		public ControlDataRow( URI type, URI prop, boolean label, boolean toolt ) {
			this.type = type;
			this.prop = prop;
			this.label = label;
			this.tooltip = toolt;
		}

		public ControlDataRow( URI type, URI prop ) {
			this( type, prop, false, false );
		}
	}
}
