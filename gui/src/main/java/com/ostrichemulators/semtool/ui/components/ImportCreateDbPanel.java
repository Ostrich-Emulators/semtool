/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import com.ostrichemulators.semtool.ui.main.PlayPane;
import com.ostrichemulators.semtool.poi.main.ImportValidationException;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportFileReader;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.rdf.engine.impl.AbstractSesameEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.BigDataEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.SesameEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineCreateBuilder;
import com.ostrichemulators.semtool.rdf.engine.util.EngineLoader;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineOperationAdapter;
import com.ostrichemulators.semtool.rdf.engine.util.EngineOperationListener;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import com.ostrichemulators.semtool.ui.helpers.StatementsSizeGuesser;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.ui.preferences.SemtoolPreferences;
import com.ostrichemulators.semtool.util.DIHelper;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class ImportCreateDbPanel extends javax.swing.JPanel {

	private static final Logger log = Logger.getLogger( ImportCreateDbPanel.class );
	public static final String METADATABASEURI = "Use Loading Sheet Metadata";

	private boolean loadable = false;
	private Map<JRadioButton, Class<? extends AbstractSesameEngine>> impls
			= new HashMap<>();

	/**
	 * Creates new form ExistingDbPanel
	 */
	public ImportCreateDbPanel() {
		initComponents();

		vocabPanel.setTitle( "Vocabularies" );

		for ( ReificationStyle rs : ReificationStyle.values() ) {
			if ( ReificationStyle.LEGACY != rs ) {
				JRadioButton jrb = new JRadioButton( rs.toString() );
				jrb.setActionCommand( rs.toString() );
				edgegroup.add( jrb );
				edgemodelPanel.add( jrb );

				// for now, we only support SEMTOOL reification
				jrb.setEnabled( ReificationStyle.SEMTOOL == rs );
				jrb.setSelected( ReificationStyle.SEMTOOL == rs );
			}
		}

		impls.put( openrdf, SesameEngine.class );
		impls.put( blazegraph, BigDataEngine.class );

		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		file.setPreferencesKeys( prefs, "lastpath" );
		file.setMultipleFilesOk( true );

		questionfile.setPreferencesKeys( prefs, "lastquestionspath" );

		questionfile.getChooser().setFileFilter( FileBrowsePanel.getInsightTypesFilter() );

		dbdir.setPreferencesKeys( prefs, "lastdbcreatepath" );
		dbdir.setMultipleFilesOk( false );
		dbdir.getChooser().setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		dbdir.setFileTextFromInit();

		baseuri.addItem( METADATABASEURI );
		Set<String> seen = new HashSet<>();
		seen.add( METADATABASEURI );
		for ( String uri : prefs.get( "lastontopath", SEMONTO.NAMESPACE ).split( ";" ) ) {
			if ( !seen.contains( uri ) ) {
				baseuri.addItem( uri );
				seen.add( uri );
			}
		}

		JFileChooser chsr = file.getChooser();
		chsr.addChoosableFileFilter( FileBrowsePanel.getLoadingSheetsFilter( true ) );
		chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter(
				"Turtle Files", "ttl" ) );

		chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter(
				"RDF/XML Files", "rdf" ) );
		chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter(
				"N-Triples Files", "nt" ) );
		chsr.addChoosableFileFilter( new FileBrowsePanel.CustomFileFilter(
				"N3 Files", "n3" ) );
		chsr.setFileFilter( FileBrowsePanel.getAllImportTypesFilter() );

		loadable = false;
		DocumentListener dl = new DocumentListener() {

			@Override
			public void insertUpdate( DocumentEvent e ) {
				check();
			}

			@Override
			public void removeUpdate( DocumentEvent e ) {
				check();
			}

			@Override
			public void changedUpdate( DocumentEvent e ) {
				check();
			}

			private void check() {
				checkOk();
			}
		};
		DocumentListener dl2 = new DocumentListener() {

			@Override
			public void insertUpdate( DocumentEvent e ) {
				check();
			}

			@Override
			public void removeUpdate( DocumentEvent e ) {
				check();
			}

			@Override
			public void changedUpdate( DocumentEvent e ) {
				check();
			}

			private void check() {
				if ( StatementsSizeGuesser.shouldUseDisk( file.getFiles() ) ) {
					diskStaging.setSelected( true );
				}

				File firstfile = file.getFirstFile();
				String dbnamer = ( null == dbname.getText() ? "" : dbname.getText() );
				if ( null != firstfile && dbnamer.isEmpty() ) {
					dbname.setText( FilenameUtils.getBaseName( firstfile.getName() ) );
				}

			}
		};

		dbname.getDocument().addDocumentListener( dl );
		dbdir.addDocumentListener( dl );
		file.addDocumentListener( dl2 );

		Preferences vc = SemtoolPreferences.get();
		calcInfers.setSelected( vc.getBoolean( Constants.CALC_INFERENCES_PREF, true ) );
	}

	private void checkOk() {
		loadable = !( null == dbdir.getFirstFile() || dbname.getText().isEmpty() );
	}

	public boolean isLoadable() {
		return loadable;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings( "unchecked" )
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    edgegroup = new javax.swing.ButtonGroup();
    stagegroup = new javax.swing.ButtonGroup();
    stylegroup = new javax.swing.ButtonGroup();
    jLabel2 = new javax.swing.JLabel();
    file = new com.ostrichemulators.semtool.ui.components.FileBrowsePanel();
    urilbl = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    dbname = new javax.swing.JTextField();
    questionlbl = new javax.swing.JLabel();
    questionfile = new com.ostrichemulators.semtool.ui.components.FileBrowsePanel();
    dbdir = new com.ostrichemulators.semtool.ui.components.FileBrowsePanel();
    jLabel1 = new javax.swing.JLabel();
    baseuri = new javax.swing.JComboBox<String>();
    jPanel2 = new javax.swing.JPanel();
    vocabPanel = new com.ostrichemulators.semtool.ui.components.VocabularyPanel();
    jPanel1 = new javax.swing.JPanel();
    diskStaging = new javax.swing.JRadioButton();
    memoryStaging = new javax.swing.JRadioButton();
    metamodel = new javax.swing.JCheckBox();
    conformer = new javax.swing.JCheckBox();
    edgemodelPanel = new javax.swing.JPanel();
    calcInfers = new javax.swing.JCheckBox();
    jPanel3 = new javax.swing.JPanel();
    jLabel4 = new javax.swing.JLabel();
    blazegraph = new javax.swing.JRadioButton();
    openrdf = new javax.swing.JRadioButton();

    jLabel2.setLabelFor(file);
    jLabel2.setText("Select File(s) to Import");

    urilbl.setText("Designate Base URI");

    jLabel3.setText("New Database Name");

    dbname.setToolTipText("Database name cannot contain spaces");

    questionlbl.setText("Import Insights");

    questionfile.setToolTipText("Enter file path and name or browse to find custom questions sheet");

    jLabel1.setText("Database Location");

    baseuri.setEditable(true);

    vocabPanel.setLayout(new javax.swing.BoxLayout(vocabPanel, javax.swing.BoxLayout.PAGE_AXIS));

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(java.awt.Color.gray, 1, true), "Load Intermediate Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 0, 12))); // NOI18N
    jPanel1.setToolTipText("Where should the raw data be loaded before importing?");

    stagegroup.add(diskStaging);
    diskStaging.setText("On Disk");
    diskStaging.setToolTipText("Loading on disk can be slower, but uses less memory");

    stagegroup.add(memoryStaging);
    memoryStaging.setSelected(true);
    memoryStaging.setText("In Memory");
    memoryStaging.setToolTipText("Loading in memory can be faster, but uses more memory");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(diskStaging)
          .addComponent(memoryStaging))
        .addGap(0, 69, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(diskStaging)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(memoryStaging))
    );

    metamodel.setSelected(true);
    metamodel.setText("Create Metamodel");

    conformer.setText("Check Quality");

    edgemodelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Reification Model", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 0, 12))); // NOI18N
    edgemodelPanel.setLayout(new javax.swing.BoxLayout(edgemodelPanel, javax.swing.BoxLayout.PAGE_AXIS));

    calcInfers.setSelected(true);
    calcInfers.setText("Compute Dependent Relationships");

    jLabel4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel4.setText("Engine Style");

    stylegroup.add(blazegraph);
    blazegraph.setText("Blazegraph (.jnl)");

    stylegroup.add(openrdf);
    openrdf.setSelected(true);
    openrdf.setText("Sesame");

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(blazegraph)
      .addComponent(openrdf)
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addComponent(jLabel4)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(openrdf)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(blazegraph)
        .addGap(0, 0, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(conformer)
              .addComponent(metamodel)
              .addComponent(calcInfers))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(57, 57, 57)))
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(vocabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(edgemodelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(calcInfers)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(metamodel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(conformer)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(vocabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(edgemodelPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
            .addComponent(questionlbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(urilbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(file, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(dbname)
          .addComponent(dbdir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(questionfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(baseuri, 0, 460, Short.MAX_VALUE)))
      .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(urilbl)
          .addComponent(baseuri, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(dbdir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(questionlbl)
          .addComponent(questionfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(file, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

	public static void showDialog( Frame frame ) {
		Object options[] = { "Create DB", "Cancel" };
		ImportCreateDbPanel icdp = new ImportCreateDbPanel();

		boolean ok = false;
		boolean docreate = false;
		while ( !ok ) {
			int opt = JOptionPane.showOptionDialog( frame, icdp,
					"Create New Database", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] );
			ok = icdp.isLoadable();

			if ( 0 == opt ) {
				if ( ok ) {
					docreate = true;
				}
				else {
					JOptionPane.showMessageDialog( frame,
							"You must specify a DB location and name", "Inconsistent Data",
							JOptionPane.ERROR_MESSAGE );
				}
			}
			else {
				ok = true;
			}
		}

		if ( docreate ) {
			try {
				icdp.doCreate();
			}
			catch ( IOException | ImportValidationException e ) {
				log.error( e, e );
				GuiUtility.showError( e.getLocalizedMessage() );
			}
		}
	}

	public void doCreate() throws ImportValidationException, IOException {
		String mybase = baseuri.getSelectedItem().toString();

		final boolean stageInMemory = memoryStaging.isSelected();
		final boolean calc = calcInfers.isSelected();
		final boolean dometamodel = metamodel.isSelected();
		final boolean conformance = conformer.isSelected();

		ButtonModel bm = edgegroup.getSelection();
		final ReificationStyle reif = ReificationStyle.valueOf( bm.getActionCommand() );

		Collection<File> files = file.getFiles();

		URI defaultBase = null;
		if ( null == mybase || mybase.isEmpty() || METADATABASEURI.equals( mybase ) ) {
			Set<URI> uris = new HashSet<>();
			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			String basepref = prefs.get( "lastontopath", SEMONTO.NAMESPACE );
			for ( String b : basepref.split( ";" ) ) {
				uris.add( new URIImpl( b ) );
			}

			defaultBase = ( files.isEmpty() ? Constants.ANYNODE
					: getDefaultBaseUri( files, uris ) );

			// save the default base for next time
			if ( null == defaultBase ) {
				return; // user canceled
			}
			else if ( !Constants.ANYNODE.equals( defaultBase ) ) {
				// user specified something
				uris.add( defaultBase );
				StringBuilder sb = new StringBuilder();
				for ( URI u : uris ) {
					if ( 0 != sb.length() ) {
						sb.append( ";" );
					}
					sb.append( u.stringValue() );
				}
				prefs.put( "lastontopath", sb.toString() );
			}
			// else {} // every file has a base URI specified
		}
		else {
			defaultBase = new URIImpl( mybase );
		}

		final URI defaultBaseUri = defaultBase;
		String title = "Creating Database from " + ( file.getFilePaths().length > 1
				? "multiple files" : file.getDelimitedPaths() );

		ProgressTask pt = new ProgressTask( title, new Runnable() {
			@Override
			public void run() {
				final File smss[] = { null };
				ImportData errors = ( conformance ? new ImportData() : null );
				final EngineUtil eutil = EngineUtil.getInstance();

				EngineOperationListener eol = new EngineOperationAdapter() {

					@Override
					public void engineOpened( IEngine eng ) {
						String smssloc = eng.getProperty( Constants.SMSS_LOCATION );
						if ( null != smss[0]
								&& smssloc.equals( smss[0].getAbsolutePath() ) ) {
							eutil.removeEngineOpListener( this );
							GuiUtility.showMessage(
									"Your database has been successfully created!" );

							if ( conformance && !errors.isEmpty() ) {
								LoadingPlaySheetFrame psf
										= new LoadingPlaySheetFrame( eng, errors );
								psf.setTitle( "Conformance Check Errors" );
								DIHelper.getInstance().getDesktop().add( psf );
							}
						}
					}
				};

				eutil.addEngineOpListener( eol );

				try {
					EngineCreateBuilder ecb
							= new EngineCreateBuilder( dbdir.getFirstFile(), dbname.getText() );

					File insights = questionfile.getFirstFile();

					JRadioButton jrb = ( openrdf.isSelected() ? openrdf : blazegraph );

					ecb.setDefaultBaseUri( defaultBaseUri,
							defaultBaseUri.toString().equals( baseuri.getSelectedItem().toString() ) )
							.setReificationModel( reif )
							.setInsightsFile( insights )
							.setFiles( files )
							.setEngineImpl( impls.get( jrb ) )
							.setBooleans( stageInMemory, calc, dometamodel )
							.setVocabularies( vocabPanel.getSelectedVocabularies() );

					smss[0] = EngineUtil2.createNew( ecb, errors );
					EngineUtil.getInstance().mount( smss[0], true );
				}
				catch ( IOException | EngineManagementException ioe ) {
					log.error( ioe, ioe );
					GuiUtility.showError( ioe.getMessage() );
				}
			}
		} );
		OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox<String> baseuri;
  private javax.swing.JRadioButton blazegraph;
  private javax.swing.JCheckBox calcInfers;
  private javax.swing.JCheckBox conformer;
  private com.ostrichemulators.semtool.ui.components.FileBrowsePanel dbdir;
  private javax.swing.JTextField dbname;
  private javax.swing.JRadioButton diskStaging;
  private javax.swing.ButtonGroup edgegroup;
  private javax.swing.JPanel edgemodelPanel;
  private com.ostrichemulators.semtool.ui.components.FileBrowsePanel file;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JRadioButton memoryStaging;
  private javax.swing.JCheckBox metamodel;
  private javax.swing.JRadioButton openrdf;
  private com.ostrichemulators.semtool.ui.components.FileBrowsePanel questionfile;
  private javax.swing.JLabel questionlbl;
  private javax.swing.ButtonGroup stagegroup;
  private javax.swing.ButtonGroup stylegroup;
  private javax.swing.JLabel urilbl;
  private com.ostrichemulators.semtool.ui.components.VocabularyPanel vocabPanel;
  // End of variables declaration//GEN-END:variables

	/**
	 * Checks every file to make sure it has a base uri set. If any files are
	 * missing a base uri, ask the user to specify one
	 *
	 * @param files the files to check
	 * @param choices choices for a dropdown for the user
	 * @return the URI the user chose, null if the user canceled, or
	 * {@link Constants#ANYNODE} if every file has a base URI specified
	 * @throws com.ostrichemulators.semtool.poi.main.ImportValidationException
	 * @throws java.io.IOException
	 */
	public static URI getDefaultBaseUri( Collection<File> files, Collection<URI> choices )
			throws ImportValidationException, IOException {
		Set<String> bases = new HashSet<>();

		URI choice = null;
		boolean everyFileHasBase = true;

		for ( File f : files ) {
			ImportFileReader reader = EngineLoader.getDefaultReader( f );
			if ( null != reader ) { // triples files don't have custom readers
				ImportMetadata metadata = reader.getMetadata( f );

				URI baser = metadata.getBase();
				if ( null == baser ) {
					everyFileHasBase = false;
				}
				else {
					bases.add( metadata.getBase().stringValue() );
				}
			}
		}

		if ( everyFileHasBase ) {
			// nothing to do here
			return Constants.ANYNODE;
		}
		else {
			if ( bases.isEmpty() ) {
				JComboBox<String> box = new JComboBox<>();
				box.addItem( "" );

				for ( URI item : choices ) {
					box.addItem( item.stringValue() );
				}
				box.setEditable( true );

				JPanel pnl = new JPanel();
				pnl.setLayout( new BoxLayout( pnl, BoxLayout.LINE_AXIS ) );
				pnl.add( new JLabel( "<html>Not all the files have Base URIs set.<br>"
						+ "Please specify a Base URI to use<br>when one is not provided." ) );
				JPanel junk = new JPanel();
				junk.add( box );
				pnl.add( junk );

				int opt = JOptionPane.showOptionDialog( null, pnl, "Specify the Base URI",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
						null, null );
				if ( JOptionPane.OK_OPTION != opt ) {
					log.debug( "create canceled" );
					return null;
				}

				choice = new URIImpl( box.getSelectedItem().toString() );
			}
		}

		return choice;
	}
}
