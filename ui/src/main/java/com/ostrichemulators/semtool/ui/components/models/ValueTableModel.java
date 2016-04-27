/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.models;

import static com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter.getDate;
import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import com.ostrichemulators.semtool.util.MultiMap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
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
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class ValueTableModel extends AbstractTableModel {

	public static final String ALLOW_INSERT = "allowinsert";
	public static final String NEEDS_SAVE = "savesetting";
	public static final String READ_ONLY = "readonly";

	private static final Logger log = Logger.getLogger( ValueTableModel.class );
	private static final long serialVersionUID = 7491106662313232478L;
	private static final String EVERYTHING = "everything"; // for prop listeners
	private static final Map<URI, Class<?>> TYPELOOKUP = new HashMap<>();
	private static final Map<Class<?>, URI> DATATYPELOOKUP = new HashMap<>();
	private static final ValueFactory VF = new ValueFactoryImpl();

	private final List<Object[]> data = new ArrayList<>();
	private final List<String> headers = new ArrayList<>();
	private final List<Class<?>> columnClasses = new ArrayList<>();

	static {

		DATATYPELOOKUP.put( Integer.class, XMLSchema.INT );
		DATATYPELOOKUP.put( Double.class, XMLSchema.DOUBLE );
		DATATYPELOOKUP.put( Float.class, XMLSchema.FLOAT );
		DATATYPELOOKUP.put( String.class, XMLSchema.STRING );
		DATATYPELOOKUP.put( Date.class, XMLSchema.DATETIME );
		DATATYPELOOKUP.put( Boolean.class, XMLSchema.BOOLEAN );
	}

	private boolean useraw;
	private boolean readonly;
	private boolean allowInsertsInPlace = false;
	private boolean saveme = false;
	private final MultiMap<String, PropertyChangeListener> listeners = new MultiMap<>();

	public ValueTableModel() {
		this( true );
	}

	public ValueTableModel( boolean raw ) {
		useraw = raw;
	}

	public ValueTableModel( List<Value[]> newdata, List<String> heads ) {
		setData( newdata, heads );
	}

	protected void clear() {
		int ds = data.size();
		data.clear();
		fireTableRowsDeleted( 0, ds );
	}

	public void addPropertyChangeListener( PropertyChangeListener pl ) {
		listeners.add( EVERYTHING, pl );
	}

	public void addPropertyChangeListener( String prop, PropertyChangeListener pl ) {
		listeners.add( prop, pl );
	}

	public void removePropertyChangeListener( PropertyChangeListener pl ) {
		listeners.getNN( EVERYTHING ).remove( pl );
	}

	public void removePropertyChangeListener( String prop, PropertyChangeListener pl ) {
		listeners.getNN( prop ).remove( pl );
	}

	/**
	 * Permits this model to allow an empty row at the bottom where the user can
	 * insert new rows of data, like old MS Access tables
	 *
	 * @param b allow?
	 */
	public void setAllowInsertsInPlace( boolean b ) {
		PropertyChangeEvent pce
				= new PropertyChangeEvent( this, ALLOW_INSERT, allowInsertsInPlace, b );

		allowInsertsInPlace = b;

		if ( !pce.getNewValue().equals( pce.getOldValue() ) ) {
			for ( PropertyChangeListener pcl : listeners.getNN( EVERYTHING ) ) {
				pcl.propertyChange( pce );
			}
			for ( PropertyChangeListener pcl : listeners.getNN( ALLOW_INSERT ) ) {
				pcl.propertyChange( pce );
			}
		}
	}

	/**
	 * Does this model support in-place data adds. Note that if
	 * {@link #isReadOnly() } returns <code>false</code>, so will this function.
	 *
	 * @return if inserts are allowed
	 */
	public boolean allowsInsertsInPlace() {
		return ( isReadOnly() ? false : allowInsertsInPlace );
	}

	/**
	 * Sets the table to readonly/read-write. If
	 *
	 * @param b
	 */
	public void setReadOnly( boolean b ) {
		PropertyChangeEvent pce
				= new PropertyChangeEvent( this, READ_ONLY, readonly, b );

		readonly = b;

		if ( !pce.getNewValue().equals( pce.getOldValue() ) ) {
			for ( PropertyChangeListener pcl : listeners.getNN( EVERYTHING ) ) {
				pcl.propertyChange( pce );
			}
			for ( PropertyChangeListener pcl : listeners.getNN( READ_ONLY ) ) {
				pcl.propertyChange( pce );
			}
		}
	}

	public boolean isReadOnly() {
		return readonly;
	}

	public final void addData( List<Value[]> newdata ) {
		int sz = data.size();

		if ( !newdata.isEmpty() ) {
			data.addAll( convertValuesToClassedData( newdata, columnClasses, useraw ) );
			int newsz = data.size();
			fireTableRowsInserted( sz - 1, newsz - 1 );
		}
	}

	public void setData( List<Value[]> newdata, List<String> heads ) {
		headers.clear();
		headers.addAll( heads );
		columnClasses.clear();
		columnClasses.addAll( RDFDatatypeTools.figureColumnClassesFromData( newdata, headers.size() ) );

		data.clear();
		data.addAll( convertValuesToClassedData( newdata, columnClasses, useraw ) );
		fireTableStructureChanged();
	}

	public void setHeaders( Map<String, URI> heads ) {
		headers.clear();
		columnClasses.clear();

		for ( Map.Entry<String, URI> en : heads.entrySet() ) {
			headers.add( en.getKey() );
			columnClasses.add( TYPELOOKUP.get( en.getValue() ) );
		}
		fireTableStructureChanged();
	}

	public void setHeaders( List<String> heads ) {
		headers.clear();
		headers.addAll( heads );
		fireTableStructureChanged();
	}

	public void setHeaders( Object[] heads ) {
		headers.clear();
		for ( Object o : heads ) {
			headers.add( o.toString() );
		}
	}

	@Override
	public int getRowCount() {
		int sz = data.size();
		return ( allowsInsertsInPlace() ? sz + 1 : sz );
	}

	/**
	 * Same as {@link #getRowCount()}, but be sure to ignore extra row if
	 * {@link #allowsInsertsInPlace()} returns <code>true</code>
	 *
	 * @return
	 */
	public int getRealRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return headers.size();
	}

	@Override
	public Object getValueAt( int r, int c ) {
		if ( isInsertRow( r ) ) {
			return ( 0 == c ? "*" : null );
		}

		return data.get( r )[c];
	}

	public Value getRdfValueAt( int r, int c ) {
		Object val = getValueAt( r, c );
		if ( null == val ) {
			return null;
		}

		String valstr = val.toString();
		Class<?> klass = getColumnClass( c );
		URI datatype = DATATYPELOOKUP.get( klass );
		return ( null == datatype ? VF.createLiteral( valstr )
				: VF.createLiteral( valstr, datatype ) );
	}

	@Override
	public boolean isCellEditable( int row, int col ) {
		if ( isInsertRow( row ) ) {
			return ( 0 == col );
		}

		return !readonly;
	}

	/**
	 * Is this the row for inserting new values?
	 *
	 * @param row the row to check
	 * @return true, if it's the insert row
	 */
	protected final boolean isInsertRow( int row ) {
		return ( allowsInsertsInPlace() && getRowCount() - 1 == row );
	}

	@Override
	public void setValueAt( Object aValue, int r, int c ) {
		Class<?> k = getColumnClass( c );

		boolean isinsert = isInsertRow( r );

		if ( isinsert ) {
			Object objs[] = new Object[getColumnCount()];
			data.add( objs );
		}

		data.get( r )[c] = k.cast( aValue );

		setNeedsSave( true );

		if ( isinsert ) {
			fireTableRowsInserted( r, r );
		}
		else {
			fireTableCellUpdated( r, c );
		}
	}

	public void setNeedsSave( boolean b ) {
		PropertyChangeEvent pce
				= new PropertyChangeEvent( this, NEEDS_SAVE, saveme, b );

		saveme = b;

		if ( !pce.getNewValue().equals( pce.getOldValue() ) ) {
			for ( PropertyChangeListener pcl : listeners.getNN( EVERYTHING ) ) {
				pcl.propertyChange( pce );
			}

			for ( PropertyChangeListener pcl : listeners.getNN( NEEDS_SAVE ) ) {
				pcl.propertyChange( pce );
			}
		}
	}

	public boolean needsSave() {
		return saveme;
	}

	@Override
	public Class<?> getColumnClass( int c ) {
		return ( c < columnClasses.size() ? columnClasses.get( c ) : Object.class );
	}

	@Override
	public String getColumnName( int column ) {
		return headers.get( column );
	}

	

	private static List<Object[]> convertValuesToClassedData( Collection<Value[]> newdata,
			List<Class<?>> columnClasses, boolean userawstrings ) {

		List<Object[]> data = new ArrayList<>();
		for ( Value[] varr : newdata ) {
			try {
				Object[] arr = new Object[varr.length];
				for ( int i = 0; i < varr.length; i++ ) {
					final Class<?> cc = columnClasses.get( i );
					Value val = varr[i];
					if ( null == val ) {
						arr[i] = null;
					}
					else if ( URI.class == cc ) {
						arr[i] = URI.class.cast( val );
					}
					else if ( Object.class == cc ) {
						arr[i] = val.stringValue();
					}
					else {
						if ( Integer.class == cc ) {
							Literal l = Literal.class.cast( val );
							arr[i] = l.intValue();
						}
						else if ( Boolean.class == cc ) {
							Literal l = Literal.class.cast( val );
							arr[i] = l.booleanValue();
						}
						else if ( Date.class == cc ) {
							Literal l = Literal.class.cast( val );
							arr[i] = getDate( l.calendarValue() );
						}
						else if ( Double.class == cc ) {
							Literal l = Literal.class.cast( val );
							arr[i] = l.doubleValue();
						}
						else if ( Float.class == cc ) {
							Literal l = Literal.class.cast( val );
							arr[i] = l.floatValue();
						}
						else if ( String.class == cc ) {
							// just because our column class is "String" doesn't mean the
							// val is a String or even a literal; it just means the output
							// should be of type String

							if ( val instanceof Literal ) {
								Literal l = Literal.class.cast( val );

								if ( userawstrings ) {
									if ( null != l.getLanguage() ) {
										arr[i] = "\"" + l.stringValue() + "\"@" + l.getLanguage();
									}
									else if ( null != l.getDatatype() ) {
										arr[i] = "\"" + l.stringValue() + "\"^^" + l.getDatatype().stringValue();
									}
									else {
										arr[i] = l.stringValue();
									}
								}
								else {
									arr[i] = val.stringValue();
								}
							}
							else {
								arr[i] = val.stringValue();
							}
						}
					}
				}
				data.add( arr );
			}
			catch ( Exception e ) {
				Logger.getLogger( ValueTableModel.class ).error( e, e );
			}
		}

		return data;
	}

	
}
