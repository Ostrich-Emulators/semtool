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
package gov.va.semoss.ui.components.semanticexplorer;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.GuiUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * This class is used to create a table model for vertex properties.
 */
public class InstancePropertyTableModel extends AbstractTableModel {
	private static final Logger log = Logger.getLogger( InstancePropertyTableModel.class );
	private static final long serialVersionUID = -1980815818428292267L;

	private static final String[] columnNames = { "Property Name", "XML Datatype", "Value" };
	private static final Class<?>[] classNames = {URI.class, URI.class, Value.class };
	private ArrayList<PropertyEditorRow> rows;
	
	@SuppressWarnings("unused")
	private final IEngine engine;
	private final Set<URI> uneditableProps = new HashSet<>();
	
	/**
	 * Constructor for InstancePropertyTableModel.
	 * 
	 * @param List<Value[]> propertyList
	 * @param IEngine engine
	 */
	public InstancePropertyTableModel(List<Value[]> propertyList, IEngine _engine) { 
		engine = _engine;
		
		uneditableProps.add(RDF.SUBJECT);
		uneditableProps.add(RDF.TYPE);
		
		populateRows(propertyList);
	}

	private void populateRows(List<Value[]> propertyList) {
		rows = new ArrayList<>();
		URI datatypeURI = null;
		
		for ( Value[] values : propertyList ) {
			if ( values[1] instanceof Literal ) {
				datatypeURI = ((Literal)values[1]).getDatatype();
				if ( datatypeURI == null ) {
					datatypeURI = XMLSchema.STRING;
				}
			}
			else if ( values[1] instanceof URI ) {
				datatypeURI = XMLSchema.ANYURI;
			}
			
			try {
				rows.add( new PropertyEditorRow( values[0], datatypeURI, values[1] ) );
			} catch (Exception e) {
				log.debug("Could not parse out PropertyEditorRow: " + e, e);
			}
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
				return pRow.getName().getLocalName();
			} case 1: { 
				return pRow.getDatatype().getLocalName();
			} case 2: {
				return pRow.getValueAsDisplayString();
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
	@Override
	public void setValueAt(Object val, int row, int column) {
		if (column != 2)
			return;
		
		PropertyEditorRow pRow = rows.get(row);
//		Value oldValue = pRow.getValue();
		
		if ( !pRow.setValue(val) ) {
			GuiUtility.showError("This value is invalid for this datatype, or this datatype is not yet supported.");
			return;
		}

		/*
		StatementPersistenceUtility.updateNodeOrEdgePropertyValue(
				engine, 
				vertex, 
				pRow.getName(), 
				oldValue, 
				pRow.getValue());
		*/
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
	@Override
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
	@Override
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
	@Override
	public boolean isCellEditable(int row, int column) {
		for (URI name:uneditableProps)
			if (name == rows.get(row).getName())
				return false;

		return column == 2 && !XMLSchema.ANYURI.equals( rows.get(row).getDatatype() );
	}

	class PropertyEditorRow {
		private final URI name;
		private final URI datatype;
		private Value value;

		public PropertyEditorRow( Value name, URI datatype, Value value ) throws Exception {
			this.name = new URIImpl( name.stringValue() );
			this.datatype = datatype;
			this.value = value;
		}

		public boolean setValue(Object val) {
			value = Value.class.cast( val );
			return true;
		}

		public URI getName() {
			return name;
		}
		
		public URI getDatatype() {
			return datatype;
		}
		
		public Value getValue() {
			return value;
		}

		public String getValueAsDisplayString() {
			if ( sameDatatype(datatype, XMLSchema.DOUBLE) ) {
				Literal l = Literal.class.cast( value );
				return l.doubleValue() + "";
			} else if ( sameDatatype(datatype, XMLSchema.FLOAT) ) {
				Literal l = Literal.class.cast( value );
				return l.floatValue() + "";
			} else if ( sameDatatype(datatype, XMLSchema.INTEGER) || sameDatatype(datatype, XMLSchema.INT) ) {
				Literal l = Literal.class.cast( value );
				return l.intValue() + "";
			} else if ( sameDatatype(datatype, XMLSchema.BOOLEAN) ) {
				Literal l = Literal.class.cast( value );
				return l.booleanValue() + "";
			} else if ( sameDatatype(datatype, XMLSchema.DATE) ) {
				Literal l = Literal.class.cast( value );
				return l.calendarValue().toGregorianCalendar().getTime() + "";
			} else if ( sameDatatype(datatype, XMLSchema.ANYURI) ) {
				return new URIImpl( value.stringValue() ).getLocalName();
			} else if ( sameDatatype(datatype, XMLSchema.STRING) ) {
				return value.stringValue();
			} else {
				log.warn("We need to handle the case for datatype: " + datatype);
				return value.stringValue();
			}
		}
		
		private boolean sameDatatype(URI uri1, URI uri2) {
			return (uri1.stringValue().equals( uri2.stringValue() ));
		}
	}
}