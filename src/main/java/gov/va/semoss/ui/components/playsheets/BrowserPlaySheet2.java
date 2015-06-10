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
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.JDesktopPane;

import netscape.javascript.JSObject;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * The BrowserPlaySheet creates an instance of a browser to utilize the D3
 * Javascript library to create visualizations.
 */
public class BrowserPlaySheet2 extends PlaySheetCentralComponent {
	private static final long serialVersionUID = 1142415334918968440L;
	private static final Logger log = Logger.getLogger( BrowserPlaySheet2.class );
	
	private final Map<String, Object> dataHash = new HashMap<>();
	private final String fileName;
	
	protected final JFXPanel jfxPanel = new JFXPanel();
	protected WebEngine engine;

	public BrowserPlaySheet2( String htmlPath ) {
		setLayout( new BorderLayout() );
		add(jfxPanel);

		fileName = "file:///" +
				DIHelper.getInstance().getProperty( Constants.BASE_FOLDER ) + htmlPath;
		
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				WebView view = new WebView();
				jfxPanel.setScene(new Scene(view));
				
				engine = view.getEngine();
				engine.setOnAlert(
					new EventHandler<WebEvent<String>>() {
						public void handle(WebEvent<String> event) {
							log.debug("handling event: " + event);
							if ("document:loaded".equals(event.getData())) {
								log.debug("Document is loaded.");
				                callIt();
							}
						}
					}
				);
			}
		});
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
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				engine.load(fileName);
			}
		});
	}
			
	
	/**
	 * Method callIt. Converts a given Hashtable to a Json and passes it to the
	 * browser.
	 *
	 * @param table Hashtable - the correctly formatted data from the SPARQL query
	 * results.
	 */
	public void callIt() {
		String json = new Gson().toJson( dataHash );
		if (null != json && !"".equals(json) && !"{}".equals(json)) {
			json = json.replace("\\n", " ");
			json = "'" + json + "'";
		}
		
		executeJavaScript("start(" + json + ");");
	}
	
	@Override
	public void incrementFont( float incr ) {
		executeJavaScript("resizeFont(" + incr + ");");
	}
	
	public void executeJavaScript(String functionNameAndArgs) {
		log.debug( "Calling javascript method: " + functionNameAndArgs );
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				engine.executeScript( functionNameAndArgs );
			}
		});
	}
	
	public void registerFunction(String namespace, Object theClass) {
		log.debug( "Registering Java class whose methods can be called from javascript. Namespace: " + namespace + ", Java class: " + theClass );
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				engine.getLoadWorker().stateProperty().addListener(
				    new ChangeListener<State>() {
				        public void changed(ObservableValue<? extends Worker.State> ov, State oldState, State newState) {
				            if (newState == Worker.State.SUCCEEDED) {
								JSObject jsobj = (JSObject) engine.executeScript("window");
								jsobj.setMember(namespace, theClass);
				            }
				        }
				    }
			    );
			}
		});
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

	public void addDataHash( Map<String, Object> newdata ) {
		dataHash.putAll( newdata );
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getData() {
		Map<String, Object> returnHash = (Map<String, Object>) super.getData();
		returnHash.put( "specificData", dataHash );
		return returnHash;
	}

	@Override
	public void run() {}
}
