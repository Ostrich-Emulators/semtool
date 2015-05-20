/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.poi.main.GraphMLWriter;
import gov.va.semoss.poi.main.ImportData;

import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.SemossFileView;

import javax.swing.JOptionPane;

/**
 *
 * @author ryan
 */
public class ExportGraphMLAction extends DbAction {

	public static enum Style {

		NT, TTL, RDF
	};
	private static final Logger log
			= Logger.getLogger( ExportGraphMLAction.class );
	private final Frame frame;
	private File exportfile;

	public ExportGraphMLAction( String optg, Frame frame ) {
		super( optg, "GraphML" );
		this.frame = frame;
		putValue( AbstractAction.SHORT_DESCRIPTION,
				"Export the database as a GraphML file" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_G );
	}

	@Override
	protected boolean preAction( ActionEvent ae ) {
		Preferences prefs = Preferences.userNodeForPackage( ExportGraphMLAction.class );
		File emptypref = FileBrowsePanel.getLocationForEmptyPref( prefs,
				"lastgraphexp" );
		JFileChooser chsr = new JFileChooser( emptypref );
		chsr.setFileView( new SemossFileView() );
		chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter( "GraphML Files", "graphml" ) );
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
							new GraphMLWriter().write( data, exportfile );
						}
						catch ( Exception re ) {
							Utility.showError( re.getLocalizedMessage() );
							log.error( re, re );
						}
					}
				} ) {
					@Override
					public void done() {
						super.done();
						Utility.showExportMessage( null, "Exported to " + exportfile,
								"Success", exportfile );
					}
				};

		return pt;
	}

	private File getSuggestedExportFile( File dir ) {
		File file = new File( dir,
				Utility.getSaveFilename( getEngineName(), ".graphml" ) );
		return file;
	}
}
