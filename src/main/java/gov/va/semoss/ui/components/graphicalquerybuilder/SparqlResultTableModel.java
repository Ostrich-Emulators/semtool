/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.graphicalquerybuilder;

import gov.va.semoss.om.AbstractNodeEdgeBase;
import gov.va.semoss.ui.components.models.ValueTableModel;
import gov.va.semoss.util.MultiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
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

	private final MultiMap<AbstractNodeEdgeBase, SparqlResultConfig> data;
	private final List<SparqlResultConfig> list = new ArrayList<>();
	private final Map<AbstractNodeEdgeBase, String> subjects = new HashMap<>();
	private final ValueFactory vf = new ValueFactoryImpl();

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
		SparqlResultConfig src = list.get( row );
		URI property = src.getProperty();

		switch ( col ) {
			case 0:
				return subjects.get( src.getId() );
			case 1:
				return property;
			case 2:
				return ValueTableModel.getValueFromObject( src.getId().getProperty( property ) );
			case 3: {
				if ( src.getId().isMarked( property ) ) {
					return src.getLabel();
				}
				else {
					return "";
				}
			}
			case 4:
				return src.getId().isMarked( property );
			case 5:
				return src.isOptional();
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

			fireTableDataChanged();
		}
		else if ( 2 == col ) {
			log.debug( aValue );
			if ( aValue instanceof URI ) {
				src.getId().setProperty( src.getProperty(), URI.class.cast( aValue ) );
			}
			else {
				src.getId().setProperty( src.getProperty(),
						ValueTableModel.getValueFromLiteral( Literal.class.cast( aValue ) ) );
			}

			fireTableCellUpdated( row, col );
		}
		else if ( 3 == col ) {
			src.setLabel( aValue.toString() );
			fireTableCellUpdated( row, col );
		}
		else if ( 4 == col ) {
			src.getId().mark( src.getProperty(), Boolean.class.cast( aValue ) );
			fireTableDataChanged();
		}
		else if ( 5 == col ) {
			src.setOptional( Boolean.class.cast( aValue ) );
			fireTableDataChanged();
		}
	}
}
