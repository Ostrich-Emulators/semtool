/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.actions;

import gov.va.semoss.model.vocabulary.ARG;
import gov.va.semoss.model.vocabulary.OLO;
import gov.va.semoss.model.vocabulary.SP;
import gov.va.semoss.model.vocabulary.SPIN;
import gov.va.semoss.model.vocabulary.SPL;
import gov.va.semoss.model.vocabulary.UI;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.components.FileBrowsePanel;
import gov.va.semoss.ui.components.ProgressTask;
import gov.va.semoss.ui.components.SemossFileView;
import info.aduna.iteration.Iterations;

import java.io.FileWriter;

import javax.swing.JOptionPane;

import org.openrdf.model.Namespace;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class ExportInsightsAction extends DbAction {

	public static enum Style {

		NT, TTL, RDF
	};
	private static final Logger log
			= Logger.getLogger( ExportInsightsAction.class );
	private final Frame frame;
	private File exportfile;

	public ExportInsightsAction( String optg, Frame frame ) {
		super( optg, "Insights (.ttl)", "semantic-webdoc" );
		this.frame = frame;
		putValue( AbstractAction.SHORT_DESCRIPTION,
				"Export the insights as a Turtle file" );
		putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_I );
	}

	@Override
	protected boolean preAction( ActionEvent ae ) {
		Preferences prefs = Preferences.userNodeForPackage(
				ExportInsightsAction.class );
		File emptypref = FileBrowsePanel.getLocationForEmptyPref( prefs,
				"lastinsightsexp" );
		JFileChooser chsr = new JFileChooser( emptypref );
		chsr.setFileView( new SemossFileView() );
		chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter( "Turtle Files", "ttl" ) );
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
				prefs.put( "lastinsightsexp", exportfile.getParent() );
				return true;
			}
		}
		return false;
	}

	@Override
	protected ProgressTask getTask( ActionEvent ae ) {
		ProgressTask pt = new ProgressTask( "Exporting " + getEngineName()
				+ " Insights to " + exportfile.getAbsolutePath(),
				new Runnable() {
					@Override
					public void run() {
						try {
							// we're going to put the statements into a
							//  Repo, so we can convert namespaces nicely
							SailRepository repo = new SailRepository( new MemoryStore() );
							repo.initialize();
							final RepositoryConnection rc = repo.getConnection();
							rc.add( getEngine().getInsightManager().getStatements() );
							rc.setNamespace( VAS.PREFIX, VAS.NAMESPACE );
							rc.setNamespace( SPIN.PREFIX, SPIN.NAMESPACE );
							rc.setNamespace( SP.PREFIX, SP.NAMESPACE );
							rc.setNamespace( SPL.PREFIX, SPL.NAMESPACE );
							rc.setNamespace( OLO.PREFIX, OLO.NAMESPACE );
							rc.setNamespace( UI.PREFIX, UI.NAMESPACE );
							rc.setNamespace( MetadataConstants.VA_INSIGHTS_PREFIX, MetadataConstants.VA_INSIGHTS_NS );
					    rc.setNamespace( ARG.PREFIX, ARG.NAMESPACE );

							getEngine().execute( new ModificationExecutorAdapter() {

								@Override
								public void exec( RepositoryConnection conn ) throws RepositoryException {
									for ( Namespace ns : Iterations.asList( conn.getNamespaces() ) ) {
										rc.setNamespace( ns.getPrefix(), ns.getName() );
									}
								}
							} );

							try ( FileWriter fw = new FileWriter( exportfile ) ) {
								fw.write( "# imports: " + SPIN.BASE_URI + "\r\n" );
								fw.write( "# imports: " + SP.BASE_URI + "\r\n" );
								rc.export( new TurtleWriter( fw ) );
							}
							catch ( Exception ioe ) {
								// we'll catch this below, in the outer catch
								throw new RepositoryException( ioe );
							}
							finally {
								try {
									rc.close();
								}
								catch ( Exception e ) {
									log.warn( e, e );
								}
								try {
									repo.shutDown();
								}
								catch ( Exception e ) {
									log.warn( e, e );
								}
							}

						}
						catch ( RepositoryException re ) {
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
				Utility.getSaveFilename( getEngineName() + "-Insights", ".ttl" ) );
		return file;
	}
}
