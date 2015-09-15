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
import javax.swing.JList;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.util.DefaultPlaySheetIcons;
import gov.va.semoss.util.PlaySheetEnum;

/**
 *
 * @author ryan
 */
public class QuestionRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger( QuestionRenderer.class );
	private final Map<URI, String> nameCache = new HashMap<>();
	private InsightManager engine;
	private Perspective perspective;

	public QuestionRenderer() {
	}

	public void setEngine( IEngine eng ) {
		engine = ( null == eng ? null : eng.getInsightManager() );
		nameCache.clear();
	}

	public void setPerspective( Perspective p ) {
		perspective = p;
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val, int idx,
			boolean sel, boolean hasfocus ) {

		Insight insight = ( null == val ? null : Insight.class.cast( val ) );
		String text = ( null == insight ? "" : perspective.getOrderedLabel( insight ) );

		super.getListCellRendererComponent( list, text, idx, sel, hasfocus );

		Icon icon = DefaultPlaySheetIcons.getDefaultIcon( PlaySheetEnum.valueFor( insight) );
		if( null == icon ){
			setIcon( null );
		}
		else{
			setIcon( icon );
		}

		if ( insight != null && insight.getLabel() != null ) {
			setToolTipText( insight.getLabel() );
		}

		return this;
	}
}
