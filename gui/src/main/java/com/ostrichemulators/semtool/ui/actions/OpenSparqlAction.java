/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.ui.components.CustomSparqlPanel;
import com.ostrichemulators.semtool.ui.components.FileBrowsePanel;
import com.ostrichemulators.semtool.ui.components.FileBrowsePanel.CustomFileFilter;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.ostrichemulators.semtool.ui.components.ProgressTask;

import com.ostrichemulators.semtool.ui.components.SemtoolFileView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class OpenSparqlAction extends DbAction {

	private static final Logger log = Logger.getLogger( OpenSparqlAction.class );
	private final List<File> files = new ArrayList<>();
	private final Frame frame;
	private final CustomSparqlPanel csp;

	public OpenSparqlAction( String optg, Frame frame, CustomSparqlPanel csp ) {
		super( optg, "Open SPARQL File", "semantic_dataset1" );
		this.frame = frame;
		this.csp = csp;
		putValue( SHORT_DESCRIPTION, "Open SPARQL File" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S );
	}

	@Override
	public void actionPerformed( ActionEvent ae ) {
		files.clear();
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		File f = FileBrowsePanel.getLocationForEmptyPref( prefs, "lastimpdir" );

		JFileChooser chsr = new JFileChooser( f );
		chsr.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chsr.setMultiSelectionEnabled( false );
		chsr.setFileView(new SemtoolFileView() );
		chsr.setFileFilter( new CustomFileFilter( "SPARQL Files", "spq", "sparql" ) );
		chsr.setDialogTitle( "Select SPARQL File to Open" );
		chsr.setMultiSelectionEnabled( true );

		if ( JFileChooser.APPROVE_OPTION == chsr.showOpenDialog( frame ) ) {
			prefs.put( "lastimpdir", chsr.getSelectedFile().getParent() );

			csp.loadFileToEmptyEditor( chsr.getSelectedFile() );
		}
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
