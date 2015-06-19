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
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
	private URL url;
	private URL insights;

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
				url = panel.getUrl();
				prefs.put( "lastexturl", url.toExternalForm() );
				insights = panel.getInsights();
				prefs.put( "lastinsighturl", insights.toExternalForm() );
				return true;
			}
			catch ( MalformedURLException mue ) {
				Utility.showError( mue.getLocalizedMessage() );
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
				Properties props = new Properties();
				props.setProperty( SesameEngine.REPOSITORY_KEY, url.toExternalForm() );
				props.setProperty( SesameEngine.INSIGHTS_KEY, insights.toExternalForm() );
				props.setProperty( Constants.ENGINE_IMPL, SesameEngine.class.getCanonicalName() );

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
					Utility.showError( e.getLocalizedMessage() );
				}
			}
		} );

		return pt;
	}

}
