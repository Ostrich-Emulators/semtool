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
package com.ostrichemulators.semtool.ui.components.semanticexplorer;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.ui.components.renderers.LabeledPairRenderer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
	private final LabeledPairRenderer<URI> renderer;
	
	/**
	 * Constructor for InstancePropertyTableModel.
	 * 
	 * @param propertyList the properties to get
	 * @param _engine the engine to query
	 */
	public InstancePropertyTableModel(List<Value[]> propertyList, IEngine _engine) { 		
		renderer = LabeledPairRenderer.getUriPairRenderer();
		renderer.cache(XMLSchema.ANYURI, "URI");
		
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
	 * @param row Row index.
	 * @param column Column index.
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
				if (pRow.getDatatype() == XMLSchema.ANYURI)
					return "URI";
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
		return false;
	}
}