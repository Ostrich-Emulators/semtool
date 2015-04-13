/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.ui.components.FileBrowsePanel;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import gov.va.semoss.ui.components.LoadingPlaySheetFrame;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.SemossFileView;
import gov.va.semoss.util.DIHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;

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
		putValue( SHORT_DESCRIPTION, "Import Spreadsheet" );
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

			JFileChooser chsr = new JFileChooser( f );
			chsr.setFileSelectionMode( JFileChooser.FILES_ONLY );
			chsr.setFileView( new SemossFileView() );
			chsr.setFileFilter( FileBrowsePanel.getLoadingSheetsFilter( true ) );
			chsr.setDialogTitle( "Select File to Import" );
			chsr.setMultiSelectionEnabled( true );

			if ( JFileChooser.APPROVE_OPTION == chsr.showOpenDialog( frame ) ) {
				prefs.put( "lastimpdir", chsr.getSelectedFile().getParent() );
		
        for( File file : chsr.getSelectedFiles() ){
          if( file.exists() ){
            files.add( file );
          }
        }
			}
		}

 		return !files.isEmpty();
	}

	@Override
	public ProgressTask getTask( ActionEvent ae ) {
		LoadingPlaySheetFrame psf
				= new LoadingPlaySheetFrame( null, files, false, false, false, false );
		DIHelper.getInstance().getDesktop().add( psf );
		files.clear(); // get ready for the next time we get called
		return psf.getLoadingTask();
	}
}
