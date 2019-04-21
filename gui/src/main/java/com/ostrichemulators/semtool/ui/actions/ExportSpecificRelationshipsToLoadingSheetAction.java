package com.ostrichemulators.semtool.ui.actions;

import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.XlsWriter;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.DBToLoadingSheetExporter;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManager;
import com.ostrichemulators.semtool.rdf.engine.util.StructureManagerFactory;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.GuiUtility;
import com.ostrichemulators.semtool.ui.components.ExportSpecificNodesPanel;
import com.ostrichemulators.semtool.ui.components.LoadingPlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.OperationsProgress;
import com.ostrichemulators.semtool.ui.components.PlaySheetFrame;
import com.ostrichemulators.semtool.ui.components.ProgressTask;

import com.ostrichemulators.semtool.ui.components.IriComboBox;
import com.ostrichemulators.semtool.util.Utility;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;

/**
 * @author john.marquiss
 */
public class ExportSpecificRelationshipsToLoadingSheetAction extends DbAction {

  private static final long serialVersionUID = 74365889907490137L;
  private static final Logger log
      = Logger.getLogger( ExportSpecificRelationshipsToLoadingSheetAction.class );
  private final Frame owner;
  private JDialog dialog;

  private JLabel maxExportLimitLabel, subjectJLabel, relationJLabel, objectJLabel;
  private JButton exportButton, addRelationshipButton;
  private List<IriComboBox> subjectComboBoxes;
  private List<IriComboBox> relationComboBoxes;
  private List<IriComboBox> objectComboBoxes;
  private List<JButton> removeRowButtons;

  private File exportFile;
  private String successMessage = "";
  private List<IRI[]> selectedTriples;
  private final JCheckBox togrid = new JCheckBox( "Export to Grid" );
  private StructureManager structures;

  public ExportSpecificRelationshipsToLoadingSheetAction( String optg, Frame _owner ) {
    super( optg, EXPORTLSSOMERELS, "excel" );

    this.owner = _owner;
    super.putValue( AbstractAction.SHORT_DESCRIPTION, "Export Specific Relationships" );
    super.putValue( AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S );
  }

  @Override
  public void actionPerformed( ActionEvent e ) {
    dialog = new JDialog( owner, true );
    dialog.setTitle( "Export Specified Relationships to Loading Sheets" );
    dialog.add( buildPanel() );
    dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
    dialog.pack();
    dialog.setLocationRelativeTo( owner );
    dialog.setVisible( true );
  }

  private JPanel buildPanel() {
    subjectJLabel = new JLabel( "Subject Node" );
    relationJLabel = new JLabel( "Relatonship Node" );
    objectJLabel = new JLabel( "Object Node" );

    removeRowButtons = new ArrayList<>();
    subjectComboBoxes = new ArrayList<>();
    relationComboBoxes = new ArrayList<>();
    objectComboBoxes = new ArrayList<>();
    for ( int i = 0; i < Constants.MAX_EXPORTS; i++ ) {
      createAndAddRemoveRowButton();
      createAndAddSubjectComboBox();
      createAndAddRelationshipComboBox();
      createAndAddObjectComboBox();
    }

    addRelationshipButton = new JButton( "+" );
    addRelationshipButton.setToolTipText( "Add a relationship to the export, for a max of "
        + Constants.MAX_EXPORTS + "." );
    addRelationshipButton.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent evt ) {
        addARow();
      }
    } );

    maxExportLimitLabel = new JLabel( "Max Export Limit: " + Constants.MAX_EXPORTS );
    maxExportLimitLabel.setVisible( false );

    exportButton = new JButton( "Export" );
    exportButton.setToolTipText( "Export specified triples" );
    exportButton.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent evt ) {
        exportButtonActionPerformed( evt );
      }
    } );

    initAllNodes();

    ActionListener objectActionListener = new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent evt ) {
        objectSelectedActionPerformed( getIndexNumber( evt ) );
      }
    };
    for ( IriComboBox cb : objectComboBoxes ) {
      cb.addActionListener( objectActionListener );
    }

    ActionListener subjectActionListener = new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent evt ) {
        subjectSelectedActionPerformed( evt );
      }
    };
    for ( IriComboBox cb : subjectComboBoxes ) {
      cb.addActionListener( subjectActionListener );
    }

    JPanel dbExportPanel = new JPanel();

    GroupLayout layout = new GroupLayout( dbExportPanel );
    layout.setAutoCreateGaps( true );
    layout.setAutoCreateContainerGaps( true );
    layout.setHorizontalGroup( makeHorizontalGroup( layout ) );
    layout.setVerticalGroup( makeVerticalGroup( layout ) );

    dbExportPanel.setLayout( layout );

    // get the object, relationship dropdowns updated
    subjectSelectedActionPerformed( new ActionEvent( subjectComboBoxes.get( 0 ),
        1, null ) );
    return dbExportPanel;
  }

  private ParallelGroup makeHorizontalGroup( GroupLayout layout ) {
    ParallelGroup removeButtonGroup = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
    for ( JButton removeRowButton : removeRowButtons ) {
      removeButtonGroup.addComponent( removeRowButton );
    }
    removeButtonGroup.addComponent( addRelationshipButton );

    ParallelGroup subjectCBGroup = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
    subjectCBGroup.addComponent( subjectJLabel );
    for ( IriComboBox subjectCB : subjectComboBoxes ) {
      subjectCBGroup.addComponent( subjectCB );
    }
    subjectCBGroup.addComponent( maxExportLimitLabel );

    ParallelGroup relationCBGroup = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
    relationCBGroup.addComponent( relationJLabel );
    for ( IriComboBox relationCB : relationComboBoxes ) {
      relationCBGroup.addComponent( relationCB );
    }

    ParallelGroup objectCBGroup = layout.createParallelGroup( GroupLayout.Alignment.LEADING );
    objectCBGroup.addComponent( objectJLabel );
    for ( IriComboBox objectCB : objectComboBoxes ) {
      objectCBGroup.addComponent( objectCB );
    }

    ParallelGroup horizontalGroup = layout.createParallelGroup( GroupLayout.Alignment.TRAILING )
        .addGroup( layout.createSequentialGroup()
            .addGroup( removeButtonGroup )
            .addGroup( subjectCBGroup )
            .addGroup( relationCBGroup )
            .addGroup( objectCBGroup )
        )
        .addGroup( layout.createSequentialGroup()
            .addComponent( togrid )
            .addComponent( exportButton ) );

    return horizontalGroup;
  }

  private SequentialGroup makeVerticalGroup( GroupLayout layout ) {
    SequentialGroup verticalGroup = layout.createSequentialGroup();

    verticalGroup.addGroup( layout.createParallelGroup( GroupLayout.Alignment.BASELINE )
        .addComponent( subjectJLabel )
        .addComponent( relationJLabel )
        .addComponent( objectJLabel )
    );

    for ( int i = 0; i < Constants.MAX_EXPORTS; i++ ) {
      verticalGroup.addGroup( layout.createParallelGroup( GroupLayout.Alignment.BASELINE )
          .addComponent( removeRowButtons.get( i ) )
          .addComponent( subjectComboBoxes.get( i ) )
          .addComponent( relationComboBoxes.get( i ) )
          .addComponent( objectComboBoxes.get( i ) )
      );
    }

    verticalGroup.addComponent( maxExportLimitLabel );
    verticalGroup.addGroup( layout.createParallelGroup( GroupLayout.Alignment.BASELINE )
        .addComponent( addRelationshipButton )
        .addComponent( togrid )
        .addComponent( exportButton )
    );

    return verticalGroup;
  }

  private void exportButtonActionPerformed( ActionEvent evt ) {
    selectedTriples = getSelectedTriples();
    if ( selectedTriples == null || selectedTriples.isEmpty() ) {
      GuiUtility.showMessage( "You must select at least one relationship to export." );
      return;
    }

    if ( !togrid.isSelected() ) {
      exportFile
          = ExportSpecificNodesPanel.getLoadingSheetExportFileLocationFromUser( "Relationships",
              dialog );
      if ( exportFile == null ) {
        return;
      }
    }

    runExport();
  }

  private List<IRI[]> getSelectedTriples() {
    selectedTriples = new ArrayList<>();

    for ( int i = 0; i < Constants.MAX_EXPORTS; i++ ) {
      IriComboBox subject = subjectComboBoxes.get( i );
      IriComboBox relationship = relationComboBoxes.get( i );
      IriComboBox object = objectComboBoxes.get( i );

      if ( subject.isVisible()
          && subject.getSelectedItem() != null
          && !subject.getSelectedItem().toString().equals( "" )
          && !subject.getSelectedItem().equals( Constants.ANYNODE )
          && relationship.getSelectedItem() != null
          && !relationship.getSelectedItem().toString().equals( "" )
          && !relationship.getSelectedItem().equals( Constants.ANYNODE )
          && object.getSelectedItem() != null
          && !object.getSelectedItem().toString().equals( "" )
          && !object.getSelectedItem().equals( Constants.ANYNODE ) ) {

        IRI[] spo = { subject.getSelectedIri(), relationship.getSelectedIri(),
          object.getSelectedIri() };
        selectedTriples.add( spo );
      }
    }

    return selectedTriples;
  }

  private void runExport() {
    dialog.setVisible( false );

    StringBuilder sb = new StringBuilder( "Exporting Specific Relationship Loading Sheets of " );
    sb.append( getEngine().getEngineName() ).append( " to " );
    sb.append( togrid.isSelected() ? "Grid" : exportFile.getAbsolutePath() );

    ProgressTask progressTask = new ProgressTask( sb.toString(), new Runnable() {
      @Override
      public void run() {
        DBToLoadingSheetExporter exper = new DBToLoadingSheetExporter( getEngine() );
        ImportData data = EngineUtil2.createImportData( getEngine() );
        for ( IRI[] triples : selectedTriples ) {
          exper.exportOneRelationship( triples[0], triples[1], triples[2], data );
        }

        if ( togrid.isSelected() ) {
          PlaySheetFrame psf = new LoadingPlaySheetFrame( getEngine(), data );
          psf.setTitle( "Loading Sheet Export" );
          DIHelper.getInstance().getDesktop().add( psf );
          successMessage = null;
        }
        else {
          XlsWriter writer = new XlsWriter();
          try {
            writer.write( data, exportFile );
            successMessage = sb.toString().replaceAll( "^Exporting",
                "Successfully exported" );
          }
          catch ( IOException ioe ) {
            log.error( ioe, ioe );
            successMessage = "Export failed: " + ioe.getLocalizedMessage();
          }
        }
      }
    } ) {
      @Override
      public void done() {
        super.done();

        if ( successMessage != null && !successMessage.equals( "" ) ) {
          GuiUtility.showMessage( successMessage );
        }
      }
    };

    OperationsProgress.getInstance( "UI" ).add( progressTask );
  }

  private void createAndAddRemoveRowButton() {
    JButton removeRowButton = new JButton( "-" );
    removeRowButton.setToolTipText( "Remove This Row" );
    removeRowButton.setName( "" + removeRowButtons.size() );
    removeRowButton.setFocusable( false );
    removeRowButton.addActionListener( new ActionListener() {
      @Override
      public void actionPerformed( ActionEvent evt ) {
        removeThisRow( getIndexNumber( evt ) );
      }
    } );
    removeRowButtons.add( removeRowButton );
  }

  private void createAndAddSubjectComboBox() {
    IriComboBox cb = new IriComboBox();
    cb.setToolTipText( "Select node that is to be the subject of a triple" );
    cb.setName( Integer.toString( subjectComboBoxes.size() ) );
    subjectComboBoxes.add( cb );
  }

  private void createAndAddRelationshipComboBox() {
    IriComboBox cb = new IriComboBox();
    cb.setToolTipText( "Select relationship between subject and object nodes" );
    cb.setName( Integer.toString( relationComboBoxes.size() ) );
    relationComboBoxes.add( cb );
  }

  private void createAndAddObjectComboBox() {
    IriComboBox cb = new IriComboBox();
    cb.setToolTipText( "Select node that is to be the object of a triple" );
    cb.setName( Integer.toString( objectComboBoxes.size() ) );
    objectComboBoxes.add( cb );
  }

  private int getIndexNumber( ActionEvent evt ) {
    return Integer.parseInt( ( (Component) evt.getSource() ).getName() );
  }

  private void addARow() {
    for ( int i = 0; i < Constants.MAX_EXPORTS; i++ ) {
      JButton removeButton = removeRowButtons.get( i );
      IriComboBox subject = subjectComboBoxes.get( i );
      IriComboBox relation = relationComboBoxes.get( i );
      IriComboBox object = objectComboBoxes.get( i );

      if ( !subject.isVisible() ) {
        removeButton.setVisible( true );
        subject.setVisible( true );
        object.setVisible( true );
        relation.setVisible( true );

        break;
      }
    }

    int numvis = getNumberOfVisibleRows();
    if ( Constants.MAX_EXPORTS == numvis ) {
      addRelationshipButton.setVisible( false );
      maxExportLimitLabel.setVisible( true );
    }

    subjectSelectedActionPerformed(
        new ActionEvent( subjectComboBoxes.get( numvis - 1 ), 1, null ) );

    dialog.pack();
  }

  private void removeThisRow( int i ) {
    JButton removeButton = removeRowButtons.get( i );
    IriComboBox subject = subjectComboBoxes.get( i );
    IriComboBox relation = relationComboBoxes.get( i );
    IriComboBox object = objectComboBoxes.get( i );

    if ( getNumberOfVisibleRows() == 1 ) {
      subject.setSelectedIndex( 0 );
      initializeComboBox( relation, true );
      initializeComboBox( object, true );
      return;
    }

    if ( getNumberOfVisibleRows() == 9 ) {
      addRelationshipButton.setVisible( true );
      maxExportLimitLabel.setVisible( false );
    }

    removeButton.setVisible( false );
    subject.setVisible( false );
    subject.setSelectedIndex( 0 );

    initializeComboBox( relation, false );
    initializeComboBox( object, false );

    dialog.pack();
  }

  private int getNumberOfVisibleRows() {
    int numVisibleRows = 0;
    for ( int i = 0; i < Constants.MAX_EXPORTS; i++ ) {
      if ( subjectComboBoxes.get( i ).isVisible() ) {
        numVisibleRows++;
      }
    }

    return numVisibleRows;
  }

  private void initAllNodes() {
    initializeComboBox( relationComboBoxes.get( 0 ), true );
    initializeComboBox( objectComboBoxes.get( 0 ), true );
    structures = StructureManagerFactory.getStructureManager( getEngine() );

    //populate all of the subject combo boxes
    Map<IRI, String> labels = new HashMap<>();
    Set<IRI> IRIs = structures.getTopLevelConcepts();
    labels.putAll( Utility.getInstanceLabels( IRIs, getEngine() ) );

    for ( int i = 1; i < Constants.MAX_EXPORTS; i++ ) {
      removeRowButtons.get( i ).setVisible( false );
      initializeComboBox( subjectComboBoxes.get( i ), false );
      initializeComboBox( objectComboBoxes.get( i ), false );
      initializeComboBox( relationComboBoxes.get( i ), false );
    }

    addRelationshipButton.setVisible( true );
    maxExportLimitLabel.setVisible( false );

    for ( IriComboBox subj : subjectComboBoxes ) {
      subj.setData( labels );
    }
  }

  private void initializeComboBox( IriComboBox comboBox, boolean visibility ) {
    comboBox.clear();
    comboBox.setEditable( false );
    comboBox.setVisible( visibility );
  }

  /**
   * Method objectSelectedActionPerformed. Dictates what actions to take when an
   * Action Event is performed.
   *
   * @param arg0 ActionEvent - The event that triggers the actions in the
   * method.
   */
  private void objectSelectedActionPerformed( int index ) {
    IriComboBox subjectCB = subjectComboBoxes.get( index );
    IriComboBox relCB = relationComboBoxes.get( index );
    IriComboBox objectCB = objectComboBoxes.get( index );
    StructureManager sm = StructureManagerFactory.getStructureManager( getEngine() );

    Model model = sm.getLinksBetween( subjectCB.getSelectedIri(),
        objectCB.getSelectedIri() );
    Set<IRI> values = new HashSet<>( model.predicates() );
    if ( model.isEmpty() ) {
      values.add( Constants.ANYNODE );
    }

    relCB.setEditable( false );
    relCB.setData( Utility.getInstanceLabels( values, getEngine() ) );

    dialog.pack();
  }

  /**
   * Method subjectSelectedActionPerformed. Dictates what actions to take when
   * an Action Event is performed.
   *
   * @param arg0 ActionEvent - The event that triggers the actions in the
   * method.
   */
  private void subjectSelectedActionPerformed( ActionEvent evt ) {
    if ( !( evt.getSource() instanceof JComboBox<?> ) ) {
      return;
    }

    IriComboBox subjectNodeTypeComboBox = IriComboBox.class.cast( evt.getSource() );
    IRI subjectNodeType = subjectNodeTypeComboBox.getSelectedIri();

    if ( subjectNodeType != null ) {
      List<IRI> results = getDestinationConcepts( subjectNodeType );
      int index = getIndexNumber( evt );

      IriComboBox objs = objectComboBoxes.get( index );
      objs.setData( Utility.getInstanceLabels( results, getEngine() ) );
      objs.setEditable( false );

      if ( objs.getSelectedItem() != null ) {
        objectSelectedActionPerformed( index );
      }
    }
  }

  /**
   * Method runObjectsQuery. Runs the query given an input node type.
   *
   * @param nodeType String
   */
  private List<IRI> getDestinationConcepts( IRI nodeType ) {
    Model model = structures.getLinksBetween( nodeType, Constants.ANYNODE );
    Set<IRI> values = new HashSet<>();
    for ( Value v : model.objects() ) {
      values.add( IRI.class.cast( v ) );
    }

    if ( values.isEmpty() ) {
      values.add( Constants.ANYNODE );
    }

    return new ArrayList<>( values );
  }

  @Override
  public IEngine getEngine() {
    if ( super.getEngine() == null ) {
      return DIHelper.getInstance().getRdfEngine();
    }

    return super.getEngine();
  }

  @Override
  protected ProgressTask getTask( ActionEvent e ) {
    throw new UnsupportedOperationException( "not supported" );
  }
}
