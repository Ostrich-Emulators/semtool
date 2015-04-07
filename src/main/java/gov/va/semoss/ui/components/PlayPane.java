/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * *************************style***************************************************
 */
package gov.va.semoss.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.api.IChakraListener;
import gov.va.semoss.ui.main.listener.impl.ProcessQueryListener;
import gov.va.semoss.ui.swing.custom.CustomAruiStyle;
import gov.va.semoss.ui.swing.custom.CustomButton;
import gov.va.semoss.ui.swing.custom.CustomDesktopPane;
import gov.va.semoss.util.CSSApplication;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import aurelienribon.ui.css.Style;
import aurelienribon.ui.css.swing.SwingStyle;

import com.ibm.icu.util.StringTokenizer;

import java.lang.reflect.Field;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.ui.actions.CheckConsistencyAction;
import gov.va.semoss.ui.actions.ClearAction;
import gov.va.semoss.ui.actions.CloneAction;
import gov.va.semoss.ui.actions.CreateDbAction;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.actions.EndpointAction;
import gov.va.semoss.ui.actions.ExportInsightsAction;
import gov.va.semoss.ui.actions.ExportLoadingSheetAction;
import gov.va.semoss.ui.actions.ExportSpecificNodesToLoadingSheetAction;
import gov.va.semoss.ui.actions.ExportSpecificRelationshipsToLoadingSheetAction;
import gov.va.semoss.ui.actions.ExportTtlAction;
import gov.va.semoss.ui.actions.ImportInsightsAction;
import gov.va.semoss.ui.actions.ImportLoadingSheetAction;
import gov.va.semoss.ui.actions.MergeAction;
import gov.va.semoss.ui.actions.MountAction;
import gov.va.semoss.ui.actions.PinAction;
import gov.va.semoss.ui.actions.PropertiesAction;
import gov.va.semoss.ui.actions.UnmountAction;
import gov.va.semoss.ui.components.playsheets.AbstractRDFPlaySheet;
import gov.va.semoss.ui.main.SemossPreferences;
import gov.va.semoss.util.DefaultPlaySheetIcons;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;

import org.apache.tika.io.IOUtils;

/**
 * The playpane houses all of the components that create the user interface in
 * SEMOSS.
 */
public class PlayPane extends JFrame {

	private static final long serialVersionUID = -715188668604903980L;
	private static final Logger logger = Logger.getLogger( PlayPane.class );
	private final String GCOSMETICS = "graphcosmetics";
	private final String GFILTER = "graphfilter";
	private final String LOGGING = "loggingpanel";
	private final String QUERYPANEL = "customSparqlPanel";
	public static final String UIPROGRESS = "UI";

	// Left Control Panel Components
	public JComboBox<Perspective> perspectiveSelector;
	public JComboBox<Insight> questionSelector;
	public JPanel paramPanel;
	public JButton submitButton;

	public JCheckBox appendChkBox;
	public RepositoryList repoList = new RepositoryList();

	//The "Custom Sparql Query" window, and related controls,
	//exist in a separate class:
	private final CustomSparqlPanel csp = new CustomSparqlPanel();
	//private JSeparator separator_1;

	// Right graphPanel desktopPane
	private JDesktopPane desktopPane;
	public JButton refreshButton;
	public JTable filterTable, edgeTable, propertyTable;
	private JScrollPane filterSliders;

	// left cosmetic panel components
	public JTable colorShapeTable, sizeTable;
	public JButton btnResetDefaults = new CustomButton( "Reset Defaults" ); //JPM unused

	// Left label panel
	public JTable labelTable, tooltipTable;

	// SUDOWL Panel Components
	private JPanel owlPanel;
	private JPanel cosmeticsPanel;
	private JPanel filterPanel;
	private LoggingPanel loggingPanel;
	public JTable objectPropertiesTable, dataPropertiesTable;
	public JTextField dataPropertiesString, objectPropertiesString;
	public JButton btnRepaintGraph;

	// Custom Update Components
	public JButton btnCustomUpdate;
	public JTextPane customUpdateTextPane;

	// Import Components
	public JComboBox<String> dbImportTypeComboBox, loadingFormatComboBox;
	public JPanel advancedImportOptionsPanel, dbImportPanel;
	public JTextField importFileNameField, customBaseURItextField,
			importMapFileNameField, dbPropFileNameField, questionFileNameField,
			dbSelectorField, dbImportURLField, dbImportUsernameField;
	public JPasswordField dbImportPWField;
	//V-CAMP RTM Import ("rtmLoadButton"):
	public JButton mapBrowseBtn, dbPropBrowseButton, questionBrowseButton,
			btnShowAdvancedImportFeatures, importButton, rtmLoadButton, fileBrowseBtn,
			btnTestRDBMSConnection, btnGetRDBMSSchema;

	public JLabel lblSelectOneFile, selectionFileLbl, dbNameLbl, lblDataInputFormat,
			lblDBImportURL, lblDesignateBaseUri, lblDBImportUsername, lblDBImportPW,
			lblDBImportDriverType;

	public JButton saveSudowl;
	public JButton vcampModuleBtn;
	protected final JTabbedPane leftTabs;
	protected final JTabbedPane rightTabs;

	private final StatusBar statusbar;

	private final DbAction creater = new CreateDbAction( UIPROGRESS, this );
	private final DbAction mounter = new MountAction( UIPROGRESS, this );
	private final DbAction toggler = new PinAction( UIPROGRESS );
	private final DbAction proper = new PropertiesAction( this );
	private final DbAction cloner = new CloneAction( UIPROGRESS, this, true );
	private final DbAction cloneconfer = new CloneAction( UIPROGRESS, this, false );
	private final DbAction clearer = new ClearAction( UIPROGRESS, this );
	private final DbAction exportttl = new ExportTtlAction( UIPROGRESS,
			ExportTtlAction.Style.TTL, this );
	private final DbAction exportnt = new ExportTtlAction( UIPROGRESS,
			ExportTtlAction.Style.NT, this );
	private final DbAction exportrdf = new ExportTtlAction( UIPROGRESS,
			ExportTtlAction.Style.RDF, this );
	private final DbAction exportinsights
			= new ExportInsightsAction( UIPROGRESS, this );
	//**//
	private final DbAction expall = new ExportLoadingSheetAction( UIPROGRESS,
			this, true, true );
	private final DbAction exprels = new ExportLoadingSheetAction( UIPROGRESS,
			this, false, true );
	private final DbAction expnodes = new ExportLoadingSheetAction( UIPROGRESS,
			this, true, false );
	private final DbAction expSpecNodes
			= new ExportSpecificNodesToLoadingSheetAction( UIPROGRESS, this );
	private final DbAction expSpecRels
			= new ExportSpecificRelationshipsToLoadingSheetAction( UIPROGRESS, this );
	private final DbAction unmounter = new UnmountAction( this );
	private final EndpointAction sparqler = new EndpointAction( UIPROGRESS, this );
	private final ImportLoadingSheetAction importls
			= new ImportLoadingSheetAction( UIPROGRESS, this );
	//private final ImportLoadingSheetAction importxls
	//		= new ImportLoadingSheetAction( UIPROGRESS, "Open", this, "open-file3" );
	private final ImportInsightsAction resetInsights
			= new ImportInsightsAction( UIPROGRESS, true, this );
	private final ImportInsightsAction importInsights
			= new ImportInsightsAction( UIPROGRESS, false, this );
	// private final ImportCapBPAction importCapBp = new ImportCapBPAction( UIPROGRESS, this );
	private final CheckConsistencyAction consistencyCheck
			= new CheckConsistencyAction( UIPROGRESS, this );
	private final JMenu windowSelector = new JMenu( "Window" );

	private final JToolBar toolbar;
	private final JToolBar playsheetToolbar;
	private final JSplitPane mainSplitPane;
	private final JSplitPane combinedSplitPane;
	private final JPanel customSparqlPanel;

	/**
	 * Launch the application.
	 *
	 * @throws java.lang.Exception
	 */
	public void start() throws Exception {
		//Since the "Custom Sparql Query" window, and related controls, 
		//exist in a separate class, load all of their listeners first:
		csp.loadCustomSparqlPanelListeners();

		// load all the listeners
		// cast it to IChakraListener
		// for each listener specify what is the view field - Listener_VIEW
		// for each listener specify the right panel field -
		// Listener_RIGHT_PANEL
		// utilize reflection to get all the fields
		// for each field go into the properties file and find any of the
		// listeners
		// Drop down scrollbars
		for ( JComboBox combo : new JComboBox[]{ questionSelector, perspectiveSelector } ) {
			Object popup = combo.getUI().getAccessibleChild( combo, 0 );
			Component c = Container.class.cast( popup ).getComponent( 0 );
			if ( c instanceof JScrollPane ) {
				JScrollPane.class.cast( c ).getVerticalScrollBar()
						.setUI( new NewScrollBarUI() );
			}
		}

		java.lang.reflect.Field[] fields = getClass().getFields();

		// run through the view components
		for ( Field field : fields ) {
			// logger.info(fields[fieldIndex].getName());
			Object obj = field.get( this );
			String fieldName = field.getName();

			logger.debug( "Checking for listeners for PlayPane." + fieldName );

			if ( obj instanceof JComboBox || obj instanceof JButton
					|| obj instanceof JToggleButton || obj instanceof JSlider
					|| obj instanceof JInternalFrame
					|| obj instanceof JRadioButton || obj instanceof JTextArea ) {
				// load the controllers
				// find the view
				// right view and listener
				String ctrlNames
						= DIHelper.getInstance().getProperty( fieldName + "_" + Constants.CONTROL );
				if ( !( ctrlNames == null || ctrlNames.isEmpty() ) ) {
					logger.debug( "Defined listeners: " + ctrlNames );
					StringTokenizer listenerTokens = new StringTokenizer( ctrlNames, ";" );
					while ( listenerTokens.hasMoreTokens() ) {
						String ctrlName = listenerTokens.nextToken();
						logger.debug( "Creating new instance of: " + ctrlName );
						String className = DIHelper.getInstance().getProperty( ctrlName );
						final IChakraListener listener
								= IChakraListener.class.cast( Class.forName( className ).
										getConstructor().newInstance() );
						// check to if this is a combobox or button
						if ( obj instanceof JComboBox<?> ) {
							( (JComboBox<?>) obj ).addActionListener( listener );
						}
						else if ( obj instanceof JButton ) {
							JButton btn = JButton.class.cast( obj );
							btn.addActionListener( new ActionListener() {

								@Override
								public void actionPerformed( final ActionEvent e ) {
									ProgressTask pt = new ProgressTask( "Executing Query", new Runnable() {

										@Override
										public void run() {
											listener.actionPerformed( e );
										}

									} );
									OperationsProgress.getInstance( UIPROGRESS ).add( pt );
								}
							} );
						}
						else if ( obj instanceof JRadioButton ) {
							( (JRadioButton) obj ).addActionListener( listener );
						}
						else if ( obj instanceof JToggleButton ) {
							( (JToggleButton) obj ).addActionListener( listener );
						}
						else if ( obj instanceof JSlider ) {
							( (JSlider) obj ).addChangeListener( (ChangeListener) listener );
						}
						else if ( obj instanceof JTextArea ) {
							( (JTextArea) obj ).addFocusListener( (FocusListener) listener );
						}
						else {
							( (JInternalFrame) obj ).addInternalFrameListener( (InternalFrameListener) listener );
						}

						logger.debug( "Loading " + ctrlName + " to local prop cache" );
						DIHelper.getInstance().setLocalProperty( ctrlName, listener );
					}
				}
			}
			logger.debug( "Loading " + fieldName + " to local prop cache" );
			DIHelper.getInstance().setLocalProperty( fieldName, obj );
		}

		// need to also add the listeners respective views
		// Go through the listeners and add the model
		String listeners = DIHelper.getInstance().getProperty( Constants.LISTENERS );
		StringTokenizer lTokens = new StringTokenizer( listeners, ";" );
		while ( lTokens.hasMoreElements() ) {
			String lToken = lTokens.nextToken();

			// set the views
			String viewName = DIHelper.getInstance().getProperty( lToken + "_" + Constants.VIEW );
			Object listener = DIHelper.getInstance().getLocalProp( lToken );
			if ( viewName != null && listener != null ) {
				// get the listener object and set it
				Method method = listener.getClass().getMethod( "setView", JComponent.class );
				Object param = DIHelper.getInstance().getLocalProp( viewName );
				logger.debug( "Param is <" + viewName + "><" + param + ">" );
				method.invoke( listener, param );
			}
		}

		statusbar.addStatus( "SEMOSS started" );
	}

	public void setApplicationIcons() {
	}

	/**
	 * Create the frame.
	 *
	 * @throws java.io.IOException
	 */
	public PlayPane() throws IOException {
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		setApplicationIcons();
		setTitle( "SEMOSS - Analytics Environment" );
		windowSelector.setEnabled( false );
		windowSelector.setMnemonic( KeyEvent.VK_W );

		final Preferences prefs = Preferences.userNodeForPackage( getClass() );
		String wloc = prefs.get( "windowLocation", "" );

		initPreferenceValues( prefs );

		AbstractRDFPlaySheet.setDefaultIcons( DefaultPlaySheetIcons.defaultIcons );

		setSize( new Dimension( 1024, 768 ) );
		if ( "".equals( wloc ) ) {
			setExtendedState( Frame.MAXIMIZED_BOTH );
		}
		else {
			String[] pos = wloc.split( "," );
			setBounds( Integer.parseInt( pos[0] ), Integer.parseInt( pos[1] ),
					Integer.parseInt( pos[2] ), Integer.parseInt( pos[3] ) );
		}

		addComponentListener( new ComponentAdapter() {

			@Override
			public void componentResized( ComponentEvent e ) {
				Rectangle rect = e.getComponent().getBounds();
				String loc;
				if ( Frame.MAXIMIZED_BOTH == PlayPane.this.getExtendedState() ) {
					loc = "";
				}
				else {
					loc = String.format( "%d,%d,%d,%d", rect.x, rect.y, rect.width, rect.height );
				}
				prefs.put( "windowLocation", loc );

			}

			@Override
			public void componentMoved( ComponentEvent e ) {
				componentResized( e );
			}

			/**
			 * Handler to reset the vertical divider's position, as a percentage of
			 * the screen-height. This can only be done after the components have been
			 * shown.
			 *
			 * @param e
			 */
			@Override
			public void componentShown( ComponentEvent e ) {
				combinedSplitPane.setDividerLocation( 0.75 );
			}
		} );
		initMenuItems();
		initRepoList();
		DIHelper.getInstance().setRepoList( repoList );

		playsheetToolbar = new JToolBar();

		rightTabs = makeRightPane();
		leftTabs = makeLeftPane();
		mainSplitPane
				= new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, leftTabs, rightTabs );
		mainSplitPane.setOneTouchExpandable( true );
		mainSplitPane.setDividerLocation( 300 );

		mainSplitPane.setContinuousLayout( true );

		//Add the Custom Sparql Query window by referencing
		//an external class:
		customSparqlPanel = csp.addCustomSparqlPanel();
		combinedSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, mainSplitPane, customSparqlPanel );
		combinedSplitPane.setOneTouchExpandable( true );
		combinedSplitPane.setDividerLocation( 900 );

		//Provide minimum sizes for the two components in the combined split pane
		mainSplitPane.setMinimumSize( new Dimension( 1000, 50 ) );
		customSparqlPanel.setMinimumSize( new Dimension( 1000, 50 ) );
		toolbar = buildToolBar( playsheetToolbar );
		buildMenuBar();

		statusbar = new StatusBar( UIPROGRESS );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( toolbar, BorderLayout.PAGE_START );
		getContentPane().add( combinedSplitPane, BorderLayout.CENTER );
		getContentPane().add( statusbar, BorderLayout.SOUTH );

		syncUIWithMenuBar();

		// UIManager.put("nimbusBase", Color.BLUE);
		// UIManager.put("nimbusBlueGrey", new Color(102,0,0));
		// UIManager.put("control", Color.WHITE);
		SwingStyle.init(); // for swing rules and functions
		CustomAruiStyle.init(); // for custom components rules and functions
		// Components to style
		// Style.registerTargetClassName(lblLearningMaterials, ".label");
		Style.registerTargetClassName( submitButton, ".createBtn" );
		Style.registerTargetClassName( btnRepaintGraph, ".standardButton" );
		Style.registerTargetClassName( vcampModuleBtn, ".standardButton" );

		Style.registerTargetClassName( btnResetDefaults, ".standardButton" );
		Style.registerTargetClassName( refreshButton, ".standardButton" );
		Style.registerTargetClassName( saveSudowl, ".standardButton" );
		//Style.registerTargetClassName( pptTrainingBtn, ".standardButton" );
		//Style.registerTargetClassName( htmlTrainingBtn, ".standardButton" );

		CSSApplication app = new CSSApplication( getContentPane() );
		DIHelper.getInstance().setLocalProperty( Constants.MAIN_FRAME, this );
	}

	public RepositoryList getRepoList() {
		return repoList;
	}

	protected void initMenuItems() {
		// already initialized at instance creation
	}

	protected void initRepoList() {
		repoList.addContainerListener( new ContainerListener() {

			@Override
			public void componentAdded( ContainerEvent e ) {
			}

			@Override
			public void componentRemoved( ContainerEvent e ) {
				repoList.clearSelection();
			}
		} );

		repoList.addListSelectionListener( new ListSelectionListener() {
			@Override
			public void valueChanged( ListSelectionEvent e ) {
				logger.debug( "populating dropdowns for V-CAMP tab" );
				IEngine engine = repoList.getSelectedValue();

				DbAction actions[] = {
					toggler, proper, cloner, cloneconfer, clearer, exportttl, exportnt,
					exportrdf, exportinsights, expall, exprels, expnodes, expSpecNodes,
					expSpecRels, unmounter, sparqler, importls, consistencyCheck };
				for ( DbAction dba : actions ) {
					dba.setEngine( engine );
					dba.setEnabled( null != engine );
				}
				if ( null != engine ) {
					sparqler.setEnabled( engine.isServerSupported() );
				}

				// importxls.setEngine( engine ); // set the engine, but always enabled
			}
		} );
	}

	protected void syncUIWithMenuBar() {
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		toolbar.setVisible( prefs.getBoolean( "showToolBar", true ) );
		statusbar.setVisible( prefs.getBoolean( "showStatus", true ) );
		if ( !getProp( prefs, QUERYPANEL ) ) {
			customSparqlPanel.setVisible( false );
		}

		if ( !getProp( prefs, Constants.GPSSudowl ) ) {
			leftTabs.remove( owlPanel );
		}

		boolean cospref = prefs.getBoolean( GCOSMETICS, false );
		if ( !cospref ) {
			leftTabs.remove( cosmeticsPanel );
		}

		boolean fpref = prefs.getBoolean( GFILTER, false );
		if ( !fpref ) {
			leftTabs.remove( filterPanel );
		}

		boolean lpref = prefs.getBoolean( LOGGING, false );
		if ( !lpref ) {
			rightTabs.remove( loggingPanel );
		}
	}

	protected JTabbedPane makeLeftPane() {
		JTabbedPane leftView = new JTabbedPane( JTabbedPane.TOP );
		JComponent main = makeMainTab();
		leftView.addTab( "Database Explorer", null, main,
				"Ask the SEMOSS database a question" );

		owlPanel = makeOwlTab();
		leftView.addTab( "SUDOWL", null, owlPanel, null );

		JPanel outputPanel = makeOutputPanel();
		leftView.addTab( "Graph Labels", null, outputPanel,
				"Customize the labels associated with the objects displayed on the graph" );

		filterPanel = makeFilterPanel();
		leftView.addTab( "Graph Filter", null, filterPanel, "Customize graph display" );

		cosmeticsPanel = makeGraphCosmeticsPanel();
		leftView.addTab( "Graph Cosmetics", null, cosmeticsPanel,
				"Modify visual appearance of a node" );

		return leftView;
	}

	protected JTabbedPane makeRightPane() {
		final JTabbedPane rightView = new JTabbedPane( JTabbedPane.TOP );

		JComponent graphPanel = makeGraphTab();
		rightView.addTab( "Display Pane", null, graphPanel,
				"Display response to questions (queries)" );

		loggingPanel = new LoggingPanel();
		rightView.addTab( "Logging", null, loggingPanel,
				"This tab keeps a log of SEMOSS warnings and error messges for use by the SEMOSS development team" );
		rightView.addChangeListener( new ChangeListener() {

			@Override
			public void stateChanged( ChangeEvent e ) {
				if ( rightView.getSelectedComponent().equals( loggingPanel ) ) {
					loggingPanel.refresh();
				}
			}
		} );

		return rightView;
	}

	private JPanel makeGraphCosmeticsPanel() {
		JPanel graphCosmeticsPanel = new JPanel( new GridLayout( 1, 0 ) );

		colorShapeTable = new JTable();
		colorShapeTable.setShowGrid( true );

		JScrollPane colorShapeScrollPane = new JScrollPane( colorShapeTable );
		colorShapeScrollPane.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		graphCosmeticsPanel.add( colorShapeScrollPane );
//		graphCosmeticsPanel.add( btnResetDefaults );

		return graphCosmeticsPanel;
	}

	/**
	 * Splits the database selector from the categories/questions in the left
	 * pane. The divider is moveable by mouse, and drags the database window's
	 * lower edge, thereby resizing it.
	 *
	 * @return makeMainTab -- (JPanel) Combines the database selector and
	 * categories/ questions in a vertical JSplitPane.
	 */
	private JComponent makeMainTab() {
		Font labelFont = new Font( "SansSerif", Font.PLAIN, 10 );
		Font selectorFont = new Font( "Tahoma", Font.PLAIN, 11 );

		SelectDatabasePanel pnl = new SelectDatabasePanel( true );
		pnl.setLabelsFont( labelFont );
		perspectiveSelector = pnl.getPerspectiveSelector();
		questionSelector = pnl.getInsightSelector();
		perspectiveSelector.setFont( selectorFont );
		questionSelector.setFont( selectorFont );
		repoList.setFont( selectorFont );
		appendChkBox = pnl.getOverlay();
		submitButton = pnl.getSubmitButton();
		paramPanel = pnl.getParamPanel();

		Action handleQuestionKeys = new AbstractAction() {
			@Override
			public void actionPerformed( final ActionEvent e ) {
				Runnable runner = new Runnable() {
					ProcessQueryListener processQueryListener = new ProcessQueryListener();

					@Override
					public void run() {
						processQueryListener.actionPerformed( e );
					}
				};

				ProgressTask pt = new ProgressTask( "Executing Query", runner );
				OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
			}
		};
		submitButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK ), "handleQuestionKeys" );
		submitButton.getInputMap( JComponent.WHEN_FOCUSED ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK ), "handleQuestionKeys" );
		submitButton.getActionMap().put( "handleQuestionKeys", handleQuestionKeys );
		Style.registerTargetClassName( submitButton, ".createBtn" );

		return pnl;
	}

	private JPanel makeOwlTab() {
		JPanel owly = new JPanel();
		owly.setBackground( SystemColor.control );
		GridBagLayout gbl_owlPanel = new GridBagLayout();
		gbl_owlPanel.columnWidths = new int[]{ 228, 0 };
		gbl_owlPanel.rowHeights = new int[]{ 29, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_owlPanel.columnWeights = new double[]{ 1.0, Double.MIN_VALUE };
		gbl_owlPanel.rowWeights = new double[]{ 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		owly.setLayout( gbl_owlPanel );

		JLabel lblDataProperties = new JLabel( "Data Properties" );
		lblDataProperties.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		lblDataProperties.setHorizontalAlignment( SwingConstants.CENTER );
		GridBagConstraints gbc_lblDataProperties = new GridBagConstraints();
		gbc_lblDataProperties.anchor = GridBagConstraints.WEST;
		gbc_lblDataProperties.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblDataProperties.gridx = 0;
		gbc_lblDataProperties.gridy = 0;
		owly.add( lblDataProperties, gbc_lblDataProperties );

		JScrollPane scrollPane_8 = new JScrollPane();
		scrollPane_8.setPreferredSize( new Dimension( 150, 350 ) );
		scrollPane_8.setMinimumSize( new Dimension( 150, 350 ) );
		scrollPane_8.setMaximumSize( new Dimension( 150, 350 ) );
		GridBagConstraints gbc_scrollPane_8 = new GridBagConstraints();
		gbc_scrollPane_8.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrollPane_8.insets = new Insets( 0, 0, 5, 0 );
		gbc_scrollPane_8.gridx = 0;
		gbc_scrollPane_8.gridy = 1;
		owly.add( scrollPane_8, gbc_scrollPane_8 );

		dataPropertiesTable = new JTable();
		dataPropertiesTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		scrollPane_8.setViewportView( dataPropertiesTable );
		dataPropertiesTable.setFillsViewportHeight( true );
		dataPropertiesTable.setShowGrid( true );
		dataPropertiesTable.setShowHorizontalLines( true );
		dataPropertiesTable.setShowVerticalLines( true );

		dataPropertiesString = new JTextField();
		dataPropertiesString.setText( DIHelper.getInstance().getProperty( Constants.PROP_URI ) );
		GridBagConstraints gbc_dataPropertiesString = new GridBagConstraints();
		gbc_dataPropertiesString.insets = new Insets( 0, 0, 5, 0 );
		gbc_dataPropertiesString.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataPropertiesString.gridx = 0;
		gbc_dataPropertiesString.gridy = 2;
		owly.add( dataPropertiesString, gbc_dataPropertiesString );
		dataPropertiesString.setColumns( 10 );
		// add the routine to do the predicate and properties

		JLabel lblObjectProperties = new JLabel( "Object Properties" );
		lblObjectProperties.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		GridBagConstraints gbc_lblObjectProperties = new GridBagConstraints();
		gbc_lblObjectProperties.anchor = GridBagConstraints.WEST;
		gbc_lblObjectProperties.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblObjectProperties.gridx = 0;
		gbc_lblObjectProperties.gridy = 3;
		owly.add( lblObjectProperties, gbc_lblObjectProperties );

		JScrollPane scrollPane_7 = new JScrollPane();
		scrollPane_7.setPreferredSize( new Dimension( 150, 350 ) );
		scrollPane_7.setMinimumSize( new Dimension( 150, 350 ) );
		scrollPane_7.setMaximumSize( new Dimension( 150, 350 ) );
		GridBagConstraints gbc_scrollPane_7 = new GridBagConstraints();
		gbc_scrollPane_7.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_7.insets = new Insets( 0, 0, 5, 0 );
		gbc_scrollPane_7.gridx = 0;
		gbc_scrollPane_7.gridy = 4;
		owly.add( scrollPane_7, gbc_scrollPane_7 );

		objectPropertiesTable = new JTable();
		scrollPane_7.setViewportView( objectPropertiesTable );
		objectPropertiesTable.setShowGrid( true );
		objectPropertiesTable.setShowHorizontalLines( true );
		objectPropertiesTable.setShowVerticalLines( true );

		objectPropertiesString = new JTextField();
		objectPropertiesString.setText( DIHelper.getInstance().getProperty( Constants.PREDICATE_URI ) );

		GridBagConstraints gbc_objectPropertiesString = new GridBagConstraints();
		gbc_objectPropertiesString.anchor = GridBagConstraints.BELOW_BASELINE;
		gbc_objectPropertiesString.insets = new Insets( 0, 0, 5, 0 );
		gbc_objectPropertiesString.fill = GridBagConstraints.HORIZONTAL;
		gbc_objectPropertiesString.gridx = 0;
		gbc_objectPropertiesString.gridy = 5;
		owly.add( objectPropertiesString, gbc_objectPropertiesString );
		objectPropertiesString.setColumns( 10 );

		btnRepaintGraph = new CustomButton( "Refresh" );
		btnRepaintGraph.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		GridBagConstraints gbc_btnRepaintGraph = new GridBagConstraints();
		gbc_btnRepaintGraph.insets = new Insets( 0, 0, 5, 0 );
		gbc_btnRepaintGraph.gridx = 0;
		gbc_btnRepaintGraph.gridy = 6;
		owly.add( btnRepaintGraph, gbc_btnRepaintGraph );

		scrollPane_7.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		scrollPane_8.getVerticalScrollBar().setUI( new NewScrollBarUI() );

		saveSudowl = new CustomButton( "Refresh" );
		saveSudowl.setText( "Save" );
		saveSudowl.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		GridBagConstraints gbc_saveSudowl = new GridBagConstraints();
		gbc_saveSudowl.gridx = 0;
		gbc_saveSudowl.gridy = 7;
		owly.add( saveSudowl, gbc_saveSudowl );

		return owly;
	}

	private JPanel makeOutputPanel() {
		JPanel outputPanel = new JPanel();
		outputPanel.setBackground( SystemColor.control );
		GridBagLayout gbl_outputPanel = new GridBagLayout();
		gbl_outputPanel.columnWidths = new int[]{ 231 };
		gbl_outputPanel.rowHeights = new int[]{ 0, 0, 350, 0, 350, 150 };
		gbl_outputPanel.columnWeights = new double[]{ 1.0 };
		gbl_outputPanel.rowWeights = new double[]{ 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		outputPanel.setLayout( gbl_outputPanel );

		JLabel lblLabelDisplay = new JLabel( "Label Display" );
		lblLabelDisplay.setToolTipText( "Customize the appearance of the node labels as displayed on the graph" );
		lblLabelDisplay.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		lblLabelDisplay.setForeground( Color.BLACK );
		lblLabelDisplay.setBackground( Color.BLACK );
		GridBagConstraints gbc_lblLabelDisplay = new GridBagConstraints();
		gbc_lblLabelDisplay.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblLabelDisplay.gridx = 0;
		gbc_lblLabelDisplay.gridy = 1;
		outputPanel.add( lblLabelDisplay, gbc_lblLabelDisplay );

		JScrollPane scrollPane_3 = new JScrollPane( (Component) null );
		scrollPane_3.setPreferredSize( new Dimension( 150, 350 ) );
		scrollPane_3.setMinimumSize( new Dimension( 150, 350 ) );
		scrollPane_3.setMaximumSize( new Dimension( 150, 350 ) );
		scrollPane_3.setForeground( Color.GRAY );
		scrollPane_3.setBorder( null );
		scrollPane_3.setBackground( Color.WHITE );
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.insets = new Insets( 0, 0, 5, 0 );
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 2;
		outputPanel.add( scrollPane_3, gbc_scrollPane_3 );

		labelTable = new JTable();
		labelTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		scrollPane_3.setViewportView( labelTable );
		labelTable.setShowGrid( true );
		labelTable.setShowHorizontalLines( true );
		labelTable.setShowVerticalLines( true );

		JLabel lblDisplayTooltip = new JLabel( "Tooltip Display" );
		lblDisplayTooltip.setToolTipText( "Customize the appearance of the node labels displayed as mouseover events" );
		lblDisplayTooltip.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		lblDisplayTooltip.setBackground( Color.BLACK );
		lblDisplayTooltip.setForeground( Color.BLACK );
		GridBagConstraints gbc_lblDisplayTooltip = new GridBagConstraints();
		gbc_lblDisplayTooltip.insets = new Insets( 0, 0, 5, 0 );
		gbc_lblDisplayTooltip.gridx = 0;
		gbc_lblDisplayTooltip.gridy = 3;
		outputPanel.add( lblDisplayTooltip, gbc_lblDisplayTooltip );

		JScrollPane scrollPane_6 = new JScrollPane();
		scrollPane_6.setMaximumSize( new Dimension( 150, 350 ) );
		scrollPane_6.setPreferredSize( new Dimension( 150, 350 ) );
		scrollPane_6.setMinimumSize( new Dimension( 150, 350 ) );
		GridBagConstraints gbc_scrollPane_6 = new GridBagConstraints();
		gbc_scrollPane_6.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_6.gridx = 0;
		gbc_scrollPane_6.gridy = 4;
		outputPanel.add( scrollPane_6, gbc_scrollPane_6 );

		tooltipTable = new JTable();
		scrollPane_6.setViewportView( tooltipTable );
		tooltipTable.setShowGrid( true );
		tooltipTable.setShowHorizontalLines( true );
		tooltipTable.setShowVerticalLines( true );

		scrollPane_3.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		scrollPane_6.getVerticalScrollBar().setUI( new NewScrollBarUI() );

		return outputPanel;
	}

	private JPanel makeFilterPanel() {
		JPanel panel = new JPanel();

		panel.setBackground( SystemColor.control );
		GridBagLayout gbl_filterPanel = new GridBagLayout();
		gbl_filterPanel.columnWidths = new int[]{ 239, 0 };
		gbl_filterPanel.rowHeights = new int[]{ 44, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_filterPanel.columnWeights = new double[]{ 1.0, Double.MIN_VALUE };
		gbl_filterPanel.rowWeights = new double[]{ 1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout( gbl_filterPanel );

		filterTable = new JTable();
		filterTable.setShowGrid( true );
		filterTable.setShowHorizontalLines( true );
		filterTable.setShowVerticalLines( true );

		JScrollPane scrollPane_2 = new JScrollPane( filterTable );
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.gridheight = 2;
		gbc_scrollPane_2.insets = new Insets( 0, 0, 5, 0 );
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 0;
		panel.add( scrollPane_2, gbc_scrollPane_2 );

		filterSliders = new JScrollPane();
		GridBagConstraints gbc_filterSliders = new GridBagConstraints();
		gbc_filterSliders.insets = new Insets( 0, 0, 5, 0 );
		gbc_filterSliders.fill = GridBagConstraints.BOTH;
		gbc_filterSliders.gridx = 0;
		gbc_filterSliders.gridy = 2;
		panel.add( filterSliders, gbc_filterSliders );

		propertyTable = new JTable();
		filterSliders.setViewportView( propertyTable );
		propertyTable.setShowGrid( true );
		propertyTable.setShowHorizontalLines( true );
		propertyTable.setShowVerticalLines( true );

		JScrollPane scrollPane_4 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
		gbc_scrollPane_4.insets = new Insets( 0, 0, 5, 0 );
		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_4.gridx = 0;
		gbc_scrollPane_4.gridy = 3;
		panel.add( scrollPane_4, gbc_scrollPane_4 );

		edgeTable = new JTable();
		scrollPane_4.setViewportView( edgeTable );
		edgeTable.setShowGrid( true );
		edgeTable.setShowHorizontalLines( true );
		edgeTable.setShowVerticalLines( true );

		// scrollPane_2.setColumnHeaderView(filterTable);
		refreshButton = new CustomButton( "Refresh Graph" );
		refreshButton.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		GridBagConstraints gbc_refreshGraph = new GridBagConstraints();
		gbc_refreshGraph.insets = new Insets( 0, 0, 5, 0 );
		gbc_refreshGraph.gridx = 0;
		gbc_refreshGraph.gridy = 6;
		panel.add( refreshButton, gbc_refreshGraph );

		scrollPane_2.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		scrollPane_4.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		filterSliders.getVerticalScrollBar().setUI( new NewScrollBarUI() );

		return panel;
	}

	private JComponent makeGraphTab() {
		desktopPane = new CustomDesktopPane( playsheetToolbar );

		DIHelper.getInstance().setDesktop( desktopPane );
		desktopPane.addContainerListener( new ContainerListener() {

			@Override
			public void componentAdded( ContainerEvent e ) {
				refresh();
			}

			@Override
			public void componentRemoved( ContainerEvent e ) {
				refresh();
			}

			private void refresh() {
				windowSelector.removeAll();
				final JInternalFrame[] frames = desktopPane.getAllFrames();
				//The following declarations are to enable/disable the "Overlay" CheckBox
				//When necessary:
				JDesktopPane pane = DIHelper.getInstance().getDesktop();
				PlaySheetFrame psf = PlaySheetFrame.class.cast( pane.getSelectedFrame() );

				if ( 0 == frames.length ) {
					appendChkBox.setSelected( false );
				}

				windowSelector.setEnabled( 0 < frames.length );
				csp.appendSparqlQueryChkBox.setEnabled( frames.length > 0 );
				for ( final JInternalFrame f : frames ) {
					JMenuItem i = new JMenuItem( f.getTitle() );
					i.setIcon( f.getFrameIcon() );
					i.addActionListener( new ActionListener() {

						@Override
						public void actionPerformed( ActionEvent e ) {
							try {
								rightTabs.setSelectedComponent( desktopPane );
								desktopPane.getDesktopManager().deiconifyFrame( f );
								f.setSelected( true );
							}
							catch ( Exception ex ) {
								// don't really care
								logger.warn( ex, ex );
							}
						}
					} );
					windowSelector.add( i );
				}

				JMenuItem closeall = new JMenuItem( "Close All" );
				closeall.addActionListener( new ActionListener() {

					@Override
					public void actionPerformed( ActionEvent ae ) {
						for ( final JInternalFrame f : frames ) {
							f.dispose();
						}
						csp.appendSparqlQueryChkBox.setEnabled( false );
					}
				} );

				if ( windowSelector.isEnabled() ) {
					windowSelector.addSeparator();
					windowSelector.add( closeall );
				}
			}
		} );

		return desktopPane;
	}

	private JComponent makeHelpPane() {
		// Here we read the release notes text file
		StringBuilder helpdata = new StringBuilder( "<html><h3>About</h3>" );
		InputStream aboutFile = PlayPane.class.getResourceAsStream( "/help/about.txt" );

		if ( aboutFile != null ) {
			try {
				helpdata.append( "<pre>" );
				helpdata.append( IOUtils.toString( aboutFile ) );
				helpdata.append( "</pre>" );
			}
			catch ( IOException ioe ) {
				logger.warn( "missing about info", ioe );
			}
		}

		helpdata.append( "<h3>Release Notes</h3>" );
		InputStream releaseFile = PlayPane.class.getResourceAsStream( "/help/release.txt" );
		if ( releaseFile != null ) {
			try {
				helpdata.append( "<pre>" );
				helpdata.append( IOUtils.toString( releaseFile ) );
				helpdata.append( "</pre>" );
			}
			catch ( IOException ioe ) {
				logger.warn( "missing release notes", ioe );
			}
		}

		JPanel helpPanel = new JPanel( new BorderLayout() );
		helpPanel.setBackground( SystemColor.control );

		JTextPane about = new JTextPane();
		about.setContentType( "text/html" );
		about.setFont( new Font( "Tahoma", Font.PLAIN, 12 ) );
		about.setEditable( false );
		about.setText( helpdata.toString() );

		about.setBackground( SystemColor.control );
		about.setCaretPosition( 0 );
		JScrollPane scroller = new JScrollPane( about );
		scroller.getVerticalScrollBar().setUI( new NewScrollBarUI() );
		scroller.getHorizontalScrollBar().setUI( new NewHoriScrollBarUI() );
		about.setBorder( BorderFactory.createEmptyBorder( 0, 15, 10, 15 ) );
		return scroller;
	}

	public StatusBar getStatusBar() {
		return statusbar;
	}

	public JToolBar getToolBar() {
		return toolbar;
	}

	protected JMenu buildToolMenuBar() {
		return null;
	}

	protected JMenu buildDatabaseMenu() {
		final JMenu db = new JMenu( "Database" );
		db.add( toggler );
		db.add( proper );
		db.addSeparator();
		db.add( cloner );
		db.add( cloneconfer );
		db.add( clearer );
		db.add( consistencyCheck );

		JMenu exptop = new JMenu( "Export" );
		exptop.add( exportttl );
		exptop.add( exportnt );
		exptop.add( exportrdf );
		exptop.setIcon( DbAction.getIcon( "exportdb" ) );

		JMenu loadingsheets = new JMenu( "Loading Sheets" );
		exptop.add( loadingsheets );
		loadingsheets.add( expnodes );
		loadingsheets.add( expSpecNodes );
		loadingsheets.add( exprels );
		loadingsheets.add( expSpecRels );
		loadingsheets.add( expall );
		exptop.add( exportinsights );
		db.add( exptop );

		JMenu importtop = new JMenu( "Import" );
		importtop.setIcon( DbAction.getIcon( "importdb" ) );
		db.add( importtop );
		importtop.add( importls );
		JMenu insights = new JMenu( "Insights" );

		insights.add( resetInsights );
		insights.add( importInsights );
		importtop.add( insights );

		db.setMnemonic( KeyEvent.VK_D );

		final JMenu mergeroot = new JMenu( DbAction.MERGE );
		mergeroot.setEnabled( false );
		db.add( mergeroot );

		db.add( unmounter );
		db.add( sparqler );
		sparqler.setEnabled( false );
		db.addSeparator();
		db.add( mounter );
		db.add( creater );

		ListSelectionListener lsl = new ListSelectionListener() {

			@Override
			public void valueChanged( ListSelectionEvent e ) {
				if ( e.getValueIsAdjusting() ) {
					return;
				}

				IEngine engine = repoList.getSelectedValue();
				mergeroot.removeAll();
				mergeroot.setEnabled( repoList.getRepositoryModel().size() > 1 );
				for ( IEngine eng : repoList.getRepositoryModel().getElements() ) {
					if ( !eng.equals( engine ) ) {
						mergeroot.add( new MergeAction( PlayPane.UIPROGRESS, engine, eng,
								PlayPane.this ) );
					}
				}
			}
		};
		repoList.addListSelectionListener( lsl );

		return db;
	}

	protected JMenu buildHelpMenu() {
		JMenu help = new JMenu( "Help" );
		JMenuItem helpitem = new JMenuItem( "About SEMOSS Tool" );
		help.add( helpitem );
		help.add( new AbstractAction( "Options" ) {

			@Override
			public void actionPerformed( ActionEvent ae ) {
				SettingsPanel.showDialog( PlayPane.this );
			}
		} );

		helpitem.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				final JDialog dlg = new JDialog( (Frame) null, false );
				dlg.setTitle( "About V-CAMP SEMOSS Tool" );
				dlg.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
				JButton closer = new JButton( "Close" );
				dlg.getContentPane().setLayout( new BorderLayout() );
				JComponent helpdata = makeHelpPane();
				dlg.getContentPane().add( helpdata );
				dlg.getContentPane().add( closer, BorderLayout.SOUTH );
				dlg.setSize( new Dimension( 800, 500 ) );
				closer.addActionListener( new ActionListener() {

					@Override
					public void actionPerformed( ActionEvent e ) {
						dlg.setVisible( false );
					}
				} );

				dlg.setVisible( true );
			}
		} );

		return help;
	}

	protected JMenu buildViewMenu() {
		final Preferences prefs = Preferences.userNodeForPackage( getClass() );

		final Map<String, JPanel> preflistenermap = new HashMap<>();
		preflistenermap.put( Constants.GPSSudowl, owlPanel );
		preflistenermap.put( GCOSMETICS, cosmeticsPanel );
		preflistenermap.put( GFILTER, filterPanel );
		preflistenermap.put( LOGGING, loggingPanel );

		ActionListener preflistener = new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				JCheckBoxMenuItem item = JCheckBoxMenuItem.class.cast( e.getSource() );
				boolean ischecked = item.isSelected();
				String cmd = e.getActionCommand();

				prefs.putBoolean( cmd, ischecked );

				DIHelper.getInstance()
						.getCoreProp().setProperty( cmd, Boolean.toString( ischecked ) );

				if ( preflistenermap.containsKey( cmd ) ) {
					JPanel panel = preflistenermap.get( cmd );
					if ( ischecked ) {
						if ( owlPanel == panel ) {
							leftTabs.addTab( "SUDOWL", null, owlPanel, null );
						}
						else if ( cosmeticsPanel == panel ) {
							leftTabs.addTab( "Graph Cosmetics", null, cosmeticsPanel,
									"Modify visual appearance of a node" );
						}
						else if ( filterPanel == panel ) {
							leftTabs.addTab( "Graph Filter", null, filterPanel,
									"Customize graph display" );
						}
						else if ( loggingPanel == panel ) {
							rightTabs.addTab( "Logging", null, loggingPanel,
									"This tab keeps a log of SEMOSS warnings and error messges for use by the SEMOSS development team" );
						}
					}
					else {
						if ( loggingPanel == panel ) {
							rightTabs.remove( panel );

						}
						else {
							leftTabs.remove( panel );
						}
					}
				}
			}
		};

		final JCheckBoxMenuItem sudowl = new JCheckBoxMenuItem( "SUDOWL tab",
				getProp( prefs, Constants.GPSSudowl ) );
		sudowl.setActionCommand( Constants.GPSSudowl );
		sudowl.addActionListener( preflistener );
		sudowl.setToolTipText( "Enables/Disables the SUDOWL tab" );

		final JCheckBoxMenuItem statbar = new JCheckBoxMenuItem( "Status Bar",
				prefs.getBoolean( "showStatus", true ) );

		statbar.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				statusbar.setVisible( statbar.isSelected() );
				prefs.putBoolean( "showStatus", statusbar.isVisible() );
			}
		} );

		final JCheckBoxMenuItem gcos = new JCheckBoxMenuItem( "Graph Cosmetics tab",
				getProp( prefs, GCOSMETICS ) );
		gcos.setActionCommand( GCOSMETICS );
		gcos.addActionListener( preflistener );
		gcos.setToolTipText( "Enables/Disables graph cosmetics tab" );

		final JCheckBoxMenuItem gfilt = new JCheckBoxMenuItem( "Graph Filter tab",
				getProp( prefs, GFILTER ) );
		gfilt.setActionCommand( GFILTER );
		gfilt.addActionListener( preflistener );
		gfilt.setToolTipText( "Enables/Disables graph filter tab" );

		final JCheckBoxMenuItem logging = new JCheckBoxMenuItem( "Logging tab",
				getProp( prefs, LOGGING ) );
		logging.setActionCommand( LOGGING );
		logging.addActionListener( preflistener );
		logging.setToolTipText( "Enables/Disables logging tab" );

		final JCheckBoxMenuItem tb = new JCheckBoxMenuItem( "Tool Bar",
				prefs.getBoolean( "showToolBar", true ) );

		tb.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				toolbar.setVisible( tb.isSelected() );
				prefs.putBoolean( "showToolBar", toolbar.isVisible() );
			}
		} );

		JCheckBoxMenuItem splithider = new JCheckBoxMenuItem( "Left Menu", true );
		splithider.setToolTipText( "Shows/Hides left menu area" );
		splithider.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent ae ) {
				leftTabs.setVisible( !leftTabs.isVisible() );
				if ( leftTabs.isVisible() ) {
					mainSplitPane.setDividerLocation( 0.25 );
				}
			}
		} );

		JCheckBoxMenuItem hidecsp = new JCheckBoxMenuItem( "Query Area",
				getProp( prefs, QUERYPANEL ) );
		hidecsp.setToolTipText( "Shows/Hides query area" );
		hidecsp.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent ae ) {
				customSparqlPanel.setVisible( !customSparqlPanel.isVisible() );
				prefs.putBoolean( QUERYPANEL, customSparqlPanel.isVisible() );
				if ( customSparqlPanel.isVisible() ) {
					combinedSplitPane.setDividerLocation( 0.75 );
				}
			}
		} );

		JMenu view = new JMenu( "View" );
		view.setMnemonic( KeyEvent.VK_V );
		view.add( gcos );
		view.add( gfilt );
		view.add( splithider );
		view.add( logging );
		view.add( hidecsp );
		view.add( statbar );
		view.add( sudowl );
		view.add( tb );
		return view;
	}

	protected void buildMenuBar() {
		JMenuBar menu = new JMenuBar();

		JMenuItem exiter = new JMenuItem( new AbstractAction( "Exit" ) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( ActionEvent e ) {
				System.exit( 0 );
			}
		} );

		JMenu file = new JMenu( "File" );
		//file.add( importxls );

		file.setMnemonic( KeyEvent.VK_F );
		exiter.setMnemonic( KeyEvent.VK_X );

		file.add( mounter );
		file.add( creater );
		file.add( unmounter );

		JMenu exptop2 = new JMenu( "Export" );
		exptop2.add( exportttl );
		exptop2.add( exportnt );
		exptop2.add( exportrdf );
		exptop2.setIcon( DbAction.getIcon( "exportdb" ) );
		JMenu loadingsheets2 = new JMenu( "Loading Sheets" );
		exptop2.add( loadingsheets2 );
		loadingsheets2.add( expnodes );
		loadingsheets2.add( expSpecNodes );
		loadingsheets2.add( exprels );
		loadingsheets2.add( expSpecRels );
		loadingsheets2.add( expall );
		exptop2.add( exportinsights );

		JMenu importtop2 = new JMenu( "Import" );
		importtop2.setIcon( DbAction.getIcon( "importdb" ) );
		importtop2.add( importls );
		JMenu insights2 = new JMenu( "Insights" );
		insights2.add( resetInsights );
		insights2.add( importInsights );
		importtop2.add( insights2 );

		file.add( exptop2 );
		file.add( importtop2 );
		file.addSeparator();
		file.add( exiter );

		menu.add( file );

		JMenu db = buildDatabaseMenu();
		JMenu help = buildHelpMenu();
		JMenu view = buildViewMenu();
		JMenu tools = buildToolMenuBar();

		if ( null != db ) {
			menu.add( db );
		}
		if ( null != tools ) {
			menu.add( tools );
		}
		if ( null != view ) {
			menu.add( view );
		}

		menu.add( windowSelector );

		if ( null != help ) {
			menu.add( Box.createHorizontalGlue() );
			menu.add( help );
		}

		this.setJMenuBar( menu );
	}

	protected JToolBar buildToolBar( JToolBar playsheeter ) {
		JToolBar tools = new JToolBar();
		tools.setFloatable( false );

		tools.add( mounter );
		tools.add( creater );
		tools.add( importls );
		tools.add( exportttl );

		tools.addSeparator();
		playsheeter.setFloatable( false );
		tools.add( playsheeter );

		return tools;
	}

	public static boolean getProp( Preferences prefs, String propstr ) {
		String prop = prefs.get( propstr, null );
		if ( null == prop ) {
			prop = DIHelper.getInstance().getProperty( propstr );
		}

		if ( null == prop ) {
			prop = Boolean.toString( false );
		}

		return Boolean.parseBoolean( prop );
	}

	public static boolean getProp( SemossPreferences prefs, String propstr ) {
		String prop = prefs.get( propstr, null );
		if ( null == prop ) {
			prop = DIHelper.getInstance().getProperty( propstr );
		}

		if ( null == prop ) {
			prop = Boolean.toString( false );
		}

		return Boolean.parseBoolean( prop );
	}

	protected void initPreferenceValues( Preferences p ) {
		final String MISSING = "not-here";
		Map<String, Boolean> bools = new HashMap<>();
		bools.put( Constants.GPSSudowl, false );
		bools.put( Constants.GPSSearch, true );
		bools.put( Constants.GPSProp, true );

		for ( Map.Entry<String, Boolean> en : bools.entrySet() ) {
			if ( MISSING.equals( p.get( en.getKey(), MISSING ) ) ) {
				p.putBoolean( en.getKey(), en.getValue() );
				DIHelper.getInstance().getCoreProp()
						.setProperty( en.getKey(), en.getValue().toString() );
			}
		}
	}
}
