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
 *****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.api.IPlaySheet;
import gov.va.semoss.ui.main.listener.impl.EditPlaySheetTitleListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * The AbstractRDFPlaySheet class creates the structure of the basic components
 * of the Play Sheet, including the RDFEngine, the query, and the panel.
 */
public abstract class AbstractRDFPlaySheet extends JInternalFrame implements IPlaySheet {

	protected boolean overlay = false;
	private String query = null;
	private IEngine engine = null;
	protected Insight insight = null;
	protected JDesktopPane pane = null;
	private static final Logger logger = Logger.getLogger( AbstractRDFPlaySheet.class );
	protected JProgressBar jBar = new JProgressBar();
	protected Perspective perspectiveName = null;
	private static final Map<String, ImageIcon> defaultIcons = new HashMap<>();

	/**
	 * Constructor for AbstractRDFPlaySheet.
	 */
	public AbstractRDFPlaySheet() {
		super( "", true, true, true, true );

		UIDefaults nimbusOverrides = new UIDefaults();
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		defaults.put( "nimbusOrange", defaults.get( "nimbusInfoBlue" ) );
		Painter blue = (Painter) defaults.get( "Button[Default+Focused+Pressed].backgroundPainter" );
		nimbusOverrides.put( "ProgressBar[Enabled].foregroundPainter", blue );
		jBar.putClientProperty( "Nimbus.Overrides", nimbusOverrides );
		jBar.putClientProperty( "Nimbus.Overrides.InheritDefaults", false );
		jBar.setStringPainted( true );
	}

	/**
	 * Method run. Calls createView() and creates the first instance of a play
	 * sheet.
	 */
	@Override
	public void run() {
		createView();
	}

	@Override
	public Object getData() {
		Map<String, String> retHash = new HashMap<>();
		retHash.put( "id", this.insight == null ? "" : this.insight.getIdStr() );
		String className;
		Class<?> enclosingClass = getClass().getEnclosingClass();
		if ( enclosingClass != null ) {
			className = enclosingClass.getName();
		}
		else {
			className = getClass().getName();
		}
		logger.debug( className );
		retHash.put( "playsheet", className );

		return retHash;
	}

	public static void setDefaultIcons( Map<String, ImageIcon> icons ) {
		defaultIcons.clear();
		defaultIcons.putAll( icons );
	}

	public void setAppend( boolean overlay ) {
		this.overlay = overlay;
	}

	/**
	 * Gets the latest query set to the play sheet.
	 * <p>
	 * If multiple queries have been set to the specific play sheet through Extend
	 * or Overlay, the function will return the last query set to the play sheet.
	 *
	 * @see #extendView()
	 * @see #overlayView()
	 * @return the SPARQL query previously set to this play sheet
	 */
	@Override
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the String version of the SPARQL query on the play sheet.
	 * <p>
	 * The query must be set before creating the model for visualization. Thus,
	 * this function is called before createView(), extendView(),
	 * overlayView()--everything that requires the play sheet to pull data through
	 * a SPARQL query.
	 *
	 * @param query the full SPARQL query to be set on the play sheet
	 * @see	#createView()
	 * @see #extendView()
	 * @see #overlayView()
	 */
	@Override
	public void setQuery( String query ) {
		query = query.trim();
		logger.debug( "New Query " + query );
		this.query = query;
	}

	/**
	 * Sets the title of the play sheet. The title is displayed as the text on top
	 * of the internal frame that is the play sheet.
	 *
	 * @param title representative name for the play sheet. Often a concatenation
	 * of the question ID and question text
	 */
	@Override
	public void setTitle( String title ) {
		super.setTitle( title );
		this.title = title;
	}

	/**
	 * Gets the title of the play sheet. The title is displayed as the text on top
	 * of the internal frame that is the play sheet.
	 *
	 * @return String representative name for the play sheet. Often a
	 * concatenation of the question ID and question text
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the RDF engine for the play sheet to run its query against. Can be any
	 * of the active engines, all of which are stored in DIHelper
	 *
	 * @param engine the active engine for the play sheet to run its query
	 * against.
	 */
	@Override
	public void setEngine( IEngine engine ) {
		logger.info( "Set the engine " );
		this.engine = engine;
	}

	/**
	 * Gets the RDF engine for the play sheet to run its query against. Can be any
	 * of the active engines, all of which are stored in DIHelper
	 *
	 *
	 * @return IEngine
	 */
	@Override
	public IEngine getEngine() {
		return this.engine;
	}

	/**
	 * This property enables the "ProcessQueryListener" to set the Perspective
	 * Name.
	 *
	 * @param perValue -- (Perspective) the currently selected Perspective.
	 */
	@Override
	public void setPerspective( Perspective perValue ) {
		this.perspectiveName = perValue;
	}

	@Override
	public void setInsight( Insight i ) {
		insight = i;
		String output = i.getOutput();
		ImageIcon icon = defaultIcons.get( output );
		if ( null != icon ) {
			this.setFrameIcon( icon );
		}
	}

	@Override
	public Insight getInsight() {
		return insight;
	}

	public Perspective getPerspective() {
		return perspectiveName;
	}

	/**
	 * Sets the JDesktopPane to display the play sheet on.
	 * <p>
	 * This must be set before calling functions like {@link #createView()} or
	 * {@link #extendView()}, as functions like these add the panel to the desktop
	 * pane set in this function.
	 *
	 * @param pane the desktop pane that the play sheet is to be displayed on
	 */
	@Override
	public void setJDesktopPane( JDesktopPane pane ) {
		this.pane = pane;
	}

	/**
	 * Updates the progress bar to display a specified status and progress value.
	 *
	 * @param status The text to be displayed on the progress bar.
	 * @param x The value of the progress bar.
	 */
	public void updateProgressBar( String status, int x ) {
		jBar.setString( status );
		jBar.setValue( x );
		jBar.setVisible( true );
	}

	protected void progressComplete( String status ) {
		updateProgressBar( status, 100 );
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

	protected void setWindow() {
		setResizable( true );
		setClosable( true );
		setMaximizable( true );
		setIconifiable( true );
		JPopupMenu popup = this.getComponentPopupMenu();
		JMenuItem editTitle = new JMenuItem( "Edit Title" );
		EditPlaySheetTitleListener mylistener = new EditPlaySheetTitleListener();
		mylistener.setPlaySheet( this );
		editTitle.addActionListener( mylistener );
		if ( null != popup ) {
			popup.add( editTitle, 0 );
		}
	}
}
