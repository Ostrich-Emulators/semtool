/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import static cern.clhep.Units.s;
import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class SparqlResultTableModel extends AbstractTableModel {

	private static final String[] COLS
			= { "Node", "Property", "Label", "Returned?" };
	private static final Class<?>[] COLCLASSES
			= { String.class, URI.class, String.class, Boolean.class };

	private final MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> data;
	private final List<SparqlResultConfig> list = new ArrayList<>();
	private final Map<AbstractNodeEdgeBase, String> subjects = new HashMap<>();

	public SparqlResultTableModel( MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> data ) {
		this.data = data;

		for ( Map.Entry<? extends AbstractNodeEdgeBase, List<SparqlResultConfig>> en : data.entrySet() ) {
			for ( SparqlResultConfig src : en.getValue() ) {
				if ( src.getProperty().equals( RDF.SUBJECT ) ) {
					subjects.put( src.getId(), src.getLabel() );
				}
				else {
					list.add( src );
				}
			}
		}

		Collections.sort( list );
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return COLS.length;
	}

	@Override
	public boolean isCellEditable( int row, int col ) {
		return ( 0 == col || 2 == col );
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
				return subjects.get( src.getId() );
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
		if ( 0 == col ) {
			subjects.put( src.getId(), aValue.toString() );
			for ( SparqlResultConfig ss : data.getNN( src.getId() ) ) {
				if ( ss.getProperty().equals( RDF.SUBJECT ) ) {
					ss.setLabel( aValue.toString() );
				}
			}
		}
		else if ( 2 == col ) {
			src.setLabel( aValue.toString() );
		}

		fireTableCellUpdated( row, col );
	}
}
