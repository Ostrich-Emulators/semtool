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
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.StatementPersistenceUtility;

import com.ostrichemulators.semtool.util.GuiUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 * This class is used to create a table model for vertex properties.
 */
public class PropertyEditorTableModel extends AbstractTableModel {
	private static final Logger log = Logger.getLogger( PropertyEditorTableModel.class );
	private static final long serialVersionUID = -1980815818428292267L;

	private static final String[] columnNames = { "Node Label", "Property Name", "XML Datatype", "Value" };
	private static final Class<?>[] classNames = { String.class, URI.class, URI.class, Value.class };
	private ArrayList<PropertyEditorRow> rows;
	private final Collection<? extends GraphElement> pickedVertices;
	private final IEngine engine;
	private final Set<URI> uneditableProps = new HashSet<>();
	
	/**
	 *
	 * @param _pickedVertices the verts/edges to show
	 * @param _engine the place where the data is
	 */
	public PropertyEditorTableModel(Collection<? extends GraphElement> _pickedVertices,
			IEngine _engine) { 
		pickedVertices = _pickedVertices;
		engine = _engine;
		
		uneditableProps.add(RDF.SUBJECT);
		uneditableProps.add(RDF.TYPE);
		
		populateRows();
	}

	public final void populateRows() {
		rows = new ArrayList<>();
		URI datatypeURI;
		for ( GraphElement vertex : pickedVertices ) {
			for ( Map.Entry<URI, Value> entry : vertex.getValues().entrySet() ) {
				// don't edit the SUBJECT field (it's just an ID for the vertex)
				if( !RDF.SUBJECT.equals( entry.getKey()) ){
					if ( entry.getValue() instanceof Literal ) {
						Literal literal = (Literal) entry.getValue();
						if ( literal.getDatatype() == null ) {
							datatypeURI = XMLSchema.STRING;
						}
						else {
							datatypeURI = literal.getDatatype();
						}
						rows.add( new PropertyEditorRow( vertex, entry.getKey(), datatypeURI,
								entry.getValue() ) );
					}
					else if ( entry.getValue() instanceof URI ) {
						rows.add( new PropertyEditorRow( vertex, entry.getKey(), XMLSchema.ANYURI,
								entry.getValue() ) );
					}
				}
			}
		}
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		PropertyEditorRow pRow = rows.get(row);
		switch ( column ) {
			case 0: {
				return pRow.getVertex().getLabel();
			} case 1: { 
				return pRow.getName();
			} case 2: { 
				return pRow.getDatatype();
			} case 3: { 
				return pRow.getValue();
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
		if (column != 3)
			return;
		
		PropertyEditorRow pRow = rows.get(row);
		Value oldValue = pRow.getValue();
		
		if ( !pRow.setValue(val) ) {
			GuiUtility.showError("This value is invalid for this datatype, or this datatype is not yet supported.");
			return;
		}
		
		GraphElement vertex = pRow.getVertex();
		vertex.setValue(pRow.getName(), pRow.getValue());
		StatementPersistenceUtility.updateNodeOrEdgePropertyValue(
				engine, 
				vertex, 
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

		return column == 3 && ! XMLSchema.ANYURI.equals( rows.get(row).getDatatype() );
	}

	public class PropertyEditorRow {
		private final GraphElement vertex;
		private final URI name;
		private final URI datatype;
		private Value value;

		public PropertyEditorRow( GraphElement vertex, URI name, URI datatype, Value value ) {
			this.vertex = vertex;
			this.name = name;
			this.datatype = datatype;
			this.value = value;
		}

		public boolean setValue(Object val) {
			value = Value.class.cast( val );
			return true;
		}

		public GraphElement getVertex() {
			return vertex;
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
	}
}