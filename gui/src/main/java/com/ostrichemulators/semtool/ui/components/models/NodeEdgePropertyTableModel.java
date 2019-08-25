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
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.om.SEMOSSEdge;
import com.ostrichemulators.semtool.om.SEMOSSVertex;

import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.SemossGraphVisualization;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.PropComparator;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 * This class is used to create a table model for vertex properties.
 */
public class NodeEdgePropertyTableModel extends AbstractTableModel implements ItemListener {

	private static final long serialVersionUID = -1980815818428292267L;
	private static final Comparator<PropertyRow> COMPARER = new Comparator<PropertyRow>() {
		PropComparator comp = new PropComparator();

		@Override
		public int compare( PropertyRow o1, PropertyRow o2 ) {
			return comp.compare( o1.name, o2.name );
		}
	};

	private static final String[] columnNames = { "Name ", "Value" };
	private static final Class<?>[] classNames = { IRI.class, Value.class };
	private final List<PropertyRow> rows = new ArrayList<>();
	private GraphElement vertex = null;
	private SemossGraphVisualization viz;

	public void setVisualization( SemossGraphVisualization vizzy ) {
		viz = vizzy;

		viz.getPickedEdgeState().addItemListener( this );
		viz.getPickedVertexState().addItemListener( this );
	}

	public void clear() {
		rows.clear();
		fireTableDataChanged();
	}

	public void setItem( GraphElement item ) {
		vertex = item;
		if ( item.isNode() ) {
			setVertex( SEMOSSVertex.class.cast( item ) );
		}
		else {
			setEdge( SEMOSSEdge.class.cast( item ) );
		}
	}

	public void setVertex( SEMOSSVertex item ) {
		rows.clear();

		Collection<SEMOSSEdge> ins = viz.getGraph().getInEdges( item );
		Collection<SEMOSSEdge> outs = viz.getGraph().getOutEdges( item );
		
		rows.add( new PropertyRow( Constants.IN_EDGE_CNT,
				SimpleValueFactory.getInstance().createLiteral( Integer.toString( ins.size() ), XMLSchema.INT ), true ) );
		rows.add( new PropertyRow( Constants.OUT_EDGE_CNT,
				SimpleValueFactory.getInstance().createLiteral( Integer.toString( outs.size() ), XMLSchema.INT ), true ) );
		refresh( item );
	}

	public void setEdge( SEMOSSEdge item ) {
		rows.clear();
		refresh( item );
	}

	private void refresh( GraphElement item ) {
		for ( Map.Entry<IRI, Value> entry : item.getValues().entrySet() ) {
			if ( !RDF.SUBJECT.equals( entry.getKey() ) ) {
				rows.add( new PropertyRow( entry.getKey(), entry.getValue() ) );
			}
		}

		Collections.sort( rows, COMPARER );
		fireTableDataChanged();
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
		PropertyRow pRow = rows.get( row );
		if ( 0 == column ) {
			return pRow.name;
		}
		else {
			return pRow.value;
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
		PropertyRow pRow = rows.get( row );
		if ( 0 == column ) {
			pRow.name = IRI.class.cast( val );
		}
		else {
			pRow.value = Value.class.cast( val );
		}

		vertex.setValue( pRow.name, pRow.value );
		//JPM 2015/05/27 is the intention here to save this back to the db?
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
		return classNames[column];
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
		PropertyRow pr = rows.get( row );
		boolean ok = ( 1 == column && !pr.ro );
		return ok;
	}

	@Override
	public void itemStateChanged( ItemEvent e ) {
		rows.clear();

		if ( ItemEvent.SELECTED == e.getStateChange() ) {
			setItem( GraphElement.class.cast( e.getItem() ) );
		}
		else {
			fireTableDataChanged();
		}
	}

	protected class PropertyRow {

		public IRI name;
		public Value value;
		public final boolean ro;

		public PropertyRow( IRI name, Value value, boolean readonly ) {
			this.name = name;
			this.value = value;
			this.ro = readonly;
		}

		public PropertyRow( IRI name, Value value ) {
			this( name, value, value instanceof IRI );
		}
	}
}
