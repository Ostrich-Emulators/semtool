/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author ryan
 */
public class SparqlResultTableModel extends AbstractTableModel {

	private static final Logger log = Logger.getLogger( SparqlResultTableModel.class );
	private static final String[] COLS
			= { "Node", "Property", "Value", "Label", "Returned?", "Optional?" };
	private static final Class<?>[] COLCLASSES
			= { String.class, URI.class, Value.class, String.class, Boolean.class,
				Boolean.class };
	private final List<RowLocator> list = new ArrayList<>();

	public SparqlResultTableModel( List<QueryNodeEdgeBase> elements ) {
		for ( QueryNodeEdgeBase element : elements ) {
			for ( URI prop : element.getAllValues().keySet() ) {
				if ( !RDF.SUBJECT.equals( prop ) ) {
					list.add( new RowLocator( element, prop ) );
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
		return ( 1 != col );
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
		RowLocator src = list.get( row );
		URI property = src.property;
		QueryNodeEdgeBase base = src.base;

		switch ( col ) {
			case 0:
				return base.getQueryId();
			case 1:
				return property;
			case 2:
				return base.getValue( property );
			case 3: {
				if ( base.isSelected( property ) ) {
					return base.getLabel( property );
				}
				else {
					return "";
				}
			}
			case 4:
				return base.isSelected( property );
			case 5:
				return base.isOptional( property );
			default:
				throw new IllegalArgumentException( "unknown column: " + col );
		}
	}

	@Override
	public void setValueAt( Object aValue, int row, int col ) {
		RowLocator src = list.get( row );
		QueryNodeEdgeBase base = src.base;

		if ( 0 == col ) {
			base.setQueryId( aValue.toString() );
			fireTableDataChanged();
		}
		else if ( 2 == col ) {
			log.debug( aValue );
			base.setProperty( src.property, Value.class.cast( aValue ) );
			fireTableCellUpdated( row, col );
		}
		else if ( 3 == col ) {
			base.setLabel( src.property, aValue.toString() );
			fireTableCellUpdated( row, col );
		}
		else if ( 4 == col ) {
			base.setSelected( src.property, Boolean.class.cast( aValue ) );
			fireTableDataChanged();
		}
		else if ( 5 == col ) {
			base.setOptional( src.property, Boolean.class.cast( aValue ) );
			fireTableDataChanged();
		}
	}

	public RowLocator getRawRow( int row ) {
		return list.get( row );
	}

	public static class RowLocator implements Comparable<RowLocator> {

		public final QueryNodeEdgeBase base;
		public final URI property;

		public RowLocator( QueryNodeEdgeBase base, URI prop ) {
			this.base = base;
			this.property = prop;
		}

		@Override
		public int compareTo( RowLocator o ) {
			int diff = base.getURI().stringValue().compareTo( o.base.getURI().stringValue() );
			if ( 0 == diff ) {
				return property.stringValue().compareTo( o.property.stringValue() );
			}

			return diff;
		}
	}
}
