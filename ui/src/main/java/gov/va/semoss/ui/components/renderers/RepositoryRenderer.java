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
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.GuiUtility;

/**
 *
 * @author ryan
 */
public class RepositoryRenderer extends DefaultListCellRenderer {

	private static final Icon pin;
	private static final Icon nopin;
	private static final Icon nopinserver;
	private static final Icon pinserver;

	static {
		pin = new ImageIcon( GuiUtility.loadImage( "icons16/dbpin_16.png" ) );
		nopin = new ImageIcon( GuiUtility.loadImage( "icons16/db_16.png" ) );
		nopinserver = new ImageIcon( GuiUtility.loadImage( "icons16/db_sparql_endpoint1_16.png" ) );
		pinserver = new ImageIcon( GuiUtility.loadImage( "icons16/dbpin_sparql_endpoint1_16.png" ) );
	}

	@Override
	public Component getListCellRendererComponent( JList list, Object val, int idx,
			boolean sel, boolean hasfocus ) {

		IEngine eng = IEngine.class.cast( val );
		String title = eng.getEngineName();

		super.getListCellRendererComponent( list, title, idx, sel, hasfocus );

		Icon icon;
		boolean pinned = Boolean.parseBoolean( eng.getProperty( Constants.PIN_KEY ) );
		if ( eng.serverIsRunning() ) {
			icon = ( pinned ? pinserver : nopinserver );
		}
		else {
			icon = ( pinned ? pin : nopin );
		}

		setIcon( icon );

		return this;
	}
}
