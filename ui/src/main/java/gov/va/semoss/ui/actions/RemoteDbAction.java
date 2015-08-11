/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.rdf.engine.impl.SesameEngine;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.RemoteDbPanel;
import gov.va.semoss.util.GuiUtility;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.prefs.Preferences;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class RemoteDbAction extends DbAction {

	private static final Logger log = Logger.getLogger( RemoteDbAction.class );
	private final Frame frame;
	private Properties props = null;

	public RemoteDbAction( String optg, Frame frame ) {
		super( optg, "Open Remote DB", "open-file3" );
		this.frame = frame;
		putValue( SHORT_DESCRIPTION, "Open Remote DB" );
		//putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_O );
	}

	@Override
	public boolean preAction( ActionEvent ae ) {

		Preferences prefs = Preferences.userNodeForPackage( getClass() );

		RemoteDbPanel panel = new RemoteDbPanel( prefs.get( "lastexturl", "http://" ),
				prefs.get( "lastinsighturl", "http://" ) );

		String options[] = { "Connect", "Cancel" };
		int opt = JOptionPane.showOptionDialog( frame, panel, "Open Remote DB",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
				options[0] );
		if ( 0 == opt ) {
			try {
				props = panel.getConnectionProperties();
				prefs.put( "lastexturl", props.getProperty( SesameEngine.REPOSITORY_KEY ) );
				prefs.put( "lastinsighturl", props.getProperty( SesameEngine.INSIGHTS_KEY ) );
				return true;
			}
			catch ( MalformedURLException mue ) {
				GuiUtility.showError( mue.getLocalizedMessage() );
			}
		}

		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		String title = "Opening Remote Database";
		ProgressTask pt = new ProgressTask( title, new Runnable() {

			@Override
			public void run() {
				try {
					File propfile = File.createTempFile( "remote-db-", ".properties" );
					propfile.deleteOnExit();

					try ( FileWriter fw = new FileWriter( propfile ) ) {
						props.store( fw, "" );
					}

					EngineUtil.getInstance().mount( propfile, true );
				}
				catch ( IOException | EngineManagementException e ) {
					log.error( e, e );
					GuiUtility.showError( e.getLocalizedMessage() );
				}
			}
		} );

		return pt;
	}

}
