/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportFileReader;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.poi.main.XlsWriter;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.rdf.engine.util.EngineUtil;
import gov.va.semoss.rdf.engine.util.QaChecker;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.ui.actions.AbstractSavingAction;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.components.CloseableTab.MarkType;
import gov.va.semoss.ui.components.models.LoadingSheetModel;
import gov.va.semoss.ui.components.playsheets.LoadingPlaySheetBase;
import gov.va.semoss.ui.components.playsheets.NodeLoadingPlaySheet;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.ui.components.playsheets.RelationshipLoadingPlaySheet;
import gov.va.semoss.ui.components.renderers.RepositoryRenderer;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.DefaultIcons;
import gov.va.semoss.util.Utility;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class LoadingPlaySheetFrame extends PlaySheetFrame {

	public static final String LOAD = "load";
	public static final String NEWTAB = "newtab";
	public static final String REALTIME_QA = "realtime";
	public static final String SHOW_ERRS = "showerrors";

	private static final Logger log = Logger.getLogger( LoadingPlaySheetFrame.class );

	private boolean dometamodel;
	private boolean doconformance;
	private boolean calcinfers;
	private boolean doreplace;

	private final List<File> toload = new ArrayList<>();
	private final List<LoadingPlaySheetBase> sheets = new ArrayList<>();

	private final LoadingAction loader = new LoadingAction();
	private final JCheckBox showerrs = new JCheckBox( "Show Only Errors" );
	private final SaveAllAction saveall = new SaveAllAction();
	private final AddAction addtab = new AddAction();
	private final ConformanceAction cnfr = new ConformanceAction();

	private final QaChecker realtimer = new QaChecker();
	private final JToggleButton timertoggle = new JToggleButton( cnfr );

	public LoadingPlaySheetFrame( IEngine eng ) {
		this( eng, true, true, false, false );
		setTitle( "Loading Sheet" );
	}

	public LoadingPlaySheetFrame( IEngine eng, boolean calc, boolean meta,
			boolean conform, boolean replace ) {
		super( eng );
		calcinfers = calc;
		dometamodel = meta;
		doconformance = conform;
		doreplace = replace;
	//	setTitle( "Import Data Review 4" );
	//	fileToLoad.
		timertoggle.setText( null );
		timertoggle.setSelected( doconformance );

		showerrs.setVisible( doconformance );
		
	}

	public LoadingPlaySheetFrame( IEngine eng, Collection<File> toload, boolean calc,
			boolean meta, boolean conform, boolean replace ) {
		this( eng, calc, meta, conform, replace );
		String sName = toload.toString();
		setTitle( sName.substring(sName.lastIndexOf("\\")+1, sName.lastIndexOf("]")) );
		setToolTipText("Window of" + sName.substring(sName.lastIndexOf("\\")+1, sName.lastIndexOf("]")));
		populateForFiles( toload );
	}

	public LoadingPlaySheetFrame() {
		this( null, false, false, false, false );
		hideProgress();
	}

	public LoadingPlaySheetFrame( ImportData data ) {
		this( null, data );
	}

	public LoadingPlaySheetFrame( IEngine eng, ImportData data ) {
		this( eng, false, data.getMetadata().isAutocreateMetamodel(), true, false );

		setTitle( "Import Data Review" );
		

		LoadingPlaySheetBase first = null;
		for ( LoadingSheetData n : data.getSheets() ) {
			LoadingPlaySheetBase ps = add( n );
			if ( null == first ) {
				first = ps;
			}
		}

		if ( null != first ) {
			selectTab( 0 );
		}

		hideProgress();
	}

	public final LoadingPlaySheetBase add( LoadingSheetData data ) {
		LoadingPlaySheetBase ret = ( data.isRel()
				? new RelationshipLoadingPlaySheet( data )
				: new NodeLoadingPlaySheet( data ) );
		ret.getLoadingModel().setReadOnly( false );

		addTab( ret );

		if ( ret.getLoadingModel().hasConformanceErrors()
				|| ret.getLoadingModel().hasModelErrors() ) {
			CloseableTab tab
					= LoadingPlaySheetFrame.this.getTabComponent( ret );
			tab.setMark( MarkType.ERROR );
		}

		return ret;
	}

	public void addTab( LoadingPlaySheetBase c ) {
		super.addTab( c );
		sheets.add( c );
		showerrs.addActionListener( c );

		QaChecker el = ( timertoggle.isSelected() ? realtimer : null );
		c.getLoadingModel().setQaChecker( el );
	}

	@Override
	public ProgressTask getCreateTask( String s ) {
		return getLoadingTask();
	}

	@Override
	public ProgressTask getOverlayTask( String query, String titleIfNeeded,
			String tabTitleIfNeeded ) {
		return getLoadingTask();
	}

	public ProgressTask getLoadingTask() {
		String t = "Loading data" + ( isLoadable() ? " to "
				+ MetadataQuery.getEngineLabel( getEngine() ) : "" );
		final String error[] = new String[1];
		ProgressTask pt = new DisappearingProgressBarTask( t, new Runnable() {
			@Override
			public void run() {

				if ( doconformance ) {
					updateProgress( "Preparing Load", 0 );
					realtimer.clear();
					realtimer.loadCaches( getEngine() );
				}

				int progressPerFile = 100 / toload.size();
				File lastloaded = null;
				for ( File fileToLoad : toload ) {
					jBar.setString( "Reading " + fileToLoad );
					ImportFileReader rdr = EngineLoader.getDefaultReader( fileToLoad );
					log.debug( "importing file:" + fileToLoad );
					try {
						ImportData data = rdr.readOneFile( fileToLoad );
						data.findPropertyLinks();
						addProgress( "Finished reading " + fileToLoad, progressPerFile );

						if ( null == lastloaded ) {
							lastloaded = fileToLoad;
						}

						for ( LoadingSheetData n : data.getSheets() ) {
							add( n );
						}
					}
					catch ( IOException | ImportValidationException eee ) {
						log.error( eee, eee );
						error[0] = eee.getLocalizedMessage();
					}
				}

				LoadingPlaySheetFrame.this.selectTab( 0 );
				if ( null != lastloaded ) {
					LoadingPlaySheetFrame.this.saveall.setSaveFile( lastloaded );
				}
				announceErrors();
				
			}
		} ) {
			@Override
			public void done() {
				super.done();
				if ( null != error[0] ) {
					Utility.showError( error[0] );
				}
			}
		};

		return pt;
	}

	public boolean isLoadable() {
		return ( getEngine() != null );
	}

	@Override
	public void populateToolbar( JToolBar jtb ) {
		jtb.add( saveall );
		jtb.add( loader );
		jtb.add( addtab );

		jtb.add( timertoggle );
		jtb.add( showerrs );

		showerrs.repaint();

		super.populateToolbar( jtb );
	}

	@Override
	public Map<String, Action> getActions() {
		Map<String, Action> map = super.getActions();
		map.put( PlaySheetFrame.SAVE, saveall );
		map.put( PlaySheetFrame.SAVE_ALL, saveall );

		map.put( LOAD, loader );
		map.put( NEWTAB, addtab );
		map.put( REALTIME_QA, cnfr );

		return map;
	}

	@Override
	public void closeTab( PlaySheetCentralComponent c ) {
		if ( c instanceof RelationshipLoadingPlaySheet ) {
			sheets.remove( RelationshipLoadingPlaySheet.class.cast( c ) );
		}
		super.closeTab( c );
	}

	private void populateForFiles( Collection<File> files ) {
		toload.addAll( files );
	}

	public RelationshipLoadingPlaySheet addTab( String relname, List<Value[]> data,
			List<String> headings ) {
		RelationshipLoadingPlaySheet grid
				= new RelationshipLoadingPlaySheet( relname, data, headings );
		grid.setTitle( "Import Data Review" );
		addTab( grid );
		hideProgress();

		return grid;
	}

	public NodeLoadingPlaySheet addTab( List<Value[]> data, List<String> headings ) {
		NodeLoadingPlaySheet grid = new NodeLoadingPlaySheet();
		grid.setTitle( "Import Data Review" );
		grid.create( data, headings, getEngine() );
		addTab( grid );
		hideProgress();

		return grid;
	}

	private int announceErrors() {
		int errors = 0;
		for ( LoadingPlaySheetBase node : sheets ) {
			errors += node.getLoadingModel().getConformanceErrorCount();
			errors += node.getLoadingModel().getModelErrorColumns().size();
		}

		if ( errors > 0 ) {
			String error = ( 1 == errors ? "error was" : "errors were" );
			String msg = errors + " Quality " + error + " detected";
			JOptionPane.showMessageDialog( rootPane, msg,
					"Non-Conforming Values Detected", JOptionPane.INFORMATION_MESSAGE );
		}

		return errors;
	}

	private IEngine askForEngine( String title ) {

		RepositoryList repos = new RepositoryList();
		Collection<IEngine> engines = DIHelper.getInstance().getEngineMap().values();
		if ( 1 == engines.size() ) {
			return engines.iterator().next();
		}

		repos.getRepositoryModel().addAll( engines );
		repos.setCellRenderer( new RepositoryRenderer() );

		int ans = JOptionPane.showOptionDialog( null, new JScrollPane( repos ),
				"Select Engine to " + title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, new String[]{ title, "Cancel" }, title );
		if ( 0 == ans && null != repos.getSelectedValue() ) {
			return repos.getSelectedValue();
		}
		return null;
	}

	private class LoadingAction extends AbstractAction {

		public LoadingAction() {
			super( "Import Data", DbAction.getIcon( "importdb" ) );
			if ( isLoadable() ) {
				putValue( Action.SHORT_DESCRIPTION, "Commit this data to "
						+ MetadataQuery.getEngineLabel( getEngine() ) );
			}
			else {
				putValue( Action.SHORT_DESCRIPTION, "Commit this data to a DB" );
			}
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			final IEngine engine = ( null == getEngine()
					? askForEngine( "Load" ) : getEngine() );

			if ( null == engine ) {
				Utility.showMessage( "Load canceled" );
				return;
			}

			for ( LoadingPlaySheetBase ps : sheets ) {
				if ( !ps.okToLoad() ) {
					selectTab( ps );
					if ( !ps.correct() ) {
						Utility.showMessage( "Load canceled" );
						return;
					}
				}
			}

			if ( askForLoad( engine ) ) {
				final int ok[] = { 0 };
				String t = "Committing data to " + engine;
				int progressPerTab = 100 / sheets.size();

				ProgressTask pt = new ProgressTask( t, new Runnable() {

					@Override
					public void run() {
						updateProgress( "Preparing data", 0 );
						ImportData importdata = ImportData.forEngine( engine );

						for ( LoadingPlaySheetBase c : sheets ) {
							String text = "Preparing tab: " + c.getTitle();
							addProgress( text, progressPerTab );
							log.debug( text );

							LoadingSheetData lsd = c.getLoadingModel().toLoadingSheet( c.getTitle() );
							importdata.add( lsd );
						}

						try {
							String ename = MetadataQuery.getEngineLabel( engine );

							if ( doreplace ) {
								updateProgress( "Clearing " + ename, 15 );
								EngineUtil.clear( engine );
							}

							updateProgress( "Loading data to " + ename, 50 );
							ImportData errs = ( doconformance ? new ImportData() : null );
							importdata.getMetadata().setAutocreateMetamodel( dometamodel );
							EngineLoader el = new EngineLoader();
							el.loadToEngine( importdata, engine, errs );
							el.release();

							if ( !( null == errs || errs.isEmpty() ) ) {
								for ( LoadingSheetData lsd : errs.getSheets() ) {
									for ( LoadingNodeAndPropertyValues nap : lsd.getData() ) {
										if ( nap.hasError() ) {
											ok[0]++;
										}
									}
								}

								PlaySheetFrame psf = new LoadingPlaySheetFrame( engine, errs );
								psf.setTitle( "Quality Check Errors" );
								LoadingPlaySheetFrame.this.getDesktopPane().add( psf );
							}

							if ( calcinfers ) {
								updateProgress( "Calculating inferences in " + ename, 80 );
								engine.calculateInferences();
							}

							LoadingPlaySheetFrame.this.dispose();
						}
						catch ( RepositoryException | IOException | ImportValidationException e ) {
							log.error( e, e );
						}
					}
				} ) {
					@Override
					public void done() {
						super.done();

						if ( ok[0] > 0 ) {
							setLabel( "Quality Check found " + ok[0]
									+ " Error" + ( ok[0] == 1 ? "" : "s" ) );
							JOptionPane.showMessageDialog( rootPane,
									"There were errors during the load",
									"Committed with Errors", JOptionPane.WARNING_MESSAGE );
						}
						else {
							JOptionPane.showMessageDialog( rootPane, "Data Committed",
									"Success", JOptionPane.INFORMATION_MESSAGE );
						}
					}
				};

				OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
			}
		}

		private boolean askForLoad( IEngine engine ) {
			JPanel top = new JPanel( new BorderLayout() );
			JPanel questions = new JPanel();
			questions.setLayout( new BoxLayout( questions, BoxLayout.PAGE_AXIS ) );
			top.add( new JLabel( "Commit this data to "
					+ MetadataQuery.getEngineLabel( engine ) + "?" ),
					BorderLayout.NORTH );
			top.add( questions, BorderLayout.CENTER );

			JCheckBox jcbs[] = {
				new JCheckBox( "Replace Existing Data", doreplace ),
				new JCheckBox( "Compute Dependent Relationships", calcinfers ),
				new JCheckBox( "Create Metamodel", dometamodel ),
				new JCheckBox( "Check Quality", doconformance )
			};
			for ( JCheckBox b : jcbs ) {
				questions.add( b );
			}

			if ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( null, top,
					"Really commit?", JOptionPane.YES_NO_OPTION ) ) {
				LoadingPlaySheetFrame.this.calcinfers = jcbs[1].isSelected();
				LoadingPlaySheetFrame.this.dometamodel = jcbs[2].isSelected();
				LoadingPlaySheetFrame.this.doconformance = jcbs[3].isSelected();

				return true;
			}
			return false;
		}
	}

	private class ConformanceAction extends AbstractAction {

		public ConformanceAction() {
			super( "Check Quality", DbAction.getIcon( "conformance-check" ) );
			putValue( Action.SHORT_DESCRIPTION,
					"Check that nodes in this sheet are already defined" );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			boolean sel = timertoggle.isSelected();

			if ( sel ) {
				IEngine eng = ( null == getEngine()
						? LoadingPlaySheetFrame.this.askForEngine( "Check" ) : getEngine() );

				if ( null == eng ) {
					Utility.showMessage( "QA Check canceled" );
					timertoggle.setSelected( false );
				}

				ProgressTask pt = new ProgressTask( "Preparing QA Checks", new Runnable() {

					@Override
					public void run() {
						realtimer.clear();
						realtimer.loadCaches( eng );
					}

				} ) {
					@Override
					public void done() {
						super.done();
						if ( !sheets.isEmpty() ) {
							recheckConformance( eng );
						}
					}
				};
				OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
			}
			else {
				realtimer.clear();
				showerrs.setVisible( false );
				if ( showerrs.isSelected() ) {
					// stop filtering rows if we're not doing QA checks
					showerrs.doClick();
				}
				for ( LoadingPlaySheetBase b : sheets ) {
					b.getLoadingModel().setQaChecker( null );
				}
			}
		}

		private void recheckConformance( final IEngine eng ) {
			showerrs.setVisible( true );

			final String t = "Checking Conformance against "
					+ MetadataQuery.getEngineLabel( eng );
			int progressPerTab = 100 / sheets.size();
			boolean ok[] = { true };
			ProgressTask pt = new DisappearingProgressBarTask( t, new Runnable() {

				@Override
				public void run() {
					updateProgress( t, 0 );

					for ( LoadingPlaySheetBase node : sheets ) {
						String text = "Checking quality of: " + node.getTitle();
						addProgress( text, progressPerTab );
						log.debug( text );

						LoadingSheetModel lsm = node.getLoadingModel();
						lsm.setQaChecker( realtimer );

						CloseableTab ct = getTabComponent( node );

						boolean noerrs = !lsm.hasConformanceErrors();
						boolean nomods = !lsm.hasModelErrors();

						ok[0] = ( noerrs && nomods );
						ct.setMark( noerrs && nomods ? MarkType.NONE : MarkType.ERROR );
						ct.repaint();
					}
				}
			} ) {

				@Override
				public void done() {
					super.done();
					if ( !ok[0] ) {
						int errs = announceErrors();
						String msg = "Quality check found " + errs
								+ ( 1 == errs ? " error" : " errors" );
						setLabel( msg );
					}
				}
			};

			OperationsProgress.getInstance( PlayPane.UIPROGRESS ).add( pt );
		}
	}

	private class SaveAllAction extends AbstractSavingAction {

		public SaveAllAction() {
			super( "Save as Loading Sheets", DefaultIcons.defaultIcons.get( DefaultIcons.SAVE ),
					false, Preferences.userNodeForPackage( DBToLoadingSheetExporter.class ),
					"lastexp" );
			setToolTip( "Saves this data as a Loading Sheet" );

			setDefaultFileName( "Custom_Loading_Sheet_" );
			setAppendDate( true );
		}

		@Override
		protected void finishFileChooser( JFileChooser chsr ) {
			chsr.setAcceptAllFileFilterUsed( false );
			FileFilter xlsxFilter
					= new FileNameExtensionFilter( "Excel Workbook (*.xlsx)", "xlsx" );
			chsr.setFileFilter( xlsxFilter );
		}

		@Override
		public void saveTo( File file ) throws IOException {
			// if we only have sheets with errors, or sheets with no errors, there's
			// no need to ask the user what they want to export. if we have some of
			// both, we need to ask
			boolean hasgoods = false;
			boolean hasbads = false;
			for ( LoadingPlaySheetBase lsd : LoadingPlaySheetFrame.this.sheets ) {
				if ( lsd.getLoadingModel().hasConformanceErrors() ) {
					hasbads = true;
				}
				else {
					hasgoods = true;
				}
			}

			boolean dobads = true;
			boolean dogoods = true;
			if ( hasgoods && hasbads ) {
				dobads = false;
				dogoods = false;
				String opts[] = { "Everything", "Only Conforming Data", "Only Non-Conforming Data" };
				Object o = JOptionPane.showInputDialog( rootPane, "What would you like to save?",
						"What to Export", JOptionPane.QUESTION_MESSAGE, null, opts, opts[0] );
				if ( o == opts[0] || o == opts[1] ) {
					dogoods = true;
				}
				if ( o == opts[0] || o == opts[2] ) {
					dobads = true;
				}
			}

			ImportData data = ImportData.forEngine( getEngine() );
			fillImportData( data, dogoods, dobads );

			XlsWriter writer = new XlsWriter();
			writer.write( data, file );
		}

		private void fillImportData( ImportData tofill, boolean dogoods, boolean dobads ) {
			for ( LoadingPlaySheetBase lsd : LoadingPlaySheetFrame.this.sheets ) {
				String tabname = getTabTitle( lsd );

				LoadingSheetData newlsd;
				if ( lsd.getLoadingModel().isRel() ) {
					LoadingSheetData rlsd = lsd.getLoadingModel().toLoadingSheet( tabname );

					LoadingSheetData newrels = LoadingSheetData.copyHeadersOf( rlsd );
					newrels.setProperties( rlsd.getPropertiesAndDataTypes() );

					for ( LoadingNodeAndPropertyValues node : rlsd.getData() ) {
						if ( node.hasError() && dobads ) {
							newrels.add( node );
						}
						else if ( !node.hasError() && dogoods ) {
							newrels.add( node );
						}
					}

					tofill.add( newrels );
				}
				else {
					newlsd = lsd.getLoadingModel().toLoadingSheet( tabname );
					if ( dogoods ) {
						tofill.add( newlsd );
					}
				}
			}
		}

		@Override
		protected String getSuccessMessage( File file ) {
			return "Loading Sheets saved to " + file;
		}
	}

	private class AddAction extends AbstractAction {

		public AddAction() {
			super( "Add Loading Sheet", DbAction.getIcon( "addtab" ) );
			putValue( Action.SHORT_DESCRIPTION, "Add a new tab to this loading sheet" );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			AddTabPanel panel = new AddTabPanel();
			int ans = JOptionPane.showOptionDialog( rootPane, panel, "Tab Details",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					new String[]{ "Create", "Cancel" }, "Create" );
			if ( 0 == ans ) {
				LoadingSheetData lsd = panel.getSheet();
				LoadingPlaySheetBase base = ( lsd.isRel()
						? new RelationshipLoadingPlaySheet( lsd, true )
						: new NodeLoadingPlaySheet( lsd, true ) );
				addTab( base );
			}
		}
	}
}
