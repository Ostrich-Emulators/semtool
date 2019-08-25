/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

/**
 *
 * @author ryan
 * @param <T> type of entity we'll store labels for
 */
public class LabeledPairTableCellRenderer<T> extends DefaultTableCellRenderer {

	private static final Logger log = Logger.getLogger( LabeledPairTableCellRenderer.class );
	private final Map<T, String> labelCache = new HashMap<>();

	public Map<T, String> getCachedLabels() {
		return new HashMap<>( labelCache );
	}

	public void clearCache() {
		labelCache.clear();
	}

	public void cache( Map<T, String> map ) {
		labelCache.putAll( map );
	}

	public void cache( T u, String label ) {
		labelCache.put( u, label );
	}

	@Override
	public Component getTableCellRendererComponent( JTable table, Object value,
			boolean sel, boolean foc, int r, int c ) {

		if ( value instanceof String ) {
			// not sure how we're getting here
			return super.getTableCellRendererComponent( table, value, sel, foc, r, c );
		}

		String text = "";
		if ( null != value ) {
			T val = (T) value;

			if ( !labelCache.containsKey( val ) ) {
				cache( val, getLabelForCacheMiss( val ) );
			}

			text = labelCache.get( val );
		}

		return super.getTableCellRendererComponent( table, text, sel, foc, r, c );
	}

	protected String getLabelForCacheMiss( T val ) {
		return val.toString();
	}

	public static LabeledPairTableCellRenderer<IRI> getUriPairRenderer() {
		return new LabeledPairTableCellRenderer<IRI>() {
			@Override
			protected String getLabelForCacheMiss( IRI val ) {
				return val.getLocalName();
			}
		};
	}

	public static LabeledPairTableCellRenderer<Value> getValuePairRenderer( RetrievingLabelCache rlc ) {
		return new LabeledPairTableCellRenderer<Value>() {
			@Override
			protected String getLabelForCacheMiss( Value val ) {
				if ( null == val ) {
					return "";
				}

				return rlc.get( val );
			}
		};
	}

	public static LabeledPairTableCellRenderer<Value> getValuePairRenderer( IEngine eng ) {
		return getValuePairRenderer( new RetrievingLabelCache( eng ) );
	}
}
