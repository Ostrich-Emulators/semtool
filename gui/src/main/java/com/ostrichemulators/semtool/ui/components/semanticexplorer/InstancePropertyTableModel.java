/**
 * *****************************************************************************
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
 * ****************************************************************************
 */
package com.ostrichemulators.semtool.ui.components.semanticexplorer;

import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import java.util.ArrayList;

import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

/**
 * This class is used to create a table model for vertex properties.
 */
public class InstancePropertyTableModel extends AbstractTableModel {

	private static final Logger log = Logger.getLogger( InstancePropertyTableModel.class );
	private static final long serialVersionUID = -1980815818428292267L;

	private static final String[] COLNAMES = { "Property Name", "XML Datatype", "Value" };
	private static final Class<?>[] CLASSNAMES = { IRI.class, IRI.class, Value.class };
	private final List<Statement> rows;

	/**
	 * Constructor for InstancePropertyTableModel.
	 *
	 * @param propertyList the properties to get
	 */
	public InstancePropertyTableModel( Collection<Statement> propertyList ) {
		rows = new ArrayList<>( propertyList );
	}

	public InstancePropertyTableModel() {
		rows = new ArrayList<>();
	}

	public Model getModel() {
		return new LinkedHashModel( rows );
	}

	public void setModel( Collection<Statement> newdata ) {
		rows.clear();
		rows.addAll( newdata );
		fireTableDataChanged();
	}

	public void clear() {
		rows.clear();
		fireTableDataChanged();
	}

	/**
	 * Gets the cell value at a particular row and column index.
	 *
	 * @param r Row index.
	 * @param c Column index.
	 *
	 * @return Object Cell value.
	 */
	@Override
	public Object getValueAt( int r, int c ) {
		Statement row = rows.get( r );
		switch ( c ) {
			case 0: {
				return row.getPredicate();
			}
			case 1: {
				return RDFDatatypeTools.getDatatype( row.getObject() );
			}
			case 2: {
				return row.getObject();
			}
			default:
				return null;
		}
	}

	/**
	 * Sets the cell value at a particular row and column index.
	 *
	 * @param val Cell value.
	 * @param row Row index.
	 * @param column Column index.
	 */
	@Override
	public void setValueAt( Object val, int row, int column ) {
	}

	/**
	 * Returns the column count.
	 *
	 * @return int Column count.
	 */
	@Override
	public int getColumnCount() {
		return COLNAMES.length;
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
		return COLNAMES[index];
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
	 * @param column Column index.
	 *
	 * @return Class Column class.
	 */
	@Override
	public Class<?> getColumnClass( int column ) {
		return CLASSNAMES[column];
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
		return false;
	}
}
