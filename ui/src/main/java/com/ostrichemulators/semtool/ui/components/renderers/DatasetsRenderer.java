/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components.renderers;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.util.Constants;
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
		super.getListCellRendererComponent( list, "http://os-em.com/ontologies/", idx, sel, hasfocus );
		setIcon( dbpin );

		return this;
	}
}
