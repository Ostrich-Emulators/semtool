/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Desktop;
import java.net.URI;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.ProgressTask;

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
				"Opens a Browser to the V-CAMP Issue Reporting systems",
				DIHelper.getInstance().getProperty( Constants.HELPURI_KEY ), "trac", KeyEvent.VK_I );
		
	}

	public static OpenURLAction getLatestReleaseAction() {
		return new OpenURLAction( "Get the Latest Release",
				"Opens a Browser to the latest V-CAMP Stable application",
				DIHelper.getInstance().getProperty( Constants.LATESTRELEASE_KEY ),
				"VCAMP-Tool", KeyEvent.VK_S );
	}

	public static OpenURLAction getExperimentalReleaseAction() {
		return new OpenURLAction( "Get the Experimental Release",
				"Opens a browser to the latest V-CAMP experimental release",
				DIHelper.getInstance().getProperty( Constants.EXPERIMENTALRELEASE_KEY ),
				"VCAMP-Labs", KeyEvent.VK_E );
	}

	public static OpenURLAction getSemossAction() {
		return new OpenURLAction( "SEMOSS User Manual",
				"Opens a browser to the online SEMOSS user manual",
				"http://semoss.org/userdocs.html", "whitelogo", KeyEvent.VK_M );
	}

	public static OpenURLAction getLicense() {
		return new OpenURLAction( "Read the Software License",
				"Opens the Browser to V-CAMP SEMOSS Tool License",
				DIHelper.getInstance().getProperty( Constants.LICENSEURI_KEY ),
				"license", KeyEvent.VK_L );
	}

	public static OpenURLAction getHelpManual() {
		try {
			return new OpenURLAction( "V-CAMP SEMOSS User Manual",
					"Opens the local V-CAMP SEMOSS User Manual",
					OpenURLAction.class.getResource( "/help/V-CAMP SEMOSS Tool User Manual.pdf" ).toURI().toString(),
					"helpbook", KeyEvent.VK_M );
		}
		catch ( Exception e ) {
			log.error( e, e );
			Utility.showError( e.getMessage() );
			throw new IllegalArgumentException( "Could not find manual" );
		}
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
								Utility.showError( "Problem opening the browser" );
								log.error( ioe, ioe );
							}
						}
					}
				} );

		return pt;
	}

}
