/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.renderers;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.GuiUtility;

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
		super.getListCellRendererComponent( list, "http://va.gov/ontologies/", idx, sel, hasfocus );
		setIcon( dbpin );

		return this;
	}
}
