package gov.va.semoss.ui.components;

import gov.va.semoss.rdf.engine.util.DBToLoadingSheetExporter;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.XlsWriter;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openrdf.model.URI;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.renderers.LabeledPairRenderer;
import gov.va.semoss.ui.components.UriComboBox.UriLabelPair;
import gov.va.semoss.util.Utility;
import gov.va.semoss.ui.actions.ExportLoadingSheetAction;
import gov.va.semoss.util.DIHelper;
import java.awt.Desktop;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author john.marquiss
 */
public class ExportSpecificNodesPanel extends JPanel {

	private static final Logger log = Logger.getLogger( ExportSpecificNodesPanel.class );
	private static final long serialVersionUID = 5742702924704796580L;
	private final IEngine engine;

	private JButton exportNodesJButton;
	private JLabel nodesJLabel;
	private JScrollPane nodesScrollPane;
	private JList<URI> nodesJList;
	private final LabeledPairRenderer<URI> renderer = new LabeledPairRenderer<>();
	private JCheckBox togrid = new JCheckBox( "Export to Grid" );
	private JCheckBox dorels = new JCheckBox( "Include Relationships" );

	private File exportFile;
	private List<URI> selectedNodes;
	private String successMessage = "";

	public ExportSpecificNodesPanel( IEngine eng ) {
		engine = eng;
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		nodesJList = new JList<>( getAllNodes() );
		nodesJList.setCellRenderer( renderer );

		nodesScrollPane = new JScrollPane();
		nodesScrollPane.setViewportView( nodesJList );
		nodesScrollPane.setToolTipText( "Select nodes to export from drop-down list" );

		nodesJLabel = new JLabel( "Choose Node(s) to export:" );
		nodesJLabel.setLabelFor( nodesScrollPane );

		exportNodesJButton = new JButton();
		exportNodesJButton.setText( "Export" );
		exportNodesJButton.setToolTipText( "Export Selected Nodes" );
		exportNodesJButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent evt ) {
				exportButtonActionPerformed( evt );
			}
		} );

		GroupLayout layout = new GroupLayout( this );
		layout.setAutoCreateGaps( true );
		layout.setAutoCreateContainerGaps( true );
		this.setLayout( layout );

		layout.setHorizontalGroup( layout.createSequentialGroup()
				.addComponent( nodesJLabel )
				.addGroup( layout.createParallelGroup( GroupLayout.Alignment.TRAILING )
						.addComponent( nodesScrollPane )
						.addComponent( exportNodesJButton )
						.addComponent( dorels )
						.addComponent( togrid )
				)
		);

		layout.setVerticalGroup( layout.createSequentialGroup()
				.addGroup( layout.createParallelGroup( GroupLayout.Alignment.BASELINE )
						.addComponent( nodesJLabel )
						.addComponent( nodesScrollPane )
				)
				.addGroup( layout.createSequentialGroup()
						.addComponent( dorels )
						.addComponent( togrid )
						.addComponent( exportNodesJButton ) )
		);
	}

	private URI[] getAllNodes() {
		List<URI> uriconcepts = new DBToLoadingSheetExporter( engine ).createConceptList();
		Map<URI, String> labels = Utility.getInstanceLabels( uriconcepts, engine );
		renderer.cache( labels );

		List<UriLabelPair> pairs = new ArrayList<>();
		for ( Map.Entry<URI, String> en : labels.entrySet() ) {
			pairs.add( new UriLabelPair( en.getKey(), en.getValue() ) );
		}

		Collections.sort( pairs );

		List<URI> uris = new ArrayList<>();
		for ( UriLabelPair lup : pairs ) {
			uris.add( lup.getUri() );
		}

		return uris.toArray( new URI[0] );
	}

	private void exportButtonActionPerformed( ActionEvent evt ) {
		selectedNodes = nodesJList.getSelectedValuesList();
		if ( selectedNodes == null || selectedNodes.isEmpty() ) {
			Utility.showMessage( "You must select at least one node to export." );
			return;
		}

		if ( !togrid.isSelected() ) {
			exportFile = getLoadingSheetExportFileLocationFromUser( "Nodes", getParent() );
			if ( exportFile == null ) {
				return;
			}
		}

		runExport();
	}

	public static File getLoadingSheetExportFileLocationFromUser( String exportType, Container parent ) {
		Preferences prefs = Preferences.userNodeForPackage( ExportLoadingSheetAction.class );
		JFileChooser fileChooser = new JFileChooser( prefs.get( "lastexp", "." ) );

		fileChooser.setFileFilter( FileBrowsePanel.getLoadingSheetsFilter( false ) );
		fileChooser.setFileView( new SemossFileView() );
		fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
		fileChooser.setDialogTitle( "Select Name and Location for Export File" );
		fileChooser.setSelectedFile(
				DBToLoadingSheetExporter.getDefaultExportFile( fileChooser.getCurrentDirectory(), exportType, false )
		);

		int response = fileChooser.showSaveDialog( parent );
		if ( response != JFileChooser.APPROVE_OPTION ) {
			return null; //go back to the previous dialog
		}

		File exportFile = fileChooser.getSelectedFile();
		if ( exportFile.exists() ) {
			int rslt = JOptionPane.showConfirmDialog( parent, "File exists. Overwrite?",
					"Overwrite?", JOptionPane.YES_NO_OPTION );
			if ( rslt != JOptionPane.YES_OPTION ) {
				return getLoadingSheetExportFileLocationFromUser( exportType, parent );
			}
		}

		File exportFileParent = exportFile.getParentFile();
		prefs.put( "lastexp", exportFileParent.getAbsolutePath() );

		return exportFile;
	}

	public void runExport() {
		getParent().getParent().getParent().getParent().setVisible( false ); //4 levels up is the containing JDialog

		StringBuilder sb = new StringBuilder( "Exporting Specific Node Loading Sheets of " );
		sb.append( engine.getEngineName() ).append( " to " );
		sb.append( togrid.isSelected() ? "Grid" : exportFile.getAbsolutePath() );
		boolean ok[] = { false };

		ProgressTask progressTask = new ProgressTask( sb.toString(), new Runnable() {
			@Override
			public void run() {
				DBToLoadingSheetExporter exper = new DBToLoadingSheetExporter( engine );
				ImportData data = ImportData.forEngine( engine );
				exper.exportNodes( selectedNodes, data );

				if ( dorels.isSelected() ) {
					exper.exportAllRelationships( selectedNodes, data );
				}

				if ( togrid.isSelected() ) {
					PlaySheetFrame psf = new LoadingPlaySheetFrame( engine, data );
					psf.setTitle( "Loading Sheet Export" );
					DIHelper.getInstance().getDesktop().add( psf );
				}
				else {
					XlsWriter writer = new XlsWriter();
					try {
						writer.write( data, exportFile );
						successMessage = "Successfully exported Nodes to: " + exportFile;
						ok[0] = true;
					}
					catch ( Exception e ) {
						log.error( e, e );
						successMessage = "Error exporting nodes to: " + exportFile;
					}
				}
			}
		} ) {
			@Override
			public void done() {
				super.done();

				if ( successMessage != null && !successMessage.equals( "" ) ) {
					if ( ok[0] && Desktop.isDesktopSupported() ) {
						Utility.showExportMessage( null, successMessage, "Success", exportFile );
					}
					else {
						Utility.showMessage( successMessage );
					}
				}
			}
		};

		OperationsProgress.getInstance( "UI" ).add( progressTask );
	}
}
