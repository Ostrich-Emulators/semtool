/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.util.DefaultPlaySheetIcons;

/**
 *
 * @author ryan
 */
public class QuestionRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger( QuestionRenderer.class );
	private final Map<Insight, Icon> iconCache = new HashMap<>();
	private final Map<URI, String> nameCache = new HashMap<>();
	private InsightManager engine;
	private URI pUri;

	public QuestionRenderer() {
	}

	public void setEngine( IEngine eng ) {
		engine = ( null == eng ? null : eng.getInsightManager() );
		iconCache.clear();
		nameCache.clear();
	}

	public void setPerspective( Perspective p ) {
		pUri = p.getUri();
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val, int idx,
			boolean sel, boolean hasfocus ) {

		// TODO: The getOrderedLabel calls must be updated to use the perspective URI as an argument
		Insight insight = ( null == val ? null : Insight.class.cast( val ) );
		String text = ( null == insight ? "" : insight.getOrderedLabel() );

		super.getListCellRendererComponent( list, text, idx, sel, hasfocus );

		if ( !iconCache.containsKey( insight ) ) {
			Icon icon = null;
			String qtext = ( null == insight ? "" : insight.getOrderedLabel( pUri ) );

			if ( null == engine || null == insight ) {
				iconCache.put( insight, null == icon ? DefaultPlaySheetIcons.blank
						: icon );
			}
			else {
				String output = insight.getOutput();
				// if we recognize the playsheet class, use the specified icon
				if ( DefaultPlaySheetIcons.defaultIcons.containsKey( output ) ) {
					icon = DefaultPlaySheetIcons.defaultIcons.get( output );
				}
				else {
          // couldn't find the right playsheet class, so see if we can
					// figure out the icon based on the question label
					for ( Map.Entry<String, ImageIcon> e : DefaultPlaySheetIcons.defaultIcons.entrySet() ) {
						if ( qtext.contains( e.getKey() ) ) {
							ImageIcon ii = e.getValue();
							icon = ii;
						}
					}
				}
			}

			iconCache.put( insight, null == icon ? DefaultPlaySheetIcons.blank : icon );
		}

		setIcon( iconCache.get( insight ) );
		
		if (insight != null && insight.getLabel() != null)
			setToolTipText(insight.getLabel());

		return this;
	}
}
