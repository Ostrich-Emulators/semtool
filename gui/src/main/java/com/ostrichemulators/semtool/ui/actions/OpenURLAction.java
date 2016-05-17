/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import java.awt.Desktop;
import java.net.URI;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.ui.components.ProgressTask;

/**
 *
 * @author ryan
 */
public class OpenURLAction extends DbAction {

	private static final Logger log = Logger.getLogger( OpenURLAction.class );
	private URI uri;

	/**
	 * Creates an Action with the given key, short description, and image
	 *
	 * @param text the action command
	 * @param description the short description
	 * @param uristr the url to open when selected
	 * @param imagePart the image name locator
	 * @param shortCut a shortcut
	 */
	public OpenURLAction( String text, String description, String uristr,
			String imagePart, int shortCut ) {
		super( null, text, imagePart );
		putValue( AbstractAction.SHORT_DESCRIPTION, description );
		putValue(AbstractAction.MNEMONIC_KEY, shortCut);
		if ( ( null == uristr || uristr.isEmpty() ) ) {
			log.error( "missing/empty URL for action: " + text );
		}
		else {
			try {
				uri = new URI( uristr );
			}
			catch ( Exception e ) {
				log.error( "couldn't create URI from string:" + uristr, e );
				uri = null;
			}
		}

		setEnabled( null != uri );
	}

	public static OpenURLAction getTracAction() {
		return new OpenURLAction( "Report an Issue",
				"Opens a Browser to the OS-EM Semantic Toolkit Issue Reporting system",
				"https://github.com/Ostrich-Emulators/semtool/issues", "trac", KeyEvent.VK_I );
		
	}

	public static OpenURLAction getLicense() {
		return new OpenURLAction( "Read the Software License",
				"Opens the Browser to OS-EM Semantic Toolkit License",
				"http://www.gnu.org/licenses/gpl-3.0.en.html",
				"license", KeyEvent.VK_L );
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		ProgressTask pt = new ProgressTask( "Opening browser",
				new Runnable() {
					@Override
					public void run() {

						if ( Desktop.isDesktopSupported() ) {
							try {
								Desktop.getDesktop().browse( uri );
							}
							catch ( IOException ioe ) {
								GuiUtility.showError( "Problem opening the browser" );
								log.error( ioe, ioe );
							}
						}
					}
				} );

		return pt;
	}

}
