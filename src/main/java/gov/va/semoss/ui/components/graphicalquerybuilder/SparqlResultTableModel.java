/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class SparqlResultTableModel extends AbstractTableModel {

	private static final String[] COLS
			= { "Node", "Property", "Label", "Returned?" };
	private static final Class<?>[] COLCLASSES
			= { AbstractNodeEdgeBase.class, URI.class, String.class, Boolean.class };

	private final MultiMap<? extends AbstractNodeEdgeBase, SparqlResultConfig> data;
	private final List<SparqlResultConfig> list = new ArrayList<>();

	public SparqlResultTableModel( MultiMap<? extends AbstractNodeEdgeBase, SparqlResultConfig> data ) {
		this.data = data;
		for ( Map.Entry<? extends AbstractNodeEdgeBase, List<SparqlResultConfig>> en : data.entrySet() ) {
			for ( SparqlResultConfig src : en.getValue() ) {
				list.add( src );
			}
		}

		Collections.sort( list );
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return COLS.length;
	}

	@Override
	public boolean isCellEditable( int row, int col ) {
		return ( col > 1 );
	}

	@Override
	public Class<?> getColumnClass( int col ) {
		return COLCLASSES[col];
	}

	@Override
	public String getColumnName( int column ) {
		return COLS[column];
	}

	@Override
	public Object getValueAt( int row, int col ) {
		SparqlResultConfig src = list.get( row );
		switch ( col ) {
			case 0:
				return src.getId();
			case 1:
				return src.getProperty();
			case 2:
				return src.getLabel();
			case 3:
				return ( !( null == src.getLabel() || src.getLabel().isEmpty() ) );
			default:
				throw new IllegalArgumentException( "unknown column: " + col );
		}
	}

	@Override
	public void setValueAt( Object aValue, int row, int col ) {
		SparqlResultConfig src = list.get( row );
		switch ( col ) {
			case 2:
				src.setLabel( aValue.toString() );
				break;
			default:
				throw new IllegalArgumentException( "unknown column: " + col );
		}

		fireTableCellUpdated( row, col );
	}
}
