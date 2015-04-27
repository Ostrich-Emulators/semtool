/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.ui.components.FileBrowsePanel;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import gov.va.semoss.ui.components.LoadingPlaySheetFrame;
import gov.va.semoss.ui.components.OperationsProgress;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.SemossFileView;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class OpenAction extends DbAction {

	private final List<File> files = new ArrayList<>();
	private final Frame frame;

	public OpenAction( String optg, Frame frame ) {
		super( optg, "Open File", "open-file3" );
		this.frame = frame;
		putValue( SHORT_DESCRIPTION, "Open Files" );
	}

	public OpenAction( String optg, Frame frame, Collection<File> toadd ) {
		this( optg, frame );
		files.addAll( toadd );
	}

	public void setFiles( Collection<File> file ) {
		files.addAll( file );
	}

	@Override
	public boolean preAction( ActionEvent ae ) {
		if ( files.isEmpty() ) {
			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			File f = FileBrowsePanel.getLocationForEmptyPref( prefs, "lastimpdir" );

			Set<File> openedDbs = new HashSet<>();
			for ( IEngine eng : DIHelper.getInstance().getEngineMap().values() ) {
				openedDbs.add( new File( eng.getProperty( Constants.SMSS_LOCATION ) ) );
			}

			JFileChooser chsr = new JFileChooser( f );
			chsr.setFileSelectionMode( JFileChooser.FILES_ONLY );
			chsr.setFileView( new SemossFileView() );
			chsr.setFileFilter( FileBrowsePanel.getLoadingSheetsFilter( true ) );
			chsr.addChoosableFileFilter( FileBrowsePanel.getDatabaseFilter( openedDbs ) );
			chsr.setDialogTitle( "Select File to Import" );
			chsr.setMultiSelectionEnabled( true );

			if ( JFileChooser.APPROVE_OPTION == chsr.showOpenDialog( frame ) ) {
				prefs.put( "lastimpdir", chsr.getSelectedFile().getParent() );

				for ( File file : chsr.getSelectedFiles() ) {
					if ( file.exists() ) {
						files.add( file );
					}
				}
			}
		}

		return !files.isEmpty();
	}

	@Override
	public ProgressTask getTask( ActionEvent ae ) {
		// remove any database files before loading loading sheets
		List<File> journals = new ArrayList<>();
		List<File> sheets = new ArrayList<>();
		for ( File f : files ) {
			if ( "jnl".equals( FilenameUtils.getExtension( f.getName() ) ) ) {
				journals.add( f );
			}
			else {
				sheets.add( f );
			}
		}
		files.clear();

		ProgressTask pt = new ProgressTask( "Opening Files", new Runnable() {

			@Override
			public void run() {
				if ( !sheets.isEmpty() ) {
					LoadingPlaySheetFrame psf = new LoadingPlaySheetFrame( null, sheets,
							false, false, false, false );
					DIHelper.getInstance().getDesktop().add( psf );
					OperationsProgress.getInstance( opprogName ).add( psf.getLoadingTask() );
				}
				if ( !journals.isEmpty() ) {
					for ( File f : journals ) {
						try {
							EngineUtil.getInstance().mount( f, true );
						}
						catch ( EngineManagementException e ) {
							Logger.getLogger( OpenAction.class ).error( e, e );
						}
					}
				}
			}
		} );

		return pt;
	}
}
