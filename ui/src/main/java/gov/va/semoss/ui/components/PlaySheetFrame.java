/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import gov.va.semoss.om.Insight;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.impl.ListOfValueArraysQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.ModelQueryAdapter;
import gov.va.semoss.ui.actions.DbAction;
import gov.va.semoss.ui.components.playsheets.PlaySheetCentralComponent;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.DefaultPlaySheetIcons;

import gov.va.semoss.util.GuiUtility;
import gov.va.semoss.util.PlaySheetEnum;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.Painter;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.apache.log4j.Logger;
import org.openrdf.model.Model;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class PlaySheetFrame extends JInternalFrame {

	public static final String FONT_UP = "fontup";
	public static final String FONT_DOWN = "fontdown";
	public static final String SAVE = "save";
	public static final String SAVE_AS = "saveas";
	public static final String SAVE_ALL = "saveall";
	public static final String EXPORT = "export";
	private static final String SAVE_MNEMONIC = "* ";

	private static final long serialVersionUID = 7908827976216133994L;
	private static final Logger log = Logger.getLogger( PlaySheetFrame.class );
	private final Action fontup = new FontSizeAction( 1 );
	private final Action fontdown = new FontSizeAction( -1 );

	private final HideableTabbedPane tabs = new HideableTabbedPane();
	protected final JProgressBar jBar = new JProgressBar();
	private IEngine engine = null;

	public PlaySheetFrame( IEngine eng ) {
		this( eng, "" );
	}

	public PlaySheetFrame( IEngine eng, String title ) {
		super( title, true, true, true, true );

		UIDefaults nimbusOverrides = new UIDefaults();
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		defaults.put( "nimbusOrange", defaults.get( "nimbusInfoBlue" ) );
		Painter<?> blue = (Painter<?>) defaults.get( "Button[Default+Focused+Pressed].backgroundPainter" );
		nimbusOverrides.put( "ProgressBar[Enabled].foregroundPainter", blue );
		jBar.putClientProperty( "Nimbus.Overrides", nimbusOverrides );
		jBar.putClientProperty( "Nimbus.Overrides.InheritDefaults", false );
		jBar.setStringPainted( true );

		tabs.setTabPlacement( JTabbedPane.BOTTOM );

		setLayout( new BorderLayout() );
		add( tabs, BorderLayout.CENTER );
		add( jBar, BorderLayout.SOUTH );

		engine = eng;

		tabs.addContainerListener( new ContainerListener() {

			@Override
			public void componentAdded( ContainerEvent e ) {
			}

			@Override
			public void componentRemoved( ContainerEvent e ) {
				if ( 0 == tabs.getTabCount() ) {
					dispose();
				}
			}
		} );

		addInternalFrameListener( new InternalFrameListener() {

			@Override
			public void internalFrameOpened( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameClosing( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameClosed( InternalFrameEvent ife ) {
				onFrameClose();
			}

			@Override
			public void internalFrameIconified( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameDeiconified( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameActivated( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameDeactivated( InternalFrameEvent ife ) {
			}
		} );
	}

	/**
	 * Gets called immediately before this frame is closed
	 */
	protected void onFrameClose() {
		PlaySheetCentralComponent pscc = getActivePlaySheet();
		if ( null != pscc ) {
			DIHelper.getInstance().getPlayPane().getFilterPanel().useBlankModels();

			if ( pscc.hasChanges() ) {
				GuiUtility.resetJTable( Constants.LABEL_TABLE );
				GuiUtility.resetJTable( Constants.TOOLTIP_TABLE );
				GuiUtility.resetJTable( Constants.COLOR_SHAPE_TABLE );

				Map<String, Action> actions = pscc.getActions();
				Map<String, Action> optactions = new LinkedHashMap<>();

				if ( actions.containsKey( SAVE ) ) {
					optactions.put( "Save", actions.get( SAVE ) );
				}

				if ( actions.containsKey( SAVE_ALL ) ) {
					optactions.put( "Save All", actions.get( SAVE_ALL ) );
				}

				if ( optactions.isEmpty() ) {
					return;
				}

				optactions.put( "Discard", null );

				int dtype = ( 2 == optactions.size() ? JOptionPane.YES_NO_OPTION
						: JOptionPane.YES_NO_CANCEL_OPTION );
				String[] options = optactions.keySet().toArray( new String[0] );

				int ret = JOptionPane.showOptionDialog( this, "Playsheet \"" + pscc.getTitle()
						+ "\" has unsaved data. Save it? ", "Save Unsaved Data?", dtype,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0] );

				Action a = optactions.get( options[ret] );

				if ( null != a ) {
					a.actionPerformed( null );
				}
			}
		}
	}

	public void setEngine( IEngine engine ) {
		this.engine = engine;
	}

	public IEngine getEngine() {
		return DIHelper.getInstance().getRdfEngine();
	}

	public void addTab( PlaySheetCentralComponent c ) {
		addTab( c.getTitle(), c );
	}

	public void addTab( String title, PlaySheetCentralComponent c ) {
		c.setFrame( this );
		tabs.add( title, c );

		if ( 1 == tabs.getTabCount() ) {
			ImageIcon icon = DefaultPlaySheetIcons.getDefaultIcon( c.getClass() );
			if ( null != icon ) {
				setFrameIcon( icon );
			}
		}
	}

	public void addChangeListener( ChangeListener cl ) {
		tabs.addChangeListener( cl );
	}

	public void closeTab( PlaySheetCentralComponent c ) {
		tabs.remove( c );
	}

	public PlaySheetCentralComponent getActivePlaySheet() {
		if ( tabs.getTabCount() > 0 ) {
			return PlaySheetCentralComponent.class.cast( tabs.getSelectedComponent() );
		}

		return null;
	}

	public Collection<PlaySheetCentralComponent> getPlaySheets() {
		List<PlaySheetCentralComponent> cmps = new ArrayList<>();
		for ( int i = 0; i < tabs.getTabCount(); i++ ) {
			cmps.add( PlaySheetCentralComponent.class.cast( tabs.getComponentAt( i ) ) );
		}

		return cmps;
	}

	protected void selectTab( PlaySheetCentralComponent c ) {
		tabs.setSelectedComponent( c );
	}

	protected void selectTab( int idx ) {
		tabs.setSelectedComponent( tabs.getComponentAt( idx ) );
	}

	/**
	 * Notifies this class that it has been added to a desktop. Default does
	 * nothing
	 *
	 * @param pane the desktop it was added to
	 */
	public void addedToDesktop( JDesktopPane pane ) {
	}

	public void hideProgress() {
		jBar.setVisible( false );
	}

	public void updateProgress( String txt, int val ) {
		jBar.setString( txt );
		jBar.setValue( val );
		if ( !jBar.isVisible() ) {
			jBar.setVisible( true );
		}
	}

	public void addProgress( String txt, int val ) {
		updateProgress( txt, jBar.getValue() + val );
	}

	public void showSaveMnemonic( boolean show ) {
		String current = getTitle();
		if ( show ) {
			setTitle( SAVE_MNEMONIC + current );
		}
		else {
			setTitle( current.replaceAll( "^\\" + SAVE_MNEMONIC, "" ) );
		}
	}

	public ProgressTask getCreateTask( Insight insight, IEngine engine ) {
		PlaySheetEnum pse = PlaySheetEnum.valueFor( insight );
		PlaySheetCentralComponent cmp
				= PlaySheetCentralComponent.class.cast( pse.getSheetInstance() );
		cmp.setTitle( insight.getLabel() );

		String query = insight.getSparql();		

		updateProgress( "Preparing Query", 10 );
		final ListQueryAdapter<Value[]> lqa
				= new ListOfValueArraysQueryAdapter( query );
		final StringBuilder builder = new StringBuilder();
		final int rows[] = { 0 };

		ProgressTask pt = new DisappearingProgressBarTask( cmp, new Runnable() {

			@Override
			public void run() {
				try {
					updateProgress( "Executing Query", 40 );

					int dsize = 0;
					if ( null == query || "NULL".equals( query.toUpperCase() ) ) {
						// uh oh...no sparql given
						// assume the pscc knows what to do with empty data
						dsize = 0;
						cmp.create( null, null, engine );
						// we have no way of determining what data got produced,
						// so assume something good happened
						dsize = 1;
					}
					else if ( lqa.getSparql().toUpperCase().startsWith( "CONSTRUCT" )
							|| lqa.getSparql().toUpperCase().startsWith( "DESCRIBE" ) ) {
						updateProgress( "Preparing Display", 80 );
						Model model = engine.construct( new ModelQueryAdapter( lqa.getSparql() ) );
						cmp.create( model, engine );
						dsize = model.size();
					}
					else {
						List<Value[]> data = engine.query( lqa );
						updateProgress( "Preparing Display", 80 );
						cmp.create( data, lqa.getBindingNames(), engine );
						dsize = data.size();
					}

					updateProgress( "Execution Complete", 90 );

					builder.append( cmp.getTitle() ).append( " " ).
							append( dsize ).append( " Data Row" ).
							append( 1 == dsize ? "" : "s" ).append( " Fetched" );
					rows[0] = dsize;

				}
				catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
					log.error( e, e );
					GuiUtility.showError( e.getLocalizedMessage() );
				}
			}
		} ) {
			@Override
			public void done() {
				setLabel( builder.toString() );
				setFinishLabel( getLabel() );

				if ( 0 == rows[0] ) {
					// let the user know we're done, but no data was returned
					JOptionPane.showMessageDialog( rootPane, "No data returned", "No Data",
							JOptionPane.INFORMATION_MESSAGE );
				}

				super.done();
			}
		};

		return pt;
	}

	public ProgressTask getCreateTask( final String query ) {
		final PlaySheetCentralComponent cmp = getActivePlaySheet();
		//String q = query.replaceAll( "(?i)^CONSTRUCT[\\s]*\\{([^\\.]+)[\\.][\\s]*\\}", "SELECT $1" );		
		String q = query;

		updateProgress( "Preparing Query", 10 );
		final ListQueryAdapter<Value[]> lqa
				= new ListOfValueArraysQueryAdapter( q );
		final StringBuilder builder = new StringBuilder();
		final int rows[] = { 0 };

		ProgressTask pt = new DisappearingProgressBarTask( cmp, new Runnable() {

			@Override
			public void run() {
				try {
					updateProgress( "Executing Query", 40 );

					int dsize = 0;
					if ( null == query || "NULL".equals( query.toUpperCase() ) ) {
						// uh oh...no sparql given
						// assume the pscc knows what to do with empty data
						dsize = 0;
						cmp.create( null, null, getEngine() );
						// we have no way of determining what data got produced,
						// so assume something good happened
						dsize = 1;
					}
					else if ( lqa.getSparql().toUpperCase().startsWith( "CONSTRUCT" )
							|| lqa.getSparql().toUpperCase().startsWith( "DESCRIBE" ) ) {
						updateProgress( "Preparing Display", 80 );
						Model model = engine.construct( new ModelQueryAdapter( lqa.getSparql() ) );
						cmp.create( model, engine );
						dsize = model.size();
					}
					else {
						List<Value[]> data = engine.query( lqa );
						updateProgress( "Preparing Display", 80 );
						cmp.create( data, lqa.getBindingNames(), getEngine() );
						dsize = data.size();
					}

					updateProgress( "Execution Complete", 90 );

					builder.append( cmp.getTitle() ).append( " " ).
							append( dsize ).append( " Data Row" ).
							append( 1 == dsize ? "" : "s" ).append( " Fetched" );
					rows[0] = dsize;

				}
				catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
					log.error( e, e );
					GuiUtility.showError( e.getLocalizedMessage() );
				}
			}
		} ) {
			@Override
			public void done() {
				setLabel( builder.toString() );
				setFinishLabel( getLabel() );

				if ( 0 == rows[0] ) {
					// let the user know we're done, but no data was returned
					JOptionPane.showMessageDialog( rootPane, "No data returned", "No Data",
							JOptionPane.INFORMATION_MESSAGE );
				}

				super.done();
			}
		};

		return pt;
	}

	public ProgressTask getOverlayTask( String query, IEngine engine,
			String tabTitleIfNeeded ) {
		String q = query;
		final PlaySheetCentralComponent overlayee = getActivePlaySheet();
		final ListQueryAdapter<Value[]> lqa
				= new ListOfValueArraysQueryAdapter( q );

		ProgressTask pt = new DisappearingProgressBarTask( overlayee, new Runnable() {

			@Override
			public void run() {
				try {

					updateProgress( "Executing Query", 40 );

					if ( lqa.getSparql().toUpperCase().startsWith( "CONSTRUCT" )
							|| lqa.getSparql().toUpperCase().startsWith( "DESCRIBE" ) ) {
						Model model = engine.construct( new ModelQueryAdapter( lqa.getSparql() ) );
						updateProgress( "Preparing Display", 80 );

						if ( overlayee.canAcceptModelData() ) {
							overlayee.overlay( model, engine );
						}
						else {
							try {
								PlaySheetCentralComponent pscc = overlayee.getClass().newInstance();
								pscc.setTitle( tabTitleIfNeeded );
								PlaySheetFrame.this.addTab( tabTitleIfNeeded, pscc );
								pscc.create( model, engine );
							}
							catch ( InstantiationException | IllegalAccessException e ) {
								log.error( e, e );
							}
						}
					}
					else {
						List<Value[]> data = engine.query( lqa );
						List<String> headers = lqa.getBindingNames();

						updateProgress( "Preparing Display", 80 );
						if ( overlayee.canAcceptDataWithHeaders( headers ) ) {
							overlayee.overlay( data, headers, engine );
						}
						else {
							try {
								PlaySheetCentralComponent pscc = overlayee.getClass().newInstance();
								pscc.setTitle( tabTitleIfNeeded );
								PlaySheetFrame.this.addTab( tabTitleIfNeeded, pscc );
								pscc.create( data, headers, engine );
							}
							catch ( InstantiationException | IllegalAccessException e ) {
								log.error( e, e );
							}
						}
					}
					updateProgress( "Execution Complete", 90 );
				}
				catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
					log.error( e, e );
				}
			}
		} );

		return pt;
	}

	public void populateToolbar( JToolBar tb ) {
		PlaySheetCentralComponent pscc
				= PlaySheetCentralComponent.class.cast( tabs.getSelectedComponent() );

		if ( null != pscc ) {
			String tabTitle = pscc.getTitle();
			pscc.populateToolBar( tb, tabTitle );

			tb.add( fontup );
			tb.add( fontdown );
		}
	}

	/**
	 * Gets a String-to-Action mapping for this instance and it's currently-active
	 * tab. The keys of the map are class-specific
	 *
	 * @return a mapping
	 */
	public Map<String, Action> getActions() {
		Map<String, Action> map = new HashMap<>();
		map.put( FONT_UP, fontup );
		map.put( FONT_DOWN, fontdown );

		if ( null != getActivePlaySheet() ) {
			map.putAll( getActivePlaySheet().getActions() );
		}

		return map;
	}

	private void changeFont( float incr ) {
		for ( PlaySheetCentralComponent pscc : getPlaySheets() ) {
			pscc.incrementFont( incr );
		}
	}

	protected String getTabTitle( PlaySheetCentralComponent pscc ) {
		int idx = tabs.indexOfComponent( pscc );
		return ( idx < 0 ? null : tabs.getTitleAt( idx ) );
	}

	protected CloseableTab getTabComponent( PlaySheetCentralComponent pscc ) {
		int idx = tabs.indexOfComponent( pscc );
		return ( idx < 0 ? null : tabs.getTabComponentAt( idx ) );
	}

	protected class DisappearingProgressBarTask extends ProgressTask {

		private String finishLabel = "Processing Complete";

		public DisappearingProgressBarTask( PlaySheetCentralComponent ps, Runnable op ) {
			this( ps.getTitle(), op );
		}

		public DisappearingProgressBarTask( String title, Runnable op ) {
			super( title, op );
		}

		@Override
		public void done() {
			super.done();
			jBar.setString( finishLabel );
			jBar.setValue( 100 );
			ActionListener al = new ActionListener() {

				@Override
				public void actionPerformed( ActionEvent ae ) {
					jBar.setVisible( false );
				}
			};

			Timer t = new Timer( 5000, al );
			t.setRepeats( false );
			t.start();
		}

		public void setFinishLabel( String s ) {
			finishLabel = s;
		}
	}

	private class FontSizeAction extends AbstractAction {

		private static final long serialVersionUID = 6101781697550580402L;
		private final float stepSize;

		public FontSizeAction( float sizer ) {
			super( ( sizer > 0 ? "Increase" : "Decrease" ) + " Font Size" );
			stepSize = sizer;

			String imgname = ( sizer > 0 ? "fontup" : "fontdown" );
			Icon img = DbAction.getIcon( imgname );
			putValue( Action.SMALL_ICON, img );
			putValue( Action.SHORT_DESCRIPTION,
					( sizer > 0 ? "Increase" : "Decrease" ) + " Font Size" );
		}

		@Override
		public void actionPerformed( ActionEvent e ) {
			PlaySheetFrame.this.changeFont( stepSize );
		}
	}
}
