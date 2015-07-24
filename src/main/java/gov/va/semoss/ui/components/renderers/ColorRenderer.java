/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import gov.va.semoss.ui.helpers.GraphColorRepository;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author ryan
 */
public class ColorRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val,
			int index, boolean sel, boolean focus ) {

		if ( null == val ) {
			return super.getListCellRendererComponent( list, val, index, sel, focus );
		}

		Color color = Color.class.cast( val );
		val = GraphColorRepository.instance().getColorName( color );
		super.getListCellRendererComponent( list, val, index, sel, focus );
		TableColorRenderer.colorify( this, color );

		return this;
	}
}
