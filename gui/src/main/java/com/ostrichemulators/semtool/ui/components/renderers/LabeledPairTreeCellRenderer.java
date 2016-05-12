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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 * @param <T> type of entity we'll store labels for
 */
public class LabeledPairTreeCellRenderer<T> extends DefaultTreeCellRenderer {

	private static final Logger log = Logger.getLogger( LabeledPairTreeCellRenderer.class );
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
	public Component getTreeCellRendererComponent( JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int r, boolean foc ) {

		if ( value instanceof DefaultMutableTreeNode ) {
			value = DefaultMutableTreeNode.class.cast( value ).getUserObject();
		}

		if ( value instanceof String ) {
			// not sure how we're getting here
			return super.getTreeCellRendererComponent( tree, value, sel, expanded,
					leaf, r, foc );
		}

		String text = "";
		if ( null != value ) {

			T val = (T) value;

			if ( !labelCache.containsKey( val ) ) {
				cache( val, getLabelForCacheMiss( val ) );
			}

			text = labelCache.get( val );
		}

		return super.getTreeCellRendererComponent( tree, text, sel, expanded,
				leaf, r, foc );
	}

	protected String getLabelForCacheMiss( T val ) {
		return val.toString();
	}

	public static LabeledPairTreeCellRenderer<URI> getUriPairRenderer() {
		return new LabeledPairTreeCellRenderer<URI>() {
			@Override
			protected String getLabelForCacheMiss( URI val ) {
				return val.getLocalName();
			}
		};
	}

	public static LabeledPairTreeCellRenderer<Value> getValuePairRenderer( IEngine eng ) {
		return getValuePairRenderer( new RetrievingLabelCache( eng ) );
	}

	public static LabeledPairTreeCellRenderer<Value> getValuePairRenderer( RetrievingLabelCache rlc ) {
		return new LabeledPairTreeCellRenderer<Value>() {
			@Override
			protected String getLabelForCacheMiss( Value val ) {
				if ( null == val ) {
					return "";
				}

				return rlc.get( val );
			}
		};
	}
}
