/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException.ErrorCode;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil;
import com.ostrichemulators.semtool.ui.components.FileBrowsePanel;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.ui.components.SemtoolFileView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ryan
 */
public class MountAction extends DbAction {

	private static final Logger log = Logger.getLogger( MountAction.class );
	private Frame frame;
	private final List<File> smssfiles = new ArrayList<>();

	public MountAction( String optg, Frame frame ) {
		super( optg, MOUNT, "attachdb" );
		putValue( AbstractAction.SHORT_DESCRIPTION, "Attach an existing database" );
	}

	@Override
	protected boolean preAction( ActionEvent e ) {
		smssfiles.clear();

		Preferences prefs = Preferences.userNodeForPackage( MountAction.class );
		JFileChooser chsr = new JFileChooser( prefs.get( "lastmountloc", "." ) );
		chsr.setDialogTitle( "Select Directory" );
		chsr.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
		chsr.setMultiSelectionEnabled( true );

		// we want to hide smss files that are already loaded
		final Set<File> loaded = new HashSet<>();
		for ( IEngine engo : DIHelper.getInstance().getEngineMap().values() ) {
			loaded.add( new File( engo.getProperty( Constants.SMSS_LOCATION ) ) );
		}

		chsr.setFileFilter( FileBrowsePanel.getDatabaseFilter( loaded ) );

		chsr.setApproveButtonText( "Attach" );
		chsr.setFileView(new SemtoolFileView() );
		int rslt = chsr.showOpenDialog( frame );
		if ( JFileChooser.APPROVE_OPTION == rslt ) {
			smssfiles.addAll( Arrays.asList( chsr.getSelectedFiles() ) );

			if ( !smssfiles.isEmpty() ) {
				prefs.put( "lastmountloc", smssfiles.get( 0 ).getParent() );
				return true;
			}
		}
		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent e ) {
		String lbl = ( 1 == smssfiles.size()
				? smssfiles.get( 0 ).getName()
				: "multiple files" );

		ProgressTask pt = new ProgressTask( "Submitting attach request for " + lbl,
				new Runnable() {
					@Override
					public void run() {
						for ( File smssfile : smssfiles ) {
							try {
								EngineUtil.getInstance().mount( smssfile.toString(), true );
							}
							catch ( EngineManagementException eme ) {
								String msg = ( ErrorCode.DUPLICATE_NAME == eme.getCode()
										? "A repository with this name is already open. Please choose another."
										: eme.getLocalizedMessage() );
								GuiUtility.showError( msg );
								log.error( eme );
							}
						}
					}
				} );

		return pt;
	}
}
