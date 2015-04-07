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
 * ****************************************************************************
 */
package gov.va.semoss.ui.components.playsheets;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.main.listener.impl.BrowserZoomListener;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserFactory;

/**
 * The BrowserPlaySheet creates an instance of a browser to utilize the D3
 * Javascript library to create visualizations.
 */
public class BrowserPlaySheet2 extends PlaySheetCentralComponent {
	private static final long serialVersionUID = 1142415334918968440L;
	private static final Logger log = Logger.getLogger( BrowserPlaySheet2.class );
	
	public static final String DATASERIES = "dataSeries";

	private Boolean empty = false;
	protected Browser browser;
	private final String fileName;
	private final Map<String, Object> dataHash = new HashMap<>();

	public BrowserPlaySheet2( String fnamepiece ) {
		browser = BrowserFactory.create();

		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.add( browser.getView().getComponent(), BorderLayout.CENTER );

		setLayout( new BorderLayout() );
		add( panel );

		String workingDir = DIHelper.getInstance().getProperty( Constants.BASE_FOLDER );
		fileName = "file://" + workingDir + fnamepiece;
	}

	@Override
	public void setFrame( PlaySheetFrame psf ) {
		super.setFrame( psf );
		psf.addInternalFrameListener( new InternalFrameListener() {

			@Override
			public void internalFrameOpened( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameClosing( InternalFrameEvent ife ) {
			}

			@Override
			public void internalFrameClosed( InternalFrameEvent ife ) {
				browser.dispose();
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

	public void addDataHash( Map<String, Object> newdata ) {
		dataHash.putAll( newdata );
	}

	/**
	 * Method callIt. Converts a given Hashtable to a Json and passes it to the
	 * browser.
	 *
	 * @param table Hashtable - the correctly formatted data from the SPARQL query
	 * results.
	 */
	public void callIt( Map<String, Object> table ) {
		String json = new Gson().toJson( table ); 
		log.debug( "Converted " + json );
		
		browser.executeJavaScript( "start('" + json + "');" );
	}
	
	@Override
	public void incrementFont( float incr ) {
		browser.executeJavaScript( "resizeFont(" + incr + ");" );
	}
	
	/**
	 * This is the function that is used to create the first view of any play
	 * sheet. It often uses a lot of the variables previously set on the play
	 * sheet, such as {@link #setQuery(String)},
	 * {@link #setJDesktopPane(JDesktopPane)}, {@link #setEngine(IEngine)}, and
	 * {@link #setTitle(String)} so that the play sheet is displayed correctly
	 * when the view is first created. It generally creates the model for
	 * visualization from the specified engine, then creates the visualization,
	 * and finally displays it on the specified desktop pane
	 *
	 * This is the function called by the PlaysheetCreateRunner.
	 * PlaysheetCreateRunner is the runner used whenever a play sheet is to first
	 * be created, most notably in ProcessQueryListener.
	 */
	@Override
	public void createView() {

		browser.getView().getComponent().addKeyListener( new BrowserZoomListener( browser ) );
		browser.loadURL( fileName );
		while ( browser.isLoading() ) {
			try {
				TimeUnit.MILLISECONDS.sleep( 50 );
			}
			catch ( InterruptedException e ) {
				// TODO Auto-generated catch block
				log.error( e, e );
			}
		}
		
		refreshView();
	}

	/**
	 * Method refreshView. Refreshes the view and re-populates the play sheet.
	 */
	public void refreshView() {
		empty = false;
		if ( dataHash.get( DATASERIES ) instanceof HashSet ) {
			HashSet<Object> dataSeries = (HashSet<Object>) dataHash.get( DATASERIES );
			if ( dataSeries == null || dataSeries.isEmpty() ) {
				empty = true;
				return;
			}
		}
		callIt( dataHash );
	}

	/**
	 * Method createCustomView.
	 *
	 * @see createView()
	 */
	public void createCustomView() {
		super.createView();
	}

	/**
	 * Method processQueryData. Processes the data from the SPARQL query into an
	 * appropriate format for the specific play sheet.
	 *
	 * @return Hashtable - the data from the SPARQL query results, formatted
	 * accordingly.
	 */
	public Map<String, Object> processQueryData() {
		return new HashMap<>();
	}

	/**
	 * Method isEmpty.
	 *
	 * @return Boolean
	 */
	public Boolean isEmpty() {
		return empty;
	}

	/**
	 * Method getBrowser. Gets the current browser.
	 *
	 * @return Browser - the current browser.
	 */
	public Browser getBrowser() {
		return this.browser;
	}

	@Override
	public Object getData() {
		Map<String, Object> returnHash = (Map<String, Object>) super.getData();
		returnHash.put( "specificData", dataHash );
		return returnHash;
	}

	@Override
	public void run() {
	}
}
