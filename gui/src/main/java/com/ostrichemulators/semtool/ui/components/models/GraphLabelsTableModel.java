/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.models;

import com.ostrichemulators.semtool.om.GraphElement;
import com.ostrichemulators.semtool.ui.components.playsheets.graphsupport.SemossGraphVisualization;
import com.ostrichemulators.semtool.ui.transformer.LabelTransformer;
import com.ostrichemulators.semtool.ui.transformer.TooltipTransformer;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.PropComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <V>
 */
public class GraphLabelsTableModel<V extends GraphElement> extends AbstractTableModel {

	private static final Logger log = Logger.getLogger( GraphLabelsTableModel.class );
	private static final PropComparator PROPCOMP = new PropComparator();
	private static final Class<?>[] ROWCLASSES = { URI.class, URI.class, Boolean.class,
		Boolean.class, String.class };
	private final List<ControlDataRow> data = new ArrayList<>();
	private final String[] columnNames;
	private LabelTransformer<V> texter;
	private TooltipTransformer<V> tooltipper;
	private SemossGraphVisualization viz;

	public GraphLabelsTableModel( String... _columnNames ) {
		columnNames = _columnNames;
	}

	public void refresh( Collection<V> instances, SemossGraphVisualization vizzy ) {
		data.clear();
		viz = vizzy;

		MultiMap<URI, URI> propmap = new MultiMap<>(); // type -> possible properties
		for ( V v : instances ) {
			Set<URI> keys = v.getPropertyKeys();
			if ( propmap.containsKey( v.getType() ) ) {
				keys.addAll( propmap.get( v.getType() ) );
			}
			propmap.put( v.getType(), new ArrayList<>( keys ) );
		}

		for ( Map.Entry<URI, List<URI>> en : propmap.entrySet() ) {
			data.add( new ControlDataRow( en.getKey(), Constants.ANYNODE ) );

			for ( URI prop : en.getValue() ) {
				ControlDataRow row = new ControlDataRow( en.getKey(), prop );
				data.add( row );
			}
		}

		Collections.sort( data );

		fireTableDataChanged();
	}

	public void setLabelers( LabelTransformer<V> txt, TooltipTransformer<V> tt ) {
		tooltipper = tt;
		texter = txt;
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
		return data.size();
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
		ControlDataRow cdr = data.get( row );
		switch ( column ) {
			case 0: {
				if ( Constants.ANYNODE.equals( cdr.prop ) ) {
					return cdr.type;
				}
				return "";
			}
			case 1:
				return cdr.prop;
			case 2:
			case 3: {
				LabelTransformer lt = ( 2 == column ? texter : tooltipper );
				List<URI> props = lt.getDisplayableProperties( cdr.type );
				if ( cdr.isHeader() ) {
					return ( this.getInstanceRows( cdr.type ).size() == props.size() );
				}
				else {
					return props.contains( cdr.prop );
				}
			}
			default:
				return null;
		}
	}

	private List<ControlDataRow> getInstanceRows( URI type ) {
		List<ControlDataRow> rows = new ArrayList<>();
		for ( ControlDataRow cdr : data ) {
			if ( cdr.type.equals( type ) && !cdr.isHeader() ) {
				rows.add( cdr );
			}
		}

		return rows;
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
		return ROWCLASSES[column];
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
		return ( column == 2 || column == 3 );
	}

	/**
	 * Sets the label value at a particular row and column index.
	 *
	 * @param value Label value.
	 * @param row Row index.
	 * @param column Column index.
	 */
	@Override
	public void setValueAt( Object value, int row, int column ) {
		Boolean showit = Boolean.class.cast( value );
		ControlDataRow cdr = data.get( row );
		LabelTransformer<V> transformer = ( 2 == column ? texter : tooltipper );

		if ( cdr.isHeader() ) {
			Set<URI> tochange = new HashSet<>();
			for ( ControlDataRow inst : getInstanceRows( cdr.type ) ) {
				tochange.add( inst.prop );
			}

			if ( showit ) {
				transformer.setDisplay( cdr.type, tochange );
			}
			else {
				transformer.setDisplay( cdr.type, new ArrayList<>() );
			}
		}
		else {
			transformer.setDisplay( cdr.type, cdr.prop, showit );
		}

		fireTableDataChanged();
		viz.repaint();
	}

	public static class ControlDataRow implements Comparable<ControlDataRow> {

		URI type;
		URI prop;
		String other;

		public ControlDataRow( URI type, URI prop ) {
			this.type = type;
			this.prop = prop;
		}

		public boolean isHeader() {
			return ( null == type || Constants.ANYNODE.equals( prop ) );
		}

		@Override
		public int compareTo( ControlDataRow o ) {
			if ( type.equals( o.type ) ) {
				if ( isHeader() ) {
					return -1;
				}
				if ( o.isHeader() ) {
					return 1;
				}

				return PROPCOMP.compare( prop, o.prop );
			}
			return type.stringValue().compareTo( o.type.stringValue() );
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 71 * hash + Objects.hashCode( this.type );
			hash = 71 * hash + Objects.hashCode( this.prop );
			return hash;
		}

		@Override
		public boolean equals( Object obj ) {
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			final ControlDataRow cdr = (ControlDataRow) obj;
			if ( !Objects.equals( this.type, cdr.type ) ) {
				return false;
			}
			if ( !Objects.equals( this.prop, cdr.prop ) ) {
				return false;
			}
			return true;
		}
	}
}
