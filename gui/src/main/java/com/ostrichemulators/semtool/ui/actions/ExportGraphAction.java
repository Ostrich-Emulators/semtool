/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.poi.main.GraphMLWriter;
import com.ostrichemulators.semtool.poi.main.GsonWriter;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.rdf.engine.util.DBToLoadingSheetExporter;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.ui.components.FileBrowsePanel;
import com.ostrichemulators.semtool.ui.components.ProgressTask;

import com.ostrichemulators.semtool.ui.components.SemtoolFileView;
import com.ostrichemulators.semtool.util.Utility;
import javax.swing.JOptionPane;

/**
 *
 * @author ryan
 */
public class ExportGraphAction extends DbAction {

	public static enum Style {

		GRAPHML, GSON
	};
	private static final Logger log
			= Logger.getLogger( ExportGraphAction.class );
	private final Frame frame;
	private File exportfile;
	private final Style style;

	public ExportGraphAction( String optg, Frame frame, Style style ) {
		super( optg, Style.GRAPHML == style ? "GraphML" : "GSON" );
		this.frame = frame;
		this.style = style;
		

		putValue( AbstractAction.SHORT_DESCRIPTION, "Export the database as a "
				+ ( Style.GRAPHML == style ? "GraphML" : "GSON" ) + " file" );
		if ( Style.GRAPHML == style ) {
			putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_G );
			putValue( AbstractAction.SMALL_ICON, DbAction.getIcon( "graphml-icon" ) );
		}
		else {
			putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_N );
			putValue( AbstractAction.SMALL_ICON, DbAction.getIcon( "gson-icon" ) );
		}
	}

	@Override
	protected boolean preAction( ActionEvent ae ) {
		Preferences prefs = Preferences.userNodeForPackage( ExportGraphAction.class );
		File emptypref = FileBrowsePanel.getLocationForEmptyPref( prefs,
				"lastgraphexp" );
		JFileChooser chsr = new JFileChooser( emptypref );
		chsr.setFileView(new SemtoolFileView() );
		if ( Style.GRAPHML == style ) {
			chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter( "GraphML Files", "graphml" ) );
		}
		else {
			chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter( "GSON Files", "gson" ) );
		}
		chsr.setDialogTitle( "Select Export Location" );
		chsr.setApproveButtonText( "Export" );
		chsr.setSelectedFile( getSuggestedExportFile( chsr.getCurrentDirectory() ) );
		int retval = chsr.showSaveDialog( frame );
		if ( JFileChooser.APPROVE_OPTION == retval ) {
			exportfile = chsr.getSelectedFile();

			if ( exportfile.exists() ) {
				int rslt = JOptionPane.showConfirmDialog( frame, "File exists. Overwrite?",
						"Overwrite?", JOptionPane.YES_NO_OPTION );
				if ( rslt != JOptionPane.YES_OPTION ) {
					return preAction( ae );
				}
			}

			if ( null != exportfile ) {
				prefs.put( "lastgraphexp", exportfile.getParent() );
				return true;
			}
		}
		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		ProgressTask pt = new ProgressTask( "Exporting " + getEngineName()
				+ " to " + exportfile.getAbsolutePath(),
				new Runnable() {
					@Override
					public void run() {
						try {
							DBToLoadingSheetExporter exp = new DBToLoadingSheetExporter( getEngine() );
							ImportData data = exp.runExport( true, true );
							if ( Style.GRAPHML == style ) {
								new GraphMLWriter().write( data, exportfile );
							}
							else {
								new GsonWriter().write( data, exportfile );
							}
						}
						catch ( Exception re ) {
							GuiUtility.showError( re.getLocalizedMessage() );
							log.error( re, re );
						}
					}
				} ) {
					@Override
					public void done() {
						super.done();
						GuiUtility.showExportMessage( null, "Exported to " + exportfile,
								"Success", exportfile );
					}
				};

		return pt;
	}

	private File getSuggestedExportFile( File dir ) {

		File file = new File( dir, Utility.getSaveFilename( getEngineName(),
				( Style.GRAPHML == style ? ".graphml" : ".gson" ) ) );
		return file;
	}
}
