/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Utility;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

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

	public static LabeledPairTableCellRenderer<URI> getUriPairRenderer() {
		return new LabeledPairTableCellRenderer<URI>() {
			@Override
			protected String getLabelForCacheMiss( URI val ) {
				return val.getLocalName();
			}
		};
	}

	public static LabeledPairTableCellRenderer<Value> getValuePairRenderer( IEngine eng ) {
		return new LabeledPairTableCellRenderer<Value>() {
			@Override
			protected String getLabelForCacheMiss( Value val ) {
				String ret;
				if ( val instanceof URI ) {
					URI uri = URI.class.cast( val );
					ret = ( null == eng ? uri.getLocalName()
							: Utility.getInstanceLabel( Resource.class.cast( val ), eng ) );
					cache( val, ret );
				}
				else if ( val instanceof Literal ) {
					ret = Literal.class.cast( val ).getLabel();
				}
				else {
					ret = val.stringValue();
				}
				return ret;
			}
		};
	}
}
