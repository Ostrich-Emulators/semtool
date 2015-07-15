/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.components.models;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.StatementPersistenceUtility;
import gov.va.semoss.util.Utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * This class is used to create a table model for vertex properties.
 */
public class PropertyEditorTableModel extends AbstractTableModel {
	private static final Logger log = Logger.getLogger( PropertyEditorTableModel.class );
	private static final long serialVersionUID = -1980815818428292267L;

	private static final String[] columnNames = { "Property Name", "XML Datatype", "Value" };
	private static final Class<?>[] classNames = { String.class, String.class, String.class };
	private ArrayList<PropertyEditorRow> rows = new ArrayList<PropertyEditorRow>();
	private AbstractNodeEdgeBase nodeOrEdge;
	private IPlaySheet playsheet;
	private Set<String> uneditableProperties = new HashSet<String>();
	
	/**
	 * Constructor for VertexPropertyTableModel.
	 * 
	 * @param nodeOrEdge AbstractNodeEdgeBase
	 * @param IEngine engine
	 */
	public PropertyEditorTableModel(AbstractNodeEdgeBase _nodeOrEdge,
			IPlaySheet _playsheet) { 
		nodeOrEdge = _nodeOrEdge;
		playsheet = _playsheet;
		
		uneditableProperties.add(Constants.URI_KEY.stringValue());
		uneditableProperties.add(Constants.VERTEX_TYPE.stringValue());
	}


	public void setData(List<Value[]> data, List<String> newheaders) {
		String datatype = "";
		for ( Map.Entry<URI, Value> entry : nodeOrEdge.getValues().entrySet() ) {
			if (entry.getValue() instanceof Literal) {
				Literal literal = (Literal) entry.getValue();
				if (literal.getDatatype()==null) {
					datatype = Constants.STRING_URI;
				} else { 
					datatype = literal.getDatatype().stringValue();
				}
			} else if (entry.getValue() instanceof URI) {
				datatype = Constants.ANYURI_URI;
			}
			rows.add( new PropertyEditorRow(entry.getKey(), datatype, entry.getValue()));
		}
	}
	
	/**
	 * Gets the cell value at a particular row and column index.
	 * 
	 * @param arg0
	 *            Row index.
	 * @param arg1
	 *            Column index.
	 * 
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt(int row, int column) {
		PropertyEditorRow pRow = rows.get(row);
		switch ( column ) {
			case 0: {
				return pRow.getName().stringValue();
			} case 1: { 
				return pRow.getDatatype();
			} case 2: { 
				return pRow.getValue().stringValue();
			} default:
				return null;
		}
	}

	/**
	 * Sets the cell value at a particular row and column index.
	 * 
	 * @param val
	 *            Cell value.
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 */
	public void setValueAt(Object val, int row, int column) {
		PropertyEditorRow pRow = rows.get(row);
		
		if (column != 2)
			return;
		
		for (String name:uneditableProperties) {
			if (name.equals(pRow.getName().stringValue())) {
				Utility.showError("You cannot edit a property with this name from this screen.");
				return;
			}
		}
		
		Value oldValue = pRow.getValue();
		if ( !pRow.setValue(val) ) {
			Utility.showError("This value is invalid for this datatype, or this datatype is not yet supported.");
			return;
		}
		
		nodeOrEdge.setValue(pRow.getName(), pRow.getValue());
		StatementPersistenceUtility.updateNodeOrEdgePropertyValue(
				playsheet.getEngine(), 
				nodeOrEdge, 
				pRow.getName(), 
				oldValue, 
				pRow.getValue());
		
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
	 * @param index
	 *            Column index.
	 * 
	 * @return String Column name.
	 */
	public String getColumnName(int index) {
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
	 * @param column
	 *            Column index.
	 * 
	 * @return Class Column class.
	 */
	public Class<?> getColumnClass(int column) {
		return classNames[column];
	}

	/**
	 * Checks whether the cell at a particular row and column index is editable.
	 * 
	 * @param row
	 *            Row index.
	 * @param column
	 *            Column index.
	 * 
	 * @return boolean True if cell is editable.
	 */
	public boolean isCellEditable(int row, int column) {
		if (column == 2)
			return true;
		return false;
	}

	public class PropertyEditorRow {
		private URI name;
		private String datatype;
		private Value value;

		public PropertyEditorRow( URI name, String datatype, Value value ) {
			this.name = name;
			this.datatype = datatype;
			this.value = value;
		}
		
		public boolean setValue(Object val) {
			Value tempValue = ValueTableModel.getValueFromDatatypeAndString(datatype, val+"");
			if (tempValue==null) {
				log.warn("Could not set Value " + val + " for datatype " + datatype);
				return false;
			}
			
			value = tempValue;
			return true;
		}

		public void setName(URI name) {
			this.name = name;
		}

		public void setDatatype(String datatype) {
			this.datatype = datatype;
		}

		public URI getName() {
			return name;
		}
		
		public String getDatatype() {
			return datatype;
		}
		
		public Value getValue() {
			return value;
		}
	}
}