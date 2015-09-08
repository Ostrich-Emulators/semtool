/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import gov.va.semoss.ui.components.api.IPlaySheet;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import org.apache.log4j.Logger;

import gov.va.semoss.util.DefaultPlaySheetIcons;
import gov.va.semoss.util.PlaySheetEnum;

/**
 * Renders icon + text for items in the Playsheet ComboBox, of the "Custom
 * Sparql Query" window.
 *
 * @author Thomas
 */
public class PlaySheetEnumRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger( PlaySheetEnumRenderer.class );

	public PlaySheetEnumRenderer() {
	}

	@Override
	public Component getListCellRendererComponent( JList<?> list, Object val, int idx,
			boolean sel, boolean hasfocus ) {
		PlaySheetEnum  pse = PlaySheetEnum.valueOf( val.toString() );

		super.getListCellRendererComponent( list, pse.getSheetName(), idx, sel, hasfocus );

		Class<? extends IPlaySheet> klass = pse.getSheetClass();
		Icon icon = DefaultPlaySheetIcons.getDefaultIcon( pse );
		if ( icon == null ) {
			icon = DefaultPlaySheetIcons.blank;
		}
		setIcon( icon );

		return this;
	}
}
