/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.om.InsightOutputType;
import com.ostrichemulators.semtool.ui.components.OutputTypeRegistry;
import com.ostrichemulators.semtool.util.GuiUtility;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.apache.log4j.Logger;

/**
 * Renders icon + text for items in the Playsheet ComboBox, of the "Custom
 * Sparql Query" window.
 *
 * @author Thomas
 */
public class PlaySheetEnumRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger( PlaySheetEnumRenderer.class );
	private final OutputTypeRegistry registry;

	public PlaySheetEnumRenderer( OutputTypeRegistry reg ) {
		registry = reg;
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val, int idx,
			boolean sel, boolean hasfocus ) {

		if ( null == val ) {
			ImageIcon icon = GuiUtility.loadImageIcon( "icons16/questions_update2_16.png" );
			super.getListCellRendererComponent( list, "Update Query", idx,
					sel, hasfocus );
			setIcon( icon );
		}
		else {
			InsightOutputType pse = InsightOutputType.valueOf( val.toString() );
			super.getListCellRendererComponent( list, registry.getSheetName( pse ), idx,
					sel, hasfocus );
			setIcon( registry.getSheetIcon( pse ) );
		}
		return this;
	}
}
