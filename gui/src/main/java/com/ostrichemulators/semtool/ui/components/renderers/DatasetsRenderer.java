/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import com.ostrichemulators.semtool.util.GuiUtility;

/**
 *
 * @author ryan
 */
public class DatasetsRenderer extends DefaultListCellRenderer {

	private static final Icon dbpin;


	static {
		dbpin = new ImageIcon( GuiUtility.loadImage( "icons16/semantic_dataset1_16.png" ) );
	}

	@Override
	public Component getListCellRendererComponent( JList list, Object val, int idx,
			boolean sel, boolean hasfocus ) {
		super.getListCellRendererComponent( list, SEMONTO.NAMESPACE, idx, sel, hasfocus );
		setIcon( dbpin );

		return this;
	}
}
