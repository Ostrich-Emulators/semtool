/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.RetrievingLabelCache;
import com.ostrichemulators.semtool.util.Utility;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
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
public class LabeledPairRenderer<T> extends DefaultListCellRenderer {

	private static final Logger log = Logger.getLogger( LabeledPairRenderer.class );
	private final Map<T, String> labelCache = new HashMap<>();
	private boolean isfetching = false;

	public boolean isIsfetching() {
		return isfetching;
	}

	public void setIsfetching( boolean isfetching ) {
		this.isfetching = isfetching;
	}

	public Map<T, String> getCachedLabels() {
		return new HashMap<>( labelCache );
	}

	public LabeledPairRenderer<T> cache( Map<T, String> map ) {
		labelCache.putAll( map );
		return this;
	}

	public LabeledPairRenderer<T> cache( T u, String label ) {
		labelCache.put( u, label );
		return this;
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object value,
			int idx, boolean sel, boolean focused ) {

		if ( value instanceof String ) {
			// not sure how we're getting here
			return super.getListCellRendererComponent( list, value, idx, sel, focused );
		}

		String text = ( isfetching ? "fetching" : "" );
		if ( null != value ) {
			T val = (T) value;

			if ( !labelCache.containsKey( val ) ) {
				cache( val, getLabelForCacheMiss( val ) );
			}

			text = labelCache.get( val );
		}

		return super.getListCellRendererComponent( list, text, idx, sel, focused );
	}

	protected String getLabelForCacheMiss( T val ) {
		return val.toString();
	}

	public static LabeledPairRenderer<URI> getUriPairRenderer() {
		return new LabeledPairRenderer<URI>() {
			@Override
			protected String getLabelForCacheMiss( URI val ) {
				return val.getLocalName();
			}
		};
	}

	public void clearCache() {
		labelCache.clear();
	}

	public static LabeledPairRenderer<Value> getValuePairRenderer( IEngine eng ) {
		return new LabeledPairRenderer<Value>() {
			@Override
			protected String getLabelForCacheMiss( Value val ) {
				if ( null == val ) {
					return "";
				}

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

	public static LabeledPairRenderer<Value> getValuePairRenderer( RetrievingLabelCache rlc ) {
		return new LabeledPairRenderer<Value>() {
			@Override
			protected String getLabelForCacheMiss( Value val ) {
				if ( null == val ) {
					return "";
				}

				return rlc.get( val );
			}
		};
	}

	public static LabeledPairRenderer<URI> getUriPairRenderer( IEngine eng ) {
		return new LabeledPairRenderer<URI>() {
			@Override
			protected String getLabelForCacheMiss( URI val ) {
				if ( null == val ) {
					return "";
				}

				URI uri = URI.class.cast( val );
				String ret = ( null == eng ? uri.getLocalName()
						: Utility.getInstanceLabel( Resource.class.cast( val ), eng ) );
				cache( val, ret );
				return ret;
			}
		};
	}
}
