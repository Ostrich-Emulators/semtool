/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil;
import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.ui.components.RemoteDbPanel;
import com.ostrichemulators.semtool.util.GuiUtility;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class RemoteEndpointAction extends DbAction {

	private static final Logger log = Logger.getLogger( RemoteEndpointAction.class );
	private final Frame frame;
	private Properties props = null;
	private User user = null;

	public RemoteEndpointAction( String optg, Frame frame ) {
		this( optg, frame, "Open SPARQL Endpoint", "open-file3" );
	}

	public RemoteEndpointAction( String optg, Frame frame, String title, String iconimg ) {
		super( optg, title, iconimg );
		this.frame = frame;
		putValue( SHORT_DESCRIPTION, title );
	}

	protected Properties getProperties(){
		return props;
	}

	protected User getUser(){
		return user;
	}

	@Override
	public boolean preAction( ActionEvent ae ) {
		RemoteDbPanel panel = new RemoteDbPanel();
		panel.setSemtoolTarget( false );

		String options[] = { "Open", "Cancel" };
		int opt = JOptionPane.showOptionDialog( frame, panel, "Open Remote SPARQL Endpoint",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
				options[0] );
		if ( 0 == opt ) {
			try {
				props = panel.getConnectionProperties();
				user = panel.getConnectedUser();
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

					EngineUtil.getInstance().mount( propfile, true, user );
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
