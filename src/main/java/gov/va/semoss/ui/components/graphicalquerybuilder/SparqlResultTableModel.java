/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.ui.components.graphicalquerybuilder.GraphicalQueryPanel.QueryOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
			= { "Node", "Property", "Value", "Label", "Returned?", "Optional?", "Filter" };
	private static final Class<?>[] COLCLASSES
			= { String.class, URI.class, Value.class, String.class, Boolean.class,
				Boolean.class, String.class };
	private final List<QueryOrder> list = new ArrayList<>();

	public SparqlResultTableModel( List<QueryGraphElement> elements,
			List<QueryOrder> ordering ) {
		
		Set<QueryOrder> seen = new HashSet<>( ordering );
		list.addAll( ordering );

		for( QueryGraphElement element : elements ){
			for ( URI prop : element.getAllValues().keySet() ) {
				if ( !RDF.SUBJECT.equals( prop ) ) {
					QueryOrder qo = new QueryOrder( element, prop );
					if( !seen.contains( qo )){
						list.add( qo );
					}
				}
			}
		}		
	}
	
	public List<QueryOrder> getQueryOrdering(){
		return list;
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
		QueryOrder src = list.get( row );
		URI property = src.property;
		QueryGraphElement base = src.base;

		switch ( col ) {
			case 0:
				return base.getQueryId();
			case 1:
				return property;
			case 2:
				return base.getValue( property );
			case 3:
				return ( base.isSelected( property ) ? base.getLabel( property ) : "" );
			case 4:
				return base.isSelected( property );
			case 5:
				return base.isOptional( property );
			case 6:
				return base.getFilter( property );
			default:
				throw new IllegalArgumentException( "unknown column: " + col );
		}
	}

	@Override
	public void setValueAt( Object aValue, int row, int col ) {
		QueryOrder src = list.get( row );
		QueryGraphElement base = src.base;

		switch ( col ) {
			case 0:
				base.setQueryId( aValue.toString() );
				fireTableDataChanged();
				break;
			case 2:
				base.setProperty( src.property, Value.class.cast( aValue ) );
				fireTableCellUpdated( row, col );
				break;
			case 3:
				base.setLabel( src.property, aValue.toString() );
				fireTableCellUpdated( row, col );
				break;
			case 4:
				base.setSelected( src.property, Boolean.class.cast( aValue ) );
				fireTableDataChanged();
				break;
			case 5:
				base.setOptional( src.property, Boolean.class.cast( aValue ) );
				fireTableDataChanged();
				break;
			case 6:
				base.setFilter( src.property, aValue.toString() );
				fireTableDataChanged();
				break;
			default:
				throw new IllegalArgumentException( "Cannot edit column " + col );
		}
	}

	public QueryOrder getRawRow( int row ) {
		return list.get( row );
	}

	public void swap( int fromRow, int toRow ) {
		if ( toRow > -1 && toRow < list.size() ) {
			QueryOrder rl = list.remove( fromRow );
			list.add( toRow, rl );

			fireTableDataChanged();
		}
	}

	public static class RowLocator implements Comparable<RowLocator> {

		public final QueryGraphElement base;
		public final URI property;

		public RowLocator( QueryGraphElement base, URI prop ) {
			this.base = base;
			this.property = prop;
		}

		@Override
		public int compareTo( RowLocator o ) {
			int diff = base.getURI().stringValue().compareTo( o.base.getURI().stringValue() );
			if ( 0 == diff ) {
				diff = property.stringValue().compareTo( o.property.stringValue() );
			}

			return diff;
		}
	}
}
