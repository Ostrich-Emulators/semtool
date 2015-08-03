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
import gov.va.semoss.util.ExportUtility;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

import javax.imageio.ImageIO;
import javax.swing.JDesktopPane;

import netscape.javascript.JSObject;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;

import com.google.gson.Gson;

/**
 * The BrowserPlaySheet creates an instance of a browser to utilize the D3
 * Javascript library to create visualizations.
 */
public class BrowserPlaySheet2 extends ImageExportingPlaySheet {

	private static final long serialVersionUID = 1142415334918968440L;
	private static final Logger log = Logger.getLogger( BrowserPlaySheet2.class );

	private final Map<String, Object> dataHash = new HashMap<>();
	private final String fileName;

	protected final JFXPanel jfxPanel = new JFXPanel();
	protected WebEngine engine;
	protected Scene scene;

	public BrowserPlaySheet2( String htmlPath ) {
		setLayout( new BorderLayout() );
		add( jfxPanel );

		fileName = "file:///"
				+ DIHelper.getInstance().getProperty( Constants.BASE_FOLDER ) + htmlPath;

		Platform.setImplicitExit( false );
		Platform.runLater( new Runnable() {
			@Override
			public void run() {
				WebView view = new WebView();
				scene = new Scene( view );
				jfxPanel.setScene( scene );

				engine = view.getEngine();
				engine.setOnAlert(
						new EventHandler<WebEvent<String>>() {
							@Override
							public void handle( WebEvent<String> event ) {
								log.debug( "handling event: " + event );
								if ( "document:loaded".equals( event.getData() ) ) {
									log.debug( "Document is loaded." );
									callIt();
								}
								else if ( event.getData().startsWith( "download:csv" ) ) {
									log.debug( "Downloading CSV file from browser." );
									downloadCSV( event.getData().substring( 12 ) );
								}
							}

						}
				);
			}
		} );
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
		Platform.runLater( new Runnable() {
			@Override
			public void run() {
				engine.load( fileName );
			}
		} );
	}

	/**
	 * Method callIt. Converts a given Hashtable to a Json and passes it to the
	 * browser.
	 *
	 * @param table Hashtable - the correctly formatted data from the SPARQL query
	 * results.
	 */
	public void callIt() {
		// Initialize the key used to store the Data Series being visualized
		String dataSeriesKey = "dataSeries";
		// Get the data series
		HashMap<?,?> dataSeries = (HashMap<?,?>)dataHash.get(dataSeriesKey);
		// If this graphic is a column chart, then sort the data series categories
		if (this.fileName.endsWith("columnchart.html")){
			// Call the sorting convenience method, to derive an order data structure
			LinkedHashMap<?,?> sortedHash = sort(dataSeries);
			// Put the data series back
			dataHash.put(dataSeriesKey, sortedHash);
		}
		// Otherwise, use the unsorted data series
		else {
			// Put the data series back
			dataHash.put(dataSeriesKey, dataSeries);
		}
		// Continue with the processing
		String json = new Gson().toJson( dataHash );
		if ( null != json && !"".equals( json ) && !"{}".equals( json ) ) {
			json = json.replace( "\\n", " " );
			json = "'" + json + "'";
		}

		executeJavaScript( "start(" + json + ");" );
	}

	@Override
	public void incrementFont( float incr ) {
		executeJavaScript( "resizeFont(" + incr + ");" );
	}

	public void executeJavaScript( String functionNameAndArgs ) {
		log.debug( "Calling javascript method: " + functionNameAndArgs );

		Platform.runLater( new Runnable() {
			@Override
			public void run() {
				engine.executeScript( functionNameAndArgs );
			}
		} );
	}

	public void registerFunction( String namespace, Object theClass ) {
		log.debug( "Registering Java class whose methods can be called from javascript. Namespace: "
				+ namespace + ", Java class: " + theClass );

		Platform.runLater( new Runnable() {
			@Override
			public void run() {
				engine.getLoadWorker().stateProperty().addListener(
						new ChangeListener<State>() {
							@Override
							public void changed( ObservableValue<? extends Worker.State> ov,
									State oldState, State newState ) {
								if ( newState == Worker.State.SUCCEEDED ) {
									JSObject jsobj = (JSObject) engine.executeScript( "window" );
									jsobj.setMember( namespace, theClass );
								}
							}
						}
				);
			}
		} );
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

	@SuppressWarnings( "unchecked" )
	@Override
	public Object getData() {
		Map<String, Object> returnHash = (Map<String, Object>) super.getData();
		returnHash.put( "specificData", dataHash );
		return returnHash;
	}

	@Override
	public void run() {
	}

	private void downloadCSV( String data ) {
		ExportUtility.doExportCSVWithDialogue( this, data );
	}

	@Override
	protected BufferedImage getExportImage() throws IOException {
		BufferedImage bufferedImage = new BufferedImage( getWidth(), getHeight(),
				BufferedImage.TYPE_INT_ARGB );
		paint( bufferedImage.getGraphics() );
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write( bufferedImage, Constants.PNG, baos );
			return ImageIO.read( new ByteArrayInputStream( baos.toByteArray() ) );
		}
	}
	
	protected BufferedImage getExportImageFromSVGBlock() throws IOException {
		log.debug("Using SVG block to save image.");
		DOMReader rdr = new DOMReader();
		Document doc = rdr.read( engine.getDocument() );
		Document svgdoc = null;
		try {
			Map<String, String> namespaceUris = new HashMap<>();
			namespaceUris.put( "svg", "http://www.w3.org/2000/svg" );
			namespaceUris.put( "xhtml", "http://www.w3.org/1999/xhtml" );

			XPath xp = DocumentHelper.createXPath( "//svg:svg" );
			xp.setNamespaceURIs( namespaceUris );

			// don't forget about the styles
			XPath stylexp = DocumentHelper.createXPath( "//xhtml:style" );
			stylexp.setNamespaceURIs( namespaceUris );

			svgdoc = DocumentHelper.createDocument();
			
			Element svg = null;
			List<?> theSVGElements = xp.selectNodes( doc );
			if (theSVGElements.size() == 1) {
				svg = Element.class.cast( theSVGElements.get(0) ).createCopy();
			} else {
				int currentTop = 0;
				int biggestSize = 0;
				for (int i=0; i<theSVGElements.size(); i++) {
					Element thisElement = Element.class.cast( theSVGElements.get(i) ).createCopy();
					int thisSize = thisElement.asXML().length();
					if ( thisSize > biggestSize) {
						currentTop = i;
						biggestSize = thisSize;
					}
				}
				svg = Element.class.cast( theSVGElements.get(currentTop) ).createCopy();
			}

			svgdoc.setRootElement( svg );
			
			Element oldstyle = Element.class.cast( stylexp.selectSingleNode( doc ) );
			if ( null != oldstyle ) {
				Element defs = svg.addElement( "defs" );
				Element style = defs.addElement( "style" );
				style.addAttribute( "type", "text/css" );
				String styledata = oldstyle.getTextTrim();
				style.addCDATA( styledata );

				// put the stylesheet definitions first
				List l = svg.elements();
				l.remove( defs );
				l.add( 0, defs );
			}

			TranscoderInput inputSvg = new TranscoderInput( new DOMWriter().write( svgdoc ) );
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			TranscoderOutput outputPng = new TranscoderOutput( baos );
			Transcoder transcoder = new PNGTranscoder();
			// transcoder.addTranscodingHint( PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE );

			
			if( log.isDebugEnabled() ){
				File errsvg = new File( FileUtils.getTempDirectory(), "graphvisualization.svg" );
				FileUtils.write( errsvg, svgdoc.asXML() );
			}
			
			transcoder.transcode( inputSvg, outputPng );
			baos.flush();
			baos.close();

			return ImageIO.read( new ByteArrayInputStream( baos.toByteArray() ) );
		}
		catch ( InvalidXPathException | DocumentException | TranscoderException e ) {
			String msg = "Problem creating image";
			if ( null != svgdoc ) {
				try {
					File errsvg = new File( FileUtils.getTempDirectory(), "graphvisualization.svg" );
					FileUtils.write( errsvg, svgdoc.asXML() );
					msg = "Could not create the image. SVG data store here: "
							+ errsvg.getAbsolutePath();
				}
				catch ( IOException ex ) {
					// don't care
				}
			}
			throw new IOException( msg, e );
		}
	}
	
	/**
	 * Convenience method for sorting (insertion order-based) data in a 
	 * HashMap according to alpha-numeric order
	 * @param hashMap The unordered table
	 * @return An ordered lookup table
	 */
	private LinkedHashMap<?,?> sort(HashMap<?,?> hashMap){
		SortedSet keys = new TreeSet(hashMap.keySet());
		LinkedHashMap sortedHash = new LinkedHashMap();
		Iterator keyIterator = keys.iterator();
		while (keyIterator.hasNext()){
			String key = (String)keyIterator.next();
			Object value = hashMap.get(key);
			sortedHash.put(key, value);
		}
		return sortedHash;
	}

}
