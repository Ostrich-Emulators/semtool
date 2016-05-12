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
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.om.GraphColorShapeRepository;
import com.ostrichemulators.semtool.om.Insight;
import com.ostrichemulators.semtool.om.InsightOutputType;
import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.util.VocabularyRegistry;
import com.ostrichemulators.semtool.ui.actions.CheckConsistencyAction;
import com.ostrichemulators.semtool.ui.actions.ClearAction;
import com.ostrichemulators.semtool.ui.actions.CloneAction;
import com.ostrichemulators.semtool.ui.actions.CreateDbAction;
import com.ostrichemulators.semtool.ui.actions.DbAction;
import com.ostrichemulators.semtool.ui.actions.ExportGraphAction;
import com.ostrichemulators.semtool.ui.actions.ExportInsightsAction;
import com.ostrichemulators.semtool.ui.actions.ExportLoadingSheetAction;
import com.ostrichemulators.semtool.ui.actions.ExportSpecificNodesToLoadingSheetAction;
import com.ostrichemulators.semtool.ui.actions.ExportSpecificRelationshipsToLoadingSheetAction;
import com.ostrichemulators.semtool.ui.actions.ExportTtlAction;
import com.ostrichemulators.semtool.ui.actions.ImportInsightsAction;
import com.ostrichemulators.semtool.ui.actions.ImportLoadingSheetAction;
import com.ostrichemulators.semtool.ui.actions.MergeAction;
import com.ostrichemulators.semtool.ui.actions.MountAction;
import com.ostrichemulators.semtool.ui.actions.NewLoadingSheetAction;
import com.ostrichemulators.semtool.ui.actions.OpenSparqlAction;
import com.ostrichemulators.semtool.ui.actions.PinAction;
import com.ostrichemulators.semtool.ui.actions.PropertiesAction;
import com.ostrichemulators.semtool.ui.actions.RemoteDbAction;
import com.ostrichemulators.semtool.ui.actions.UnmountAction;
import com.ostrichemulators.semtool.ui.components.graphicalquerybuilder.GraphicalQueryPanel;
import com.ostrichemulators.semtool.ui.components.insight.manager.InsightManagerPanel;
import com.ostrichemulators.semtool.ui.components.playsheets.AppDupeHeatMapSheet;
import com.ostrichemulators.semtool.ui.components.playsheets.ColumnChartPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.DendrogramPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GraphPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridRAWPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.GridScatterSheet;
import com.ostrichemulators.semtool.ui.components.playsheets.HeatMapPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.ParallelCoordinatesPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.PieChartPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.USHeatMapPlaySheet;
import com.ostrichemulators.semtool.ui.components.playsheets.WorldHeatMapPlaySheet;
import com.ostrichemulators.semtool.ui.components.semanticexplorer.SemanticExplorerPanel;
import com.ostrichemulators.semtool.ui.helpers.DefaultColorShapeRepository;
import com.ostrichemulators.semtool.ui.main.SemossPreferences;
import com.ostrichemulators.semtool.ui.swing.custom.CustomDesktopPane;
import com.ostrichemulators.semtool.util.DIHelper;
import com.ostrichemulators.semtool.util.GuiUtility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The playpane houses all of the components that create the user interface in
 * SEMOSS.
 */
public class PlayPane extends JFrame {

	private static final long serialVersionUID = -715188668604903980L;
	private static final Logger logger = Logger.getLogger( PlayPane.class );

	private final String GQUERYBUILDER = "qQueryBuilderPanel";
	private final String SEMANTICEXPLORER = "semanticExplorer";
	private final String IMANAGE_2 = "iManagePanel_2";
	private final String LOGGING = "loggingpanel";
	private final String QUERYPANEL = "customSparqlPanel";
	public static final String UIPROGRESS = "UI";

	// Left Control Panel Components
	private JComboBox<Perspective> perspectiveSelector;
	private JComboBox<Insight> questionSelector;

	private JCheckBox appendChkBox;
	protected final RepositoryList repoList = new RepositoryList();

	private GraphicalQueryPanel gQueryBuilderPanel;
	private InsightManagerPanel insightManager;
	private static SemanticExplorerPanel semanticExplorer;

	// Right graphPanel desktopPane
	private CustomDesktopPane desktopPane;

	private LoggingPanel loggingPanel;

	private JComponent dbexplorer;
	private JTabbedPane rightTabs;
	private StatusBar statusbar;

	private final DbAction creater = new CreateDbAction( UIPROGRESS, this );
	private final DbAction mounter = new MountAction( UIPROGRESS, this );
	private final DbAction toggler = new PinAction( UIPROGRESS );
	private final DbAction proper = new PropertiesAction( this );
	private final DbAction cloner = new CloneAction( UIPROGRESS, this );
	private final DbAction clearer = new ClearAction( UIPROGRESS, this );
	private final DbAction exportttl = new ExportTtlAction( UIPROGRESS,
			ExportTtlAction.Style.TTL, this );
	private final DbAction exportnt = new ExportTtlAction( UIPROGRESS,
			ExportTtlAction.Style.NT, this );
	private final DbAction exportrdf = new ExportTtlAction( UIPROGRESS,
			ExportTtlAction.Style.RDF, this );
	private final DbAction exportinsights
			= new ExportInsightsAction( UIPROGRESS, this );
	private final DbAction expgraphml
			= new ExportGraphAction( UIPROGRESS, this, ExportGraphAction.Style.GRAPHML );
	private final DbAction expgson
			= new ExportGraphAction( UIPROGRESS, this, ExportGraphAction.Style.GSON );
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
	private final DbAction unmounter = new UnmountAction( this, "Close DB" );
	private final ImportLoadingSheetAction importls
			= new ImportLoadingSheetAction( UIPROGRESS, this );
//	private final OpenAction importxls = new OpenAction( UIPROGRESS, this );
	private final RemoteDbAction remoteDb = new RemoteDbAction( UIPROGRESS, this );
	private final NewLoadingSheetAction newls
			= new NewLoadingSheetAction( UIPROGRESS, this );
	private final ImportInsightsAction resetInsights
			= new ImportInsightsAction( UIPROGRESS, true, this );
	private final ImportInsightsAction importInsights
			= new ImportInsightsAction( UIPROGRESS, false, this );

	private final CheckConsistencyAction consistencyCheck
			= new CheckConsistencyAction( UIPROGRESS, this );
	protected final JMenu windowSelector = new JMenu( "Window" );
	protected final JMenu fileMenu = new JMenu( "File" );
	protected final JMenuItem fileMenuSave = new JMenuItem( "Save" );
	protected final JMenuItem fileMenuSaveAs = new JMenuItem( "Save As" );
	protected final JMenuItem fileMenuSaveAll = new JMenuItem( "Save All" );
	private JCheckBoxMenuItem hidecsp;
	private JCheckBoxMenuItem splithider;
	private final JCheckBoxMenuItem loggingItem = new JCheckBoxMenuItem( "Logging",
			DbAction.getIcon( "log_tab1" ) );
	private final JCheckBoxMenuItem gQueryBuilderItem
			= new JCheckBoxMenuItem( "Graphical Query Builder",
					DbAction.getIcon( "graphic_query" ) );
	private final JCheckBoxMenuItem insightManagerItem = new JCheckBoxMenuItem( "Insight Manager",
			DbAction.getIcon( "insight_manager_tab1" ) );
	private final JCheckBoxMenuItem semanticExplorerItem = new JCheckBoxMenuItem( "Semantic Explorer",
			DbAction.getIcon( "semantic_dataset2" ) );

	private JToolBar toolbar;
	private JToolBar playsheetToolbar;
	private JSplitPane mainSplitPane;
	private JSplitPane combinedSplitPane;
	private final CustomSparqlPanel customSparqlPanel = new CustomSparqlPanel();

	// public JTable colorShapeTable;
	//public JTable sizeTable;
	private SelectDatabasePanel selectDatabasePanel;
	private final static Preferences prefs = Preferences.userNodeForPackage( PlayPane.class );
	private final DefaultColorShapeRepository colorsShapes = new DefaultColorShapeRepository();

	public PlayPane() {
		colorsShapes.setSaveToPreferences( true );
	}

	public GraphColorShapeRepository getColorShapeRepository() {
		return colorsShapes;
	}

	protected String getStartupMessage() {
		return "Semantic Toolkit started";
	}

	/**
	 * Launch the application.
	 *
	 * @throws java.lang.Exception
	 */
	public void start() throws Exception {
		initializeUi();
		desktopPane.registerFrameListener( customSparqlPanel.makeDesktopListener() );
		ApplicationContext ctx = new ClassPathXmlApplicationContext( "/appContext.xml" );
		DIHelper.getInstance().setAppCtx( ctx );
		DIHelper.getInstance().setPlayPane( this );

		statusbar.addStatus( getStartupMessage() );
	}

	public void setApplicationIcons() {
		List<Image> images = new ArrayList<>();
		images.add( GuiUtility.loadImage( "SemTool-16x16.png" ) );
		images.add( GuiUtility.loadImage( "SemTool-32x32.png" ) );
		images.add( GuiUtility.loadImage( "SemTool-64x64.png" ) );
		images.add( GuiUtility.loadImage( "SemTool-128x128.png" ) );
		images.add( GuiUtility.loadImage( "SemTool-256x256.png" ) );
		setIconImages( images );

	}

	protected void initializeUi() {
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		setApplicationIcons();
		OutputTypeRegistry registry = DIHelper.getInstance().getOutputTypeRegistry();
		registerPlaySheets( registry );
		customSparqlPanel.setOutputTypeRegistry( registry );

		setTitle( "OS-EM Semantic Toolkit" );
		windowSelector.setEnabled( false );
		windowSelector.setMnemonic( KeyEvent.VK_W );
		windowSelector.setToolTipText( "Manage Open Windows" );

		String wloc = prefs.get( "windowLocation", "" );

		initPreferenceValues( prefs );

		VocabularyRegistry.registerVocabulary( "semoss",
				IEngine.class.getResource( "/models/semoss.ttl" ), true );
		VocabularyRegistry.registerVocabulary( "va-semoss",
				IEngine.class.getResource( "/models/va-semoss.ttl" ), true );

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
		DIHelper.getInstance().setRepoList( repoList );

		playsheetToolbar = new JToolBar();
		rightTabs = makeRightPane();
		dbexplorer = makeExplorerPane();
		initRepoList();

		customSparqlPanel.setOverlayCheckBox( appendChkBox );
		customSparqlPanel.setInsightsComboBox( questionSelector );

		mainSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, dbexplorer, rightTabs );
		mainSplitPane.setOneTouchExpandable( true );
		mainSplitPane.setDividerLocation( 300 );
		mainSplitPane.setContinuousLayout( true );

		//Add the Custom Sparql Query window by referencing an external class:
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

		this.addWindowListener( new WindowAdapter() {

			@Override
			public void windowClosing( WindowEvent e ) {
				JInternalFrame frames[] = desktopPane.getAllFrames();
				for ( JInternalFrame jif : frames ) {
					if ( jif instanceof PlaySheetFrame ) {
						// signal the frame that it's closing
						PlaySheetFrame.class.cast( jif ).dispose();
					}
				}

				super.windowClosing( e );
			}

		} );

	}

	public RepositoryList getRepoList() {
		return repoList;
	}

	public SelectDatabasePanel getDatabasePanel() {
		return selectDatabasePanel;
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
				IEngine engine = repoList.getSelectedValue();
				logger.debug( "resetting actions for new engine: " + engine );

				DbAction actions[] = {
					toggler, proper, cloner, clearer, exportttl, exportnt, expgraphml,
					expgson, exportrdf, exportinsights, expall, exprels, expnodes,
					expSpecNodes, expSpecRels, unmounter, importls, consistencyCheck };
				for ( DbAction dba : actions ) {
					dba.setEngine( engine );
					dba.setEnabled( null != engine );
				}

				if ( null != gQueryBuilderPanel ) {
					gQueryBuilderPanel.setEngine( engine );
				}
				insightManager.setEngine( engine );
				semanticExplorer.setEngine( engine );
			}
		} );
	}

	protected void syncUIWithMenuBar() {
		toolbar.setVisible( prefs.getBoolean( "showToolBar", true ) );
		statusbar.setVisible( prefs.getBoolean( "showStatus", true ) );

		if ( !getProp( QUERYPANEL ) ) {
			customSparqlPanel.setVisible( false );
		}

		if ( !getProp( LOGGING ) ) {
			rightTabs.remove( loggingPanel );
		}
		if ( !getProp( GQUERYBUILDER ) ) {
			rightTabs.remove( gQueryBuilderPanel );
		}
		if ( !getProp( SEMANTICEXPLORER ) ) {
			rightTabs.remove( semanticExplorer );
		}
		if ( !getProp( IMANAGE_2 ) ) {
			rightTabs.remove( insightManager );
		}
	}

	protected JTabbedPane makeRightPane() {
		final JTabbedPane rightView = new JTabbedPane( JTabbedPane.TOP );

		JComponent graphPanel = makeGraphTab();
		rightView.addTab( "Display Pane", DbAction.getIcon( "display_tab1" ), graphPanel,
				"Display response to questions (queries)" );
		JLabel dislbl = new JLabel( "Display Pane" );
		Icon disicon = DbAction.getIcon( "display_tab1" );
		dislbl.setIcon( disicon );
		dislbl.setIconTextGap( 5 );
		dislbl.setHorizontalTextPosition( SwingConstants.RIGHT );
		rightView.setTabComponentAt( 0, dislbl );

		loggingPanel = new LoggingPanel();
		rightView.addTab( "Logging", DbAction.getIcon( "log_tab1" ), loggingPanel,
				"This tab keeps a log of SEMOSS warnings and error messges for use by the SEMOSS development team" );
		rightView.setTabComponentAt(
				rightView.indexOfComponent( loggingPanel ),
				new PlayPaneCloseableTab( rightView, loggingItem, DbAction.getIcon( "log_tab1" ) )
		);

//		gQueryBuilderPanel = new GraphicalQueryPanel( UIPROGRESS, colorsShapes );
//		gQueryBuilderPanel.setSparqlArea( customSparqlPanel.getOpenEditor() );
//		rightView.addTab( "Graphical Query Builder", null, gQueryBuilderPanel,
//				"Build queries graphically and generate Sparql" );
//		rightView.setTabComponentAt(
//				rightView.indexOfComponent( gQueryBuilderPanel ),
//				new PlayPaneCloseableTab( rightView, gQueryBuilderItem, DbAction.getIcon( "graphic_query" ) )
//		);
		insightManager = new InsightManagerPanel();
		rightView.addTab( "Insight Manager", null, insightManager,
				"Manage perspectives and insights" );
		rightView.setTabComponentAt(
				rightView.indexOfComponent( insightManager ),
				new PlayPaneCloseableTab( rightView, insightManagerItem, DbAction.getIcon( "insight_manager_tab1" ) )
		);

		semanticExplorer = new SemanticExplorerPanel();
		rightView.addTab( "Semantic Explorer", DbAction.getIcon( "semantic_dataset2" ),
				semanticExplorer, "Explore the classes and instances" );
		rightView.setTabComponentAt(
				rightView.indexOfComponent( semanticExplorer ),
				new PlayPaneCloseableTab( rightView, semanticExplorerItem, DbAction.getIcon( "semantic_dataset2" ) )
		);

		rightView.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged( ChangeEvent e ) {
				if ( rightView.getSelectedComponent().equals( loggingPanel ) ) {
					loggingPanel.refresh();
				}
				else if ( rightView.getSelectedComponent().equals( gQueryBuilderPanel ) ) {
					if ( !hidecsp.isSelected() ) {
						hidecsp.doClick();
					}
				}
			}
		} );

		return rightView;
	}

	/**
	 * Splits the database selector from the categories/questions in the left
	 * pane. The divider is moveable by mouse, and drags the database window's
	 * lower edge, thereby resizing it.
	 *
	 * @return makeMainTab -- (JPanel) Combines the database selector and
	 * categories/ questions in a vertical JSplitPane.
	 */
	private JComponent makeExplorerPane() {
		Font labelFont = new Font( "SansSerif", Font.PLAIN, 10 );
		Font selectorFont = new Font( "Tahoma", Font.PLAIN, 11 );

		selectDatabasePanel = new SelectDatabasePanel( true );
		selectDatabasePanel.setLabelsFont( labelFont );
		perspectiveSelector = selectDatabasePanel.getPerspectiveSelector();
		questionSelector = selectDatabasePanel.getInsightSelector();
		perspectiveSelector.setFont( selectorFont );
		questionSelector.setFont( selectorFont );
		repoList.setFont( selectorFont );
		appendChkBox = selectDatabasePanel.getOverlay();
		JButton submitButton = selectDatabasePanel.getSubmitButton();

		Action handleQuestionKeys = selectDatabasePanel.getInsightAction();
		submitButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK ), "handleQuestionKeys" );
		submitButton.getInputMap( JComponent.WHEN_FOCUSED ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK ), "handleQuestionKeys" );
		submitButton.getActionMap().put( "handleQuestionKeys", handleQuestionKeys );

		return selectDatabasePanel;
	}

	private JComponent makeGraphTab() {
		desktopPane = new CustomDesktopPane( playsheetToolbar );
		desktopPane.registerFrameListener( new InternalFrameListener() {

			@Override
			public void internalFrameOpened( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameClosing( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameClosed( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameIconified( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameDeiconified( InternalFrameEvent e ) {
			}

			@Override
			public void internalFrameActivated( InternalFrameEvent e ) {

				JInternalFrame jif = e.getInternalFrame();

				if ( jif instanceof PlaySheetFrame ) {
					// populate the file menu
					PlaySheetFrame psf = PlaySheetFrame.class.cast( jif );
					Map<String, Action> actions = psf.getActions();
					if ( actions.containsKey( PlaySheetFrame.SAVE ) ) {
						String s = fileMenuSave.getText();
						fileMenuSave.setAction( actions.get( PlaySheetFrame.SAVE ) );
						fileMenuSave.setEnabled( true );
						fileMenuSave.setText( s );
					}
					if ( actions.containsKey( PlaySheetFrame.SAVE_AS ) ) {
						String sa = fileMenuSaveAs.getText();
						fileMenuSaveAs.setAction( actions.get( PlaySheetFrame.SAVE_AS ) );
						fileMenuSaveAs.setEnabled( true );
						fileMenuSaveAs.setText( sa );
					}
					if ( actions.containsKey( PlaySheetFrame.SAVE_ALL ) ) {
						String sA = fileMenuSaveAll.getText();
						fileMenuSaveAll.setAction( actions.get( PlaySheetFrame.SAVE_ALL ) );
						fileMenuSaveAll.setEnabled( true );
						fileMenuSaveAll.setText( sA );
					}
				}
			}

			@Override
			public void internalFrameDeactivated( InternalFrameEvent e ) {
				fileMenuSave.setEnabled( false );
				fileMenuSaveAs.setEnabled( false );
				fileMenuSaveAll.setEnabled( false );
			}
		} );

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
				final JInternalFrame[] frames = desktopPane.getAllFrames();
				windowSelector.removeAll();
				if ( 0 == frames.length ) {
					appendChkBox.setSelected( false );
				}

				windowSelector.setEnabled( 0 < frames.length );
				customSparqlPanel.enableAppend( frames.length > 0 );

				JMenuItem closeone = new JMenuItem( new AbstractAction( "Close" ) {
					private static final long serialVersionUID = -9155947477934035700L;

					@Override
					public void actionPerformed( ActionEvent ae ) {
						JInternalFrame f = desktopPane.getSelectedFrame();
						if ( null != f ) {
							f.dispose();
						}

						JInternalFrame remainingframes[] = desktopPane.getAllFrames();
						if ( remainingframes.length > 0 ) {
							try {
								remainingframes[0].setSelected( true );
							}
							catch ( Exception e ) {
								// don't care
							}
						}
					}
				} );

				JMenuItem closeall = new JMenuItem( new AbstractAction( "Close All" ) {
					private static final long serialVersionUID = 108861767551874851L;

					@Override
					public void actionPerformed( ActionEvent ae ) {
						for ( final JInternalFrame f : frames ) {
							f.dispose();
						}
						customSparqlPanel.enableAppend( false );
					}
				} );

				JMenuItem tilehor = new JMenuItem( new AbstractAction( "Tile Horizontally" ) {
					private static final long serialVersionUID = 8345138168576254769L;

					@Override
					public void actionPerformed( ActionEvent ae ) {
						Dimension d = desktopPane.getSize();
						final int hPerWindow = d.height / frames.length;
						int y = 0;
						for ( JInternalFrame f : frames ) {
							f.setBounds( 0, y, d.width, hPerWindow );
							y += hPerWindow;
						}
					}
				} );
				//tileh to tilever
				JMenuItem tilever = new JMenuItem( new AbstractAction( "Tile Vertically" ) {
					private static final long serialVersionUID = -3082079771504175656L;

					@Override
					public void actionPerformed( ActionEvent ae ) {
						Dimension d = desktopPane.getSize();
						final int wPerWindow = d.width / frames.length;
						int x = 0;
						for ( JInternalFrame f : frames ) {
							f.setBounds( x, 0, wPerWindow, d.height );
							x += wPerWindow;
						}
					}
				} );

				JMenuItem tilec = new JMenuItem( new AbstractAction( "Cascade" ) {
					private static final long serialVersionUID = 5620428177844892386L;

					@Override
					public void actionPerformed( ActionEvent ae ) {
						Dimension d = desktopPane.getSize();
						int x = 0;
						for ( JInternalFrame f : frames ) {
							try {
								f.setSelected( true );
							}
							catch ( Exception e ) {
								// ignore
							}
							f.setBounds( x, x, d.width - x, d.height - x );
							x += CustomDesktopPane.CASCADE_STEPSIZE;
						}
					}
				} );

				if ( windowSelector.isEnabled() ) {
					windowSelector.add( closeone );
					closeone.setToolTipText( "Close the current Window" );
					closeone.setMnemonic( KeyEvent.VK_C );
					windowSelector.add( closeall );
					closeall.setToolTipText( "Close all Windows" );
					closeall.setMnemonic( KeyEvent.VK_A );
					windowSelector.addSeparator();

					windowSelector.add( tilehor );
					tilever.setToolTipText( "Arrange Windows in vertical tiles" );
					tilever.setMnemonic( KeyEvent.VK_H );
					tilever.setIcon( DbAction.getIcon( "window_tile_vertical1" ) );
					windowSelector.add( tilever );
					tilehor.setToolTipText( "Arrange Windows in horizontal tiles" );
					tilehor.setMnemonic( KeyEvent.VK_V );
					tilehor.setIcon( DbAction.getIcon( "window_tile_horizontal1" ) );

					windowSelector.add( tilec );
					tilec.setToolTipText( "Arrange Windows in cascade" );
					tilec.setMnemonic( KeyEvent.VK_S );
					tilec.setIcon( DbAction.getIcon( "window_cascade1" ) );
					windowSelector.addSeparator();
				}

				int numI = 0;
				for ( final JInternalFrame f : frames ) {
					numI++;

					JMenuItem i = new JMenuItem( numI + ". " + f.getTitle() );
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

			}
		} );

		return desktopPane;
	}

	private JComponent makeHelpPane() {
		// Here we read the release notes text file
		StringBuilder helpdata = new StringBuilder();
		helpdata.append( "<html><h2>OS-EM Semantic Toolkit</h2>" );
		helpdata.append( "<h3>About</h3>" );
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

		helpdata.append( "<h3>Build Information</h3>" );
		Properties buildprops = GuiUtility.getBuildProperties();
		if ( buildprops.isEmpty() ) {
			helpdata.append( "no build information found" );
		}
		else {
			for ( String str : buildprops.stringPropertyNames() ) {
				helpdata.append( str ).append( ": " ).
						append( buildprops.getProperty( str, "not available" ) ).
						append( "<br/>\n" );
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
		JMenu tools = new JMenu( "Tools" );
		tools.setMnemonic( KeyEvent.VK_T );
		tools.setToolTipText( "Additional data tools" );
		tools.getAccessibleContext().setAccessibleName( "Additional data tools" );
		tools.getAccessibleContext().setAccessibleDescription( "Additional data tools" );

		tools.add( loggingItem );
		tools.add( gQueryBuilderItem );
		tools.add( insightManagerItem );
		tools.add( semanticExplorerItem );

		return tools;
	}

	//Added 508 compliance changes
	protected JMenu buildDatabaseMenu() {
		final JMenu db = new JMenu( "Database" );
		db.add( toggler );
		//Quality Check
		db.add( consistencyCheck );
		db.addSeparator();
		//Export
		JMenu exptop = new JMenu( "Export" );
		exptop.setToolTipText( "Export Database Activities" );
		exptop.getAccessibleContext().setAccessibleName( "Export Database Activities" );
		exptop.getAccessibleContext().setAccessibleDescription( "Export Database Activities" );
		exptop.setMnemonic( KeyEvent.VK_E );
		exptop.setIcon( DbAction.getIcon( "exportdb" ) );

		//Loading Sheets
		JMenu loadingsheets = new JMenu( "Loading Sheets" );
		loadingsheets.setToolTipText( "Export the Loading Sheets" );
		loadingsheets.getAccessibleContext().setAccessibleName( "Export the Loading Sheets" );
		loadingsheets.getAccessibleContext().setAccessibleDescription( "Export the Loading Sheets" );
		loadingsheets.setMnemonic( KeyEvent.VK_L );
		loadingsheets.setIcon( DbAction.getIcon( "import_data_review" ) );

		exptop.add( loadingsheets );
		//Semantic Web
		JMenu semsheets = new JMenu( "Semantic Web" );
		semsheets.setToolTipText( "Export the Semantic Web" );
		semsheets.getAccessibleContext().setAccessibleName( "Export the Semantic Web" );
		semsheets.getAccessibleContext().setAccessibleDescription( "Export the Semantic Web" );
		semsheets.setMnemonic( KeyEvent.VK_S );
		semsheets.setIcon( DbAction.getIcon( "semantic_dataset1" ) );
		exptop.add( semsheets );
		semsheets.add( exportttl );
		semsheets.add( exportnt );
		semsheets.add( exportrdf );
		//Nodes
		JMenu nodes = new JMenu( "Nodes" );
		nodes.setToolTipText( "Export the Nodes" );
		nodes.getAccessibleContext().setAccessibleName( "Export the Nodes" );
		nodes.getAccessibleContext().setAccessibleDescription( "Export the Nodes" );
		nodes.setMnemonic( KeyEvent.VK_N );
		nodes.setIcon( DbAction.getIcon( "protege/individual" ) );

		loadingsheets.add( nodes );
		//Nodes SubMenu
		nodes.add( expnodes );
		nodes.add( expSpecNodes );
		//RelationShips
		JMenu relationS = new JMenu( "Relationships" );
		relationS.setToolTipText( "Export the Relations" );
		relationS.getAccessibleContext().setAccessibleName( "Export the Relations" );
		relationS.getAccessibleContext().setAccessibleDescription( "Export the Relations" );
		relationS.setMnemonic( KeyEvent.VK_R );
		relationS.setIcon( DbAction.getIcon( "relationship1" ) );
		loadingsheets.add( relationS );
		//Relationships SubMenu
		relationS.add( exprels );
		relationS.add( expSpecRels );

		loadingsheets.add( expall );
		exptop.add( exportinsights );

		JMenu gexp = new JMenu( "Graph" );
		gexp.setToolTipText( "Database Graphs" );
		gexp.getAccessibleContext().setAccessibleName( "Database Graphs" );
		gexp.getAccessibleContext().setAccessibleDescription( "Database Graphs" );
		gexp.setMnemonic( KeyEvent.VK_G );
		gexp.setIcon( DbAction.getIcon( "graph-icon" ) );

		gexp.add( expgraphml );
		gexp.add( expgson );

		exptop.add( gexp );
		db.add( exptop );

		JMenu importtop = new JMenu( "Import" );
		importtop.setToolTipText( "Import Database Operations" );
		importtop.getAccessibleContext().setAccessibleName( "Import Database Operations" );
		importtop.getAccessibleContext().setAccessibleDescription( "Import Database Operations" );
		importtop.setMnemonic( KeyEvent.VK_I );
		importtop.setIcon( DbAction.getIcon( "importdb" ) );
		importtop.setMnemonic( KeyEvent.VK_I );
		db.add( importtop );
		//JMenu iDatabase = new JMenu( "Database" );
		//iDatabase.setToolTipText("Import Database Operations");
		//iDatabase.setMnemonic(KeyEvent.VK_D);
		//importtop.add( iDatabase );
		final JMenu mergeroot = new JMenu( DbAction.MERGE );
		mergeroot.setToolTipText( "Merge the Data between databases" );
		mergeroot.getAccessibleContext().setAccessibleName( "Merge the Data between databases" );
		mergeroot.getAccessibleContext().setAccessibleDescription( "Merge the Data between databases" );
		mergeroot.setMnemonic( KeyEvent.VK_D );
		mergeroot.setIcon( DbAction.getIcon( "semossjnl" ) );
		mergeroot.setEnabled( false );
		importtop.add( mergeroot );
		importtop.add( importls );

		JMenu insights = new JMenu( "Insights" );
		insights.setToolTipText( "Import Insight Operations" );
		insights.getAccessibleContext().setAccessibleName( "Import Insight Operations" );
		insights.getAccessibleContext().setAccessibleDescription( "Import Insight Operations" );

		insights.setMnemonic( KeyEvent.VK_I );

		//Ticket #792
		insights.add( importInsights );
		insights.add( resetInsights );
		importtop.add( insights );
		//Insite Manager Icon
		insights.setIcon( DbAction.getIcon( "insight_manager_tab1" ) );
		//importInsights
		db.setMnemonic( KeyEvent.VK_D );
		db.setToolTipText( "Database operations" );
		db.getAccessibleContext().setAccessibleName( "Database operations" );
		db.getAccessibleContext().setAccessibleDescription( "Database operations" );

		db.add( cloner );
		db.add( clearer );
		//db.add( unmounter) ;
		db.addSeparator();

		db.add( proper );
		db.setEnabled( false );
		ListSelectionListener lsl = new ListSelectionListener() {

			@Override
			public void valueChanged( ListSelectionEvent e ) {
				//	if ( e.getValueIsAdjusting() ) {
				//		return;
				//	}

				IEngine engine = repoList.getSelectedValue();
				mergeroot.removeAll();
				mergeroot.setEnabled( repoList.getRepositoryModel().size() > 1 );
				for ( IEngine eng : repoList.getRepositoryModel().getElements() ) {
					if ( !eng.equals( engine ) ) {
						mergeroot.add( new MergeAction( PlayPane.UIPROGRESS, engine, eng,
								PlayPane.this ) );
					}
				}

				db.setEnabled( null != engine );
			}
		};
		repoList.addListSelectionListener( lsl );

		return db;
	}

	protected JMenu buildHelpMenu() {
		JMenu help = new JMenu( "Help" );
		JMenuItem helpitem = new JMenuItem( "About OS-EM Semantic Toolkit" );
		help.add( helpitem );
		help.add( new AbstractAction( "Options" ) {
			private static final long serialVersionUID = -5306914853342321083L;

			@Override
			public void actionPerformed( ActionEvent ae ) {
				SettingsPanel.showDialog( PlayPane.this );
			}
		} );

		helpitem.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				final JDialog dlg = new JDialog( (Frame) null, false );
				dlg.setTitle( "About OS-EM Semantic Toolkit" );
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

	//Added 508 compliance code
	protected JMenu buildViewMenu() {
		final Map<String, JPanel> preflistenermap = new HashMap<>();
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
						if ( null != cmd ) { // Enable- Disable Logic
							switch ( cmd ) {
								case LOGGING:
									item.setToolTipText( "Disable the Logging Tab" );
									item.getAccessibleContext().setAccessibleName( "Disable the Logging Tab " );
									item.getAccessibleContext().setAccessibleDescription( "Disable the Logging Tab " );
									break;
								default:
									item.setToolTipText( "Disable " + cmd );
									item.getAccessibleContext().setAccessibleName( "Disable " + cmd );
									item.getAccessibleContext().setAccessibleDescription( "Disable " + cmd );
									break;
							}
						}

						if ( loggingPanel == panel ) {
							rightTabs.addTab( "Logging", DbAction.getIcon( "log_tab1" ), loggingPanel,
									"This tab keeps a log of SEMOSS warnings and error messges for "
									+ "use by the SEMOSS development team" );
							int idx = rightTabs.indexOfComponent( loggingPanel );
							CloseableTab ct = new PlayPaneCloseableTab( rightTabs, loggingItem,
									DbAction.getIcon( "log_tab1" ) );
							rightTabs.setTabComponentAt( idx, ct );
						}
					}
					else {
						if ( null != cmd ) {
							switch ( cmd ) {
								case LOGGING:
									item.setToolTipText( "Enable the Logging Tab" );
									item.getAccessibleContext().setAccessibleName( "Enable the Logging Tab " );
									item.getAccessibleContext().setAccessibleDescription( "Enable the Logging Tab " );
									break;
								default:
									item.setToolTipText( "Enable " + cmd );
									item.getAccessibleContext().setAccessibleName( "Enable " + cmd );
									item.getAccessibleContext().setAccessibleDescription( "Enable " + cmd );
									break;
							}
						}

						if ( loggingPanel == panel ) {
							rightTabs.remove( panel );
						}
						else {
							dbexplorer.remove( panel );
						}
					}
				}
			}
		};

		//Status Tab
		final JCheckBoxMenuItem statbar = new JCheckBoxMenuItem( "Status Bar",
				prefs.getBoolean( "showStatus", true ) );
		if ( prefs.getBoolean( "showStatus", true ) == true ) {
			statbar.setToolTipText( "Disable the Status bar" );
			statbar.getAccessibleContext().setAccessibleName( "Disable the Status bar" );
			statbar.getAccessibleContext().setAccessibleDescription( "Disable the Status bar" );
		}
		else {
			statbar.setToolTipText( "Enable the Status bar" );
			statbar.getAccessibleContext().setAccessibleName( "Enable the Status bar" );
			statbar.getAccessibleContext().setAccessibleDescription( "Enable the Status bar" );
		}

		statbar.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				statusbar.setVisible( statbar.isSelected() );
				prefs.putBoolean( "showStatus", statusbar.isVisible() );
				if ( statusbar.isVisible() ) {
					statbar.setToolTipText( "Disable the Status bar" );
					statbar.getAccessibleContext().setAccessibleName( "Disable the Status bar" );
					statbar.getAccessibleContext().setAccessibleDescription( "Disable the Status bar" );
				}
				else {
					statbar.setToolTipText( "Enable the Status bar" );
					statbar.getAccessibleContext().setAccessibleName( "Enable the Status bar" );
					statbar.getAccessibleContext().setAccessibleDescription( "Enable the Status bar" );

				}
			}
		} );

		loggingItem.setSelected( getProp( LOGGING ) );
		loggingItem.setActionCommand( LOGGING );
		loggingItem.addActionListener( preflistener );

		if ( getProp( LOGGING ) ) {
			loggingItem.setToolTipText( "Disable the Logging Tab" );
			loggingItem.getAccessibleContext().setAccessibleName( "Disable the Logging Tab" );
			loggingItem.getAccessibleContext().setAccessibleDescription( "Disable the Logging Tab" );
		}
		else {
			loggingItem.setToolTipText( "Enable the Logging Tab" );
			loggingItem.getAccessibleContext().setAccessibleName( "Enable the Logging Tab" );
			loggingItem.getAccessibleContext().setAccessibleDescription( "Enable the Logging Tab" );
		}

		//Tool Bar
		final JCheckBoxMenuItem tb = new JCheckBoxMenuItem( "Tool Bar",
				prefs.getBoolean( "showToolBar", true ) );

		if ( prefs.getBoolean( "showToolBar", true ) == true ) {
			tb.setToolTipText( "Disable the Tool Bar" );
			tb.getAccessibleContext().setAccessibleName( "Disable the Tool Bar" );
			tb.getAccessibleContext().setAccessibleDescription( "Disable the Tool Bar" );
		}
		else {
			tb.setToolTipText( "Enable the Tool Bar" );
			tb.getAccessibleContext().setAccessibleName( "Enable the Tool Bar" );
			tb.getAccessibleContext().setAccessibleDescription( "Enable the Tool Bar" );
		}

		tb.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				toolbar.setVisible( tb.isSelected() );
				prefs.putBoolean( "showToolBar", toolbar.isVisible() );
				if ( toolbar.isVisible() ) {
					tb.setToolTipText( "Disable the Tool bar" );
					tb.getAccessibleContext().setAccessibleName( "Disable the Tool Bar" );
					tb.getAccessibleContext().setAccessibleDescription( "Disable the Tool Bar" );
				}
				else {
					tb.setToolTipText( "Enable the Tool bar" );
					tb.getAccessibleContext().setAccessibleName( "Enable the Tool Bar" );
					tb.getAccessibleContext().setAccessibleDescription( "Enable the Tool Bar" );
				}
			}
		} );

		splithider = new JCheckBoxMenuItem( "Database Explorer", true );

		splithider.setToolTipText( "Disable the Database Explorer" );
		splithider.getAccessibleContext().setAccessibleName( splithider.getToolTipText() );
		splithider.getAccessibleContext().setAccessibleDescription( splithider.getToolTipText() );

		splithider.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent ae ) {
				dbexplorer.setVisible( !dbexplorer.isVisible() );
				if ( dbexplorer.isVisible() ) {
					mainSplitPane.setDividerLocation( 0.25 );
					splithider.setToolTipText( "Disable the Database Explorer" );
				}
				else {
					splithider.setToolTipText( "Enable the Left Panel" );
				}

				splithider.getAccessibleContext().setAccessibleName( splithider.getToolTipText() );
				splithider.getAccessibleContext().setAccessibleDescription( splithider.getToolTipText() );
			}
		} );

		hidecsp = new JCheckBoxMenuItem( "Query Panel", getProp( QUERYPANEL ) );

		if ( getProp( QUERYPANEL ) ) {
			hidecsp.setToolTipText( "Disable the Query Panel" );
			hidecsp.getAccessibleContext().setAccessibleName( "Disable the Query Panel" );
			hidecsp.getAccessibleContext().setAccessibleDescription( "Disable the Query Panel" );
		}
		else {
			hidecsp.setToolTipText( "Enable the Query Panel" );
			hidecsp.getAccessibleContext().setAccessibleName( "Enable the Query Panel" );
			hidecsp.getAccessibleContext().setAccessibleDescription( "Enable the Query Panel" );
		}

		hidecsp.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent ae ) {
				customSparqlPanel.setVisible( !customSparqlPanel.isVisible() );
				prefs.putBoolean( QUERYPANEL, customSparqlPanel.isVisible() );
				if ( customSparqlPanel.isVisible() ) {
					combinedSplitPane.setDividerLocation( 0.75 );
					hidecsp.setToolTipText( "Disable the Query Panel" );
					hidecsp.getAccessibleContext().setAccessibleName( "Disable the Query Panel" );
					hidecsp.getAccessibleContext().setAccessibleDescription( "Disable the Query Panel" );
				}
				else {
					hidecsp.setToolTipText( "Enable the Query Panel" );
				}
			}
		} );

		gQueryBuilderItem.setSelected( getProp( GQUERYBUILDER ) );
		if ( getProp( GQUERYBUILDER ) ) {
			gQueryBuilderItem.setToolTipText( "Disable the Graphical Query Builder Tab" );
			makeGQB();
		}
		else {
			gQueryBuilderItem.setToolTipText( "Enable the Graphical Query Builder Tab" );
		}

		gQueryBuilderItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				boolean ischecked = gQueryBuilderItem.isSelected();
				prefs.putBoolean( GQUERYBUILDER, ischecked );
				DIHelper.getInstance().getCoreProp().setProperty( GQUERYBUILDER,
						Boolean.toString( ischecked ) );

				if ( ischecked ) {
					makeGQB();
				}
				else {
					rightTabs.remove( gQueryBuilderPanel );
					gQueryBuilderItem.setToolTipText( "Enable the Graphical Query Builder Tab" );
				}
			}
		} );
		insightManagerItem.setMnemonic( KeyEvent.VK_I );
		insightManagerItem.setSelected( getProp( IMANAGE_2 ) );

		if ( getProp( IMANAGE_2 ) == true ) {
			insightManagerItem.setMnemonic( KeyEvent.VK_I );
			insightManagerItem.setToolTipText( "Disable the Insite Manager  Tab" );
		}
		else {
			insightManagerItem.setToolTipText( "Enable the Insite Manager  Tab" );
		}

		insightManagerItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				boolean ischecked = insightManagerItem.isSelected();
				prefs.putBoolean( IMANAGE_2, ischecked );
				DIHelper.getInstance().getCoreProp().setProperty( IMANAGE_2,
						Boolean.toString( ischecked ) );

				if ( ischecked ) {
					rightTabs.addTab( "Insight Manager", DbAction.getIcon( "insight_manager_tab1" ), insightManager,
							"Manage perspectives and insights" );
					CloseableTab ct2_2 = new PlayPaneCloseableTab( rightTabs, insightManagerItem,
							DbAction.getIcon( "insight_manager_tab1" ) );
					insightManagerItem.setMnemonic( KeyEvent.VK_I );
					int idx = rightTabs.indexOfComponent( insightManager );
					rightTabs.setTabComponentAt( idx, ct2_2 );

					insightManagerItem.setToolTipText( "Disable the Insite Manager  Tab" );
				}
				else {
					rightTabs.remove( insightManager );
					insightManagerItem.setToolTipText( "Enable the Insite Manager Tab" );
				}
			}
		} );

		semanticExplorerItem.setSelected( getProp( SEMANTICEXPLORER ) );
		semanticExplorerItem.setMnemonic( KeyEvent.VK_S );
		if ( getProp( SEMANTICEXPLORER ) ) {
			semanticExplorerItem.setToolTipText( "Disable the Semantic Explorer Tab" );
		}
		else {
			semanticExplorerItem.setToolTipText( "Enable the Semantic Explorer Tab" );
		}

		semanticExplorerItem.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				boolean ischecked = semanticExplorerItem.isSelected();
				prefs.putBoolean( SEMANTICEXPLORER, ischecked );
				DIHelper.getInstance().getCoreProp().setProperty( SEMANTICEXPLORER,
						Boolean.toString( ischecked ) );

				if ( ischecked ) {
					rightTabs.addTab( "Semantic Explorer", DbAction.getIcon( "semantic_dataset2" ), semanticExplorer,
							"Explore the classes and instances" );
					rightTabs.setTabComponentAt(
							rightTabs.indexOfComponent( semanticExplorer ),
							new PlayPaneCloseableTab( rightTabs, semanticExplorerItem, DbAction.getIcon( "semantic_dataset2" ) )
					);
					semanticExplorerItem.setToolTipText( "Disable the Semantic Explorer Tab" );
				}
				else {
					rightTabs.remove( semanticExplorer );
					semanticExplorerItem.setToolTipText( "Enable the Semantic Explorer Tab" );
				}
			}
		} );

		JMenu view = new JMenu( "View" );
		view.setMnemonic( KeyEvent.VK_V );
		view.setToolTipText( "Enable or disable the application tabs" );
		view.getAccessibleContext().setAccessibleName( "Enable the Insite Manager Tab" );
		view.getAccessibleContext().setAccessibleDescription( "Enable the Insite Manager Tab" );

		view.add( gQueryBuilderItem );
		gQueryBuilderItem.setMnemonic( KeyEvent.VK_B );
		view.add( insightManagerItem );
		insightManagerItem.setMnemonic( KeyEvent.VK_I );
		view.add( splithider );
		splithider.setMnemonic( KeyEvent.VK_L );
		view.add( loggingItem );
		//Icon for the Menu Item
		//logging.setIcon( DbAction.getIcon( "log_tab1" ));
		loggingItem.setMnemonic( KeyEvent.VK_L );
		view.add( hidecsp );
		hidecsp.setMnemonic( KeyEvent.VK_Q );
		view.add( statbar );
		statbar.setMnemonic( KeyEvent.VK_S );
		view.add( tb );
		tb.setMnemonic( KeyEvent.VK_T );

		return view;
	}

	private GraphicalQueryPanel makeGQB() {
		gQueryBuilderPanel = new GraphicalQueryPanel( PlayPane.UIPROGRESS, colorsShapes );
		gQueryBuilderPanel.setEngine( repoList.getSelectedValue() );
		gQueryBuilderPanel.setSparqlArea( customSparqlPanel.getOpenEditor() );

		rightTabs.addTab( "Graphical Query Builder", DbAction.getIcon( "graphic_query" ),
				gQueryBuilderPanel, "Build queries graphically and generate Sparql" );
		CloseableTab ct1 = new PlayPaneCloseableTab( rightTabs, gQueryBuilderItem,
				DbAction.getIcon( "graphic_query" ) );
		int idx = rightTabs.indexOfComponent( gQueryBuilderPanel );
		rightTabs.setTabComponentAt( idx, ct1 );
		gQueryBuilderItem.setToolTipText( "Disable the Graphical Query Builder Tab" );
		return gQueryBuilderPanel;
	}

	protected void buildMenuBar() {
		JMenuBar menu = new JMenuBar();
		menu.getAccessibleContext().setAccessibleName( "TopMenu" );
		menu.getAccessibleContext().setAccessibleDescription( "V-CAMP SEMOSS APPLICATION MENU" );

		JMenuItem exiter = new JMenuItem( new AbstractAction( "Exit" ) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( ActionEvent e ) {
				System.exit( 0 );
			}
		} );

		fileMenuSave.setEnabled( false );
		fileMenuSaveAs.setEnabled( false );
		fileMenuSaveAll.setEnabled( false );

		JMenu newmenu = new JMenu( "New" );
		newmenu.setToolTipText( "Create a new Database or Loading Sheet" );
		newmenu.getAccessibleContext().setAccessibleName( "Create a new Database or Loading Sheet" );
		newmenu.getAccessibleContext().setAccessibleDescription( "Create a new Database or Loading Sheet" );
		newmenu.setMnemonic( KeyEvent.VK_N );
		newmenu.setIcon( DbAction.getIcon( "file-new1" ) );
		fileMenu.add( newmenu );

		// Database
		JMenuItem jmi = newmenu.add( creater );
		jmi.setText( "Database" );
		jmi.getAccessibleContext().setAccessibleName( "Create a new Database " );
		jmi.getAccessibleContext().setAccessibleDescription( "Create a new Database " );
		jmi.setMnemonic( KeyEvent.VK_D );
		jmi = newmenu.add( newls );

		//Loading Sheet
		jmi.setText( "Loading Sheet" );
		jmi.getAccessibleContext().setAccessibleName( "Create a new Loading Sheet " );
		jmi.getAccessibleContext().setAccessibleDescription( "Create a new Loading Sheet" );
		jmi.setMnemonic( KeyEvent.VK_L );

		fileMenu.setMnemonic( KeyEvent.VK_F );
		fileMenu.setToolTipText( "File Operations" );
		fileMenu.getAccessibleContext().setAccessibleName( "File Operations" );
		fileMenu.getAccessibleContext().setAccessibleDescription( "File Operations" );

		//Open Menu
		JMenu openmenu = new JMenu( "Open" );
		openmenu.setToolTipText( "Open Database or SPARQL file" );
		openmenu.getAccessibleContext().setAccessibleName( openmenu.getToolTipText() );
		openmenu.getAccessibleContext().setAccessibleDescription( openmenu.getToolTipText() );
		openmenu.setMnemonic( KeyEvent.VK_O );
		openmenu.setIcon( DbAction.getIcon( "open-file3" ) );
		fileMenu.add( openmenu );

		jmi = openmenu.add( mounter );
		jmi.setText( "Local DB" );
		jmi.setToolTipText( "Open Local Files to Import" );
		jmi.getAccessibleContext().setAccessibleName( "Open Local Files to Import" );
		jmi.getAccessibleContext().setAccessibleDescription( "Open Local Files to Import" );
		jmi.setMnemonic( KeyEvent.VK_L );
		jmi.setIcon( DbAction.getIcon( "local-db" ) );

		jmi = openmenu.add( remoteDb );
		jmi.setText( "Remote DB" );
		jmi.setToolTipText( "Open Remote Files to Import" );
		jmi.getAccessibleContext().setAccessibleName( "Open Remote Files to Import" );
		jmi.getAccessibleContext().setAccessibleDescription( "Open Remote Files to Import" );
		jmi.setMnemonic( KeyEvent.VK_R );
		jmi.setIcon( DbAction.getIcon( "remote-db" ) );

		jmi = openmenu.add( new OpenSparqlAction( UIPROGRESS, this, customSparqlPanel ) );
		jmi.setText( "SPARQL File" );
		jmi.setToolTipText( "Open SPARQL File in Query Panel" );
		jmi.getAccessibleContext().setAccessibleName( jmi.getToolTipText() );
		jmi.getAccessibleContext().setAccessibleDescription( jmi.getToolTipText() );
		jmi.setMnemonic( KeyEvent.VK_S );
		jmi.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				// make sure the query panel is visible if we load a sparql file
				if ( !hidecsp.isSelected() ) {
					hidecsp.doClick();
				}
			}
		} );

		fileMenu.addSeparator();
		fileMenu.add( unmounter );
		unmounter.setEnabled( false );
		fileMenuSave.setToolTipText( "Save changes" );
		fileMenuSave.getAccessibleContext().setAccessibleName( "Locally Save changes" );
		fileMenuSave.getAccessibleContext().setAccessibleDescription( "Locally Save changes" );

		fileMenuSave.setMnemonic( KeyEvent.VK_S );
		fileMenuSave.setIcon( DbAction.getIcon( "save_diskette1" ) );
		fileMenuSaveAs.getAccessibleContext().setAccessibleName( "Save to a new file name" );
		fileMenuSaveAs.getAccessibleContext().setAccessibleDescription( "Save to a new file name" );
		fileMenu.add( fileMenuSave );
		fileMenuSaveAs.setToolTipText( "Save to a new file name" );
		fileMenuSaveAs.setMnemonic( KeyEvent.VK_A );
		fileMenuSaveAs.setIcon( DbAction.getIcon( "save_as_diskette1" ) );
		fileMenu.add( fileMenuSaveAs );
		fileMenuSaveAs.getAccessibleContext().setAccessibleName( "SaveAs" );
		fileMenuSaveAs.getAccessibleContext().setAccessibleDescription( "Save to a new file name" );
		fileMenuSaveAll.setToolTipText( "Save all changes" );
		fileMenuSaveAll.setMnemonic( KeyEvent.VK_V );
		fileMenuSaveAll.setIcon( DbAction.getIcon( "save_alldiskette1" ) );
		fileMenuSaveAll.getAccessibleContext().setAccessibleName( "SaveAll" );
		fileMenuSaveAll.getAccessibleContext().setAccessibleDescription( "Save All changes" );
		//	fileMenu.add( fileMenuSaveAll );

		fileMenu.addSeparator();
		exiter.setIcon( DbAction.getIcon( "exit1" ) );
		exiter.setMnemonic( KeyEvent.VK_X );
		exiter.setToolTipText( "Exit the V-CAMP SEMOSS Tool" );
		exiter.getAccessibleContext().setAccessibleName( "Exit the V-CAMP SEMOSS Tool" );
		exiter.getAccessibleContext().setAccessibleDescription( "Exit the V-CAMP SEMOSS Tool" );

		fileMenu.add( exiter );
		menu.add( fileMenu );

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
			//	menu.add( Box.createHorizontalGlue() );
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

	public void showDesktop() {
		rightTabs.setSelectedIndex( 0 );
	}

	public static boolean getProp( String propstr ) {
		return getProp( propstr, false );
	}

	public static boolean getProp( String propstr, boolean defaultValue ) {
		String prop = prefs.get( propstr, null );
		if ( null == prop ) {
			prop = DIHelper.getInstance().getProperty( propstr );
		}

		if ( null == prop ) {
			prop = Boolean.toString( defaultValue );
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
	}

	protected void registerPlaySheets( OutputTypeRegistry reg ) {

		reg.register( InsightOutputType.GRID, GridPlaySheet.class, "Grid",
				GuiUtility.loadImageIcon( "icons16/questions_grid2_16.png" ),
				"GridPlaySheet Hint: SELECT ?x1 ?x2 ?x3 WHERE{ ... }" );

		reg.register( InsightOutputType.GRID_RAW, GridRAWPlaySheet.class, "Raw Grid",
				GuiUtility.loadImageIcon( "icons16/questions_raw_grid2_16.png" ),
				"GridRAWPlaySheet Hint: SELECT ?x1 ?x2 ?x3 WHERE{ ... }" );

		reg.register( InsightOutputType.COLUMN_CHART, ColumnChartPlaySheet.class,
				"Column Chart", GuiUtility.loadImageIcon( "icons16/questions_bar_chart1_16.png" ),
				"ColumnChartPlaySheet Hint: SELECT ?xAxis ?yAxis1 (OPTIONAL) ?yAxis2 ?yAxis3 ... (where all yAxis values are numbers) WHERE { ... }" );

		reg.register( InsightOutputType.GRAPH, GraphPlaySheet.class, "Graph",
				GuiUtility.loadImageIcon( "icons16/questions_graph_16.png" ),
				"GraphPlaySheet Hint: CONSTRUCT {?subject ?predicate ?object} WHERE{ ... }" );

		reg.register( InsightOutputType.DENDROGRAM, DendrogramPlaySheet.class,
				"Dendrogram", GuiUtility.loadImageIcon( "icons16/questions_dendrogram1_16.png" ),
				"DendrogramPlaySheet Hint: SELECT DISTINCT ?var-1 ?var-2 ?var-3 ... (where ?var-2 contains children of ?var-1, ?var-3 of ?var-2, etc.) WHERE { ... }" );

		reg.register( InsightOutputType.GRID_SCATTER, GridScatterSheet.class, "Grid Scatter",
				GuiUtility.loadImageIcon( "icons16/questions_grid_scatter1_16.png" ),
				"GridScatterSheet Hint: SELECT ?elementName ?xAxisValues ?yAxisValues (OPTIONAL)?zAxisValues WHERE{ ... }" );
		reg.register( InsightOutputType.HEATMAP, HeatMapPlaySheet.class, "Heat Map",
				GuiUtility.loadImageIcon( "icons16/questions_heat_map3_16.png" ),
				"HeatMapPlaySheet Hint: SELECT ?xAxisList ?yAxisList ?numericHeatValue WHERE{ ... } GROUP BY ?xAxisList ?yAxisList" );

		reg.register( InsightOutputType.PARALLEL_COORDS, ParallelCoordinatesPlaySheet.class,
				"Parallel Coordinates", GuiUtility.loadImageIcon( "icons16/questions_parcoords6_16.png" ),
				"ParallelCoordinatesPlaySheet Hint: SELECT ?axis1 ?axis2 ?axis3 WHERE{ ... }" );

		reg.register( InsightOutputType.PIE_CHART, PieChartPlaySheet.class, "Pie Chart",
				GuiUtility.loadImageIcon( "icons16/questions_pie_chart1_16.png" ),
				"PieChartPlaySheet Hint: SELECT ?wedgeName ?wedgeValue WHERE { ... }" );

//		reg.register( InsightOutputType.SANKEY, SankeyPlaySheet.class, "Sankey Diagram",
//				GuiUtility.loadImageIcon( "icons16/questions_sankey2_16.png" ),
//				"SankeyPlaySheet Hint: SELECT ?source ?target ?value ?target2 ?value2 ?target3 ?value3...etc  Note: ?target is the source for ?target2 and ?target2 is the source for ?target3...etc WHERE{ ... }" );
		reg.register( InsightOutputType.HEATMAP_US, USHeatMapPlaySheet.class, "US Heat Map",
				GuiUtility.loadImageIcon( "icons16/questions_us_heat_map1_16.png" ),
				"USHeatMapPlaySheet Hint: SELECT ?state ?numericHeatValue WHERE{ ... }" );

		reg.register( InsightOutputType.HEATMAP_WORLD, WorldHeatMapPlaySheet.class, "World Heat Map",
				GuiUtility.loadImageIcon( "icons16/questions_world_heat_map3_16.png" ),
				"WorldHeatMapPlaySheet Hint: SELECT ?country ?numericHeatValue WHERE{ ... }" );

		reg.register( InsightOutputType.HEATMAP_APPDUPE, AppDupeHeatMapSheet.class,
				"Application Duplication Heat Map",
				GuiUtility.loadImageIcon( "icons16/questions_heat_map3_16.png" ),
				"AppDupeHeatMapPlaySheet Hint: SELECT ?xAxisList ?yAxisList ?numericHeatValue WHERE{ ... } GROUP BY ?xAxisList ?yAxisList" );

		reg.register( InsightOutputType.GRAPH_METAMODEL, GraphPlaySheet.class, "Metamodel Graph",
				GuiUtility.loadImageIcon( "icons16/questions_metamodel1_16.png" ),
				"MetamodelGraphPlaySheet Hint: SELECT DISTINCT ?source ?relation ?target WHERE{ ... }" );
	}

	protected class PlayPaneCloseableTab extends CloseableTab {

		private static final long serialVersionUID = -1674137465659730374L;
		private final JCheckBoxMenuItem item;

		public PlayPaneCloseableTab( JTabbedPane parent, JCheckBoxMenuItem item,
				Icon icon ) {
			super( parent );
			setIcon( icon );
			this.item = item;
		}

		@Override
		public void actionPerformed( ActionEvent ae ) {
			if ( null == item ) {
				super.actionPerformed( ae );
			}
			else {
				item.doClick();
			}
		}
	}

	public static SemanticExplorerPanel getSemanticExplorerPanel() {
		return semanticExplorer;
	}
}
