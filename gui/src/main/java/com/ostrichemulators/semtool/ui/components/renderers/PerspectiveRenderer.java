/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.ui.actions.DbAction;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.apache.log4j.Logger;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;

/**
 *
 * @author ryan
 */
public class PerspectiveRenderer extends DefaultListCellRenderer {

	private static final Logger log = Logger.getLogger( PerspectiveRenderer.class );
	private final Set<Perspective> locals = new HashSet<>();

	public PerspectiveRenderer() {
	}

	@Override
	public Component getListCellRendererComponent( JList list, Object val, int idx,
			boolean sel, boolean hasfocus ) {

		if ( null == val ) {
			return super.getListCellRendererComponent( list, null, idx, sel, hasfocus );
		}

		Perspective p = Perspective.class.cast( val );
		setToolTipText( p.getDescription() );
		String label = p.getLabel();

		Component cmp = super.getListCellRendererComponent( list, label, idx,
				sel, hasfocus );

		if ( locals.contains( p ) ) {
			setIcon( DbAction.getIcon( "privacy" ) );
		}

		return cmp;
	}

	public void clear() {
		locals.clear();
	}

	public void setLocal( Perspective p, boolean local ) {
		if ( local ) {
			locals.add( p );
		}
		else {
			locals.remove( p );
		}

		invalidate();
	}
}
