/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.XlsWriter;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.rdf.engine.util.DBToLoadingSheetExporter;
import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.ui.components.FileBrowsePanel;
import com.ostrichemulators.semtool.ui.components.LoadingPlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.ProgressTask;
import com.ostrichemulators.semtool.ui.components.SemtoolFileView;
import com.ostrichemulators.semtool.util.DIHelper;

import java.io.IOException;

import javax.swing.JOptionPane;

/**
 *
 * @author ryan
 */
public class ExportLoadingSheetAction extends DbAction {

	private static final long serialVersionUID = 6700489640671853500L;
	private static final Logger log = Logger.getLogger( ExportLoadingSheetAction.class );
	private final Frame frame;
	private File exportfile;
	private final boolean donodes;
	private final boolean dorels;
	private boolean togrid;

	public ExportLoadingSheetAction( String optg, Frame frame, boolean donodes,
			boolean dorels ) {
		super( optg,
				( donodes && dorels ? EXPORTLS : donodes ? EXPORTLSNODES : EXPORTLSRELS ),
				"excel" );
		this.frame = frame;
		this.donodes = donodes;
		this.dorels = dorels;

		if ( donodes && dorels ) {
			putValue( AbstractAction.SHORT_DESCRIPTION, "Export all nodes and relationships as loading sheets" );
			putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_C );
		}
		else if ( donodes ) {
			putValue( AbstractAction.SHORT_DESCRIPTION, "Export all nodes as loading sheets" );
			putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_A );
		}
		else {
			putValue( AbstractAction.SHORT_DESCRIPTION, "Export all relationships as loading sheets" );
			putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_A );
		}
	}

	@Override
	protected boolean preAction( ActionEvent ae ) {
		Preferences prefs = Preferences.userNodeForPackage( ExportLoadingSheetAction.class );

		Object choices[] = { "Grid", "File" };

		Integer choice = JOptionPane.showOptionDialog( frame, "Export to Grid or File?",
				"Export Type", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, choices, choices[0] );
		if ( JOptionPane.CLOSED_OPTION == choice ) {
			return false;
		}

		togrid = ( 0 == choice );
		if ( togrid ) {
			return true;
		}

		JFileChooser chsr = new JFileChooser( prefs.get( "lastexp", "." ) );
		chsr.setFileFilter( FileBrowsePanel.getLoadingSheetsFilter( false ) );
		chsr.setFileView(new SemtoolFileView() );

		chsr.setFileSelectionMode( JFileChooser.FILES_ONLY );
		chsr.setDialogTitle( "Select Export File" );
		
		String what = ( dorels && donodes ? "Complete_Database" : donodes ? "Nodes" : "Relationships");
		chsr.setSelectedFile( DBToLoadingSheetExporter.getDefaultExportFile( chsr.getCurrentDirectory(),
				what, !( dorels && donodes ) ) );

		int retval = chsr.showSaveDialog( frame );
		if ( JFileChooser.APPROVE_OPTION == retval ) {
			exportfile = chsr.getSelectedFile();

			if ( null != exportfile ) {
				File exppref = exportfile.getParentFile();
				prefs.put( "lastexp", exppref.getAbsolutePath() );
				return true;
			}
		}

		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		StringBuilder sb = new StringBuilder( "Exporting " );
		if ( !( donodes && dorels ) ) {
			sb.append( donodes ? "Node " : "Relationship " );
		}
		sb.append( "Loading Sheets of " );
		sb.append( getEngineName() ).append( " to " );
		sb.append( togrid ? "Grid" : exportfile.getAbsolutePath() );

		ProgressTask pt = new ProgressTask( sb.toString(), new Runnable() {
			@Override
			public void run() {
				DBToLoadingSheetExporter exper
						= new DBToLoadingSheetExporter( getEngine() );
				ImportData data = exper.runExport( donodes, dorels );
				if ( togrid ) {
					LoadingPlaySheetFrame psf = new LoadingPlaySheetFrame( getEngine(), data );
					psf.setTitle( "Loading Sheet Export" );
					DIHelper.getInstance().getDesktop().add( psf );
				}
				else {
					String msg;
					boolean ok = true;
					XlsWriter writer = new XlsWriter();
					try {
						writer.write( data, exportfile );
						msg = sb.toString().replaceAll( "^Exporting", "Successfully exported" );
					}
					catch ( IOException ioe ) {
						log.error( ioe, ioe );
						ok = false;
						msg = "Export failed: " + ioe.getLocalizedMessage();
					}

					if ( ok ) {
						GuiUtility.showExportMessage( frame, msg, "Success", exportfile );
					}
					else {
						GuiUtility.showMessage( msg );
					}
				}
			}
		} );

		return pt;
	}
}
