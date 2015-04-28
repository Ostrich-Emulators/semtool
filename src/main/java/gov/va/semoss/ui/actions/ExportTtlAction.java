/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;

import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.main.SemossPreferences;

import java.util.Map;

import javax.swing.JOptionPane;

/**
 *
 * @author ryan
 */
public class ExportTtlAction extends DbAction {

	public static enum Style {

		NT, TTL, RDF
	};
	private static final Logger log = Logger.getLogger( ExportTtlAction.class );
	private final Frame frame;
	private File exportfile;
	private final Style exportAs;

	public ExportTtlAction( String optg, Style style, Frame frame ) {
		super( optg,
				Style.NT == style ? EXPORTNT : Style.TTL == style ? EXPORTTTL
								: EXPORTRDF,
				"semantic-webdoc" );
		this.frame = frame;
		exportAs = style;
		String desc;
		switch ( exportAs ) {
			case NT:
				desc = "an N-Triples";
				putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_N );
				break;
			case TTL:
				desc = "a Turtle";
				putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_T );
				break;
			default:
				desc = "an RDF";
				putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_R );
		}
		putValue( AbstractAction.SHORT_DESCRIPTION,
				"Export the database as " + desc + " file" );
	}

	@Override
	protected boolean preAction( ActionEvent ae ) {
		Preferences prefs = Preferences.userNodeForPackage( ExportTtlAction.class );
		File emptypref = FileBrowsePanel.getLocationForEmptyPref( prefs, "lastexp" );
		JFileChooser chsr = new JFileChooser( emptypref );
		chsr.setDialogTitle( "Select Export Location" );
		chsr.setApproveButtonText( "Export" );
		chsr.setSelectedFile( getSuggestedExportFile( chsr.getCurrentDirectory() ) );
		int retval = chsr.showSaveDialog( frame );
		if ( JFileChooser.APPROVE_OPTION == retval ) {
			exportfile = chsr.getSelectedFile();

			if ( null != exportfile ) {
				if ( exportfile.exists() ) {
					int rslt = JOptionPane.showConfirmDialog( frame, "File exists. Overwrite?",
							"Overwrite?", JOptionPane.YES_NO_OPTION );
					if ( rslt != JOptionPane.YES_OPTION ) {
						return preAction( ae );
					}
				}

				prefs.put( "lastexp", exportfile.getParent() );
				return true;
			}
		}
		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		final boolean ok[] = { true };

		ProgressTask pt = new ProgressTask( "Exporting " + getEngineName()
				+ " to " + exportfile.getAbsolutePath(),
				new Runnable() {
					@Override
					public void run() {
						try {
							getEngine().execute( new ModificationExecutorAdapter() {

								@Override
								public void exec( RepositoryConnection conn ) throws RepositoryException {
									try ( FileWriter fw = new FileWriter( exportfile ) ) {
										RDFHandler handler;
										switch ( exportAs ) {
											case NT:
												handler = new NTriplesWriter( fw );
												break;
											case TTL:
												handler = new TurtleWriter( fw );
												break;
											default:
												handler = new RDFXMLWriter( fw );
										}
										handler.handleComment( "baseURI: "
												+ getEngine().getDataBuilder().toString() );

										Map<String, String> ns
										= SemossPreferences.getInstance().getNamespaces();
										for ( Map.Entry<String, String> en : ns.entrySet() ) {
											handler.handleNamespace( en.getKey(), en.getValue() );
										}

										conn.export( handler );
									}
									catch ( Exception ioe ) {
										// we'll catch this below, in the outer catch
										throw new RepositoryException( ioe );
									}
								}
							} );
						}
						catch ( RepositoryException re ) {
							Utility.showError( re.getLocalizedMessage() );
							ok[0] = false;
							log.error( re, re );
						}
					}
				} ) {
					@Override
					public void done() {
						super.done();

						if ( ok[0] ) {
							Utility.showExportMessage( frame, "Exported to " + exportfile,
									"Success", exportfile );
						}
						else {
							Utility.showMessage( "Exported to " + exportfile );
						}
					}
				};

		return pt;
	}

	private File getSuggestedExportFile( File dir ) {
		StringBuilder name = new StringBuilder( getEngineName() );
		SimpleDateFormat sdf = new SimpleDateFormat( " MMM dd, yyyy HHmm." );
		name.append( sdf.format( new Date() ) );
		name.append( Style.NT == exportAs ? "nt"
				: Style.TTL == exportAs ? "ttl" : "rdf" );
		File file = new File( dir, name.toString() );
		return file;
	}
}
