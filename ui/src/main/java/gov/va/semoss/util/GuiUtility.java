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
package gov.va.semoss.util;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.ui.components.PlaySheetFrame;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * The GuiUtility class contains a variety of miscellaneous functions
 * implemented extensively throughout SEMOSS. Some of these functionalities
 * include getting concept names, printing messages, loading engines, and
 * writing Excel workbooks.
 */
public class GuiUtility {

	private static final Logger log = Logger.getLogger( GuiUtility.class );
	private static int id = 0;

	/**
	 * Splits up a string URI into tokens based on "/" character, and uses logic
	 * to return the instance name. If the input string is not a URI, then it is
	 * returned unmodified.
	 *
	 * @param uri -- (String) to be split into tokens.
	 *
	 * @return getInstanceName -- (String) Described above.
	 */
	public static String getInstanceName( String uri ) {
		try {
			//If the string is really a URI, then return its right end:
			new ValueFactoryImpl().createURI( uri );
			//This code block will only continue if the passed-in value
			//can be converted into an absolute URI:

			String uris[] = uri.split( "/" );
			return uris[uris.length - 1];
		}
		catch ( IllegalArgumentException e ) {
		}

		//Otherwise, simply return the input string:
		return uri;
	}

	/**
	 * Overload on the above method, to get the URI's label from the passed-in uri
	 * string. If the passed-in value is not a URI, then the above method is
	 * called to extract the ending word, after the last "/".
	 *
	 * This method calls "String getInstanceLabel(URI uri, IEngine eng)" and
	 * "String getInstanceName(String uri)".
	 *
	 * @param uri -- (String) A string URI.
	 * @param eng -- (IEngine) The active query engine.
	 *
	 * @return getInstanceName -- (String) Described above.
	 */
	public static String getInstanceName( String uri, IEngine eng ) {
		String strReturnValue;

		//If the string is really a URI, then return its label:
		try {
			ValueFactory vf = new ValueFactoryImpl();
			URI uriURI = vf.createURI( uri );
			strReturnValue = getInstanceLabel( uriURI, eng );

			//If the previous method call returned nothing,
			//then extract the ending word of the URI's string:
			if ( null == strReturnValue || strReturnValue.equals( "" ) ) {
				strReturnValue = getInstanceName( uri );
			}
			//Otherwise, simply return the input string:
		}
		catch ( IllegalArgumentException e ) {
			strReturnValue = uri;
		}
		return strReturnValue;
	}

	/**
	 * A convenience function to {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) } when you only have a single URI. If
	 * you have more than one URI, {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) } is much more performant.
	 *
	 * @param eng where to get the label from
	 * @param uri the uri we need a label for
	 *
	 * @return the label, or the localname if no label is in the engine
	 */
	public static <X extends Resource> String getInstanceLabel( X uri, IEngine eng ) {
		return getInstanceLabels( Arrays.asList( uri ), eng ).get( uri );
	}

	/**
	 * Gets labels for the given uris from the given engine. If the engine doesn't
	 * contain a {@link RDFS#LABEL} element, just use a
	 * {@link URLDecoder#decode(java.lang.String, java.lang.String) URLDecoded}
	 * version of the local name
	 *
	 * @param uris the URIs to retrieve the labels from
	 * @param eng the engine to search for labels
	 *
	 * @return a map of URI=&gt;label
	 */
	public static <X extends Resource> Map<X, String>
			getInstanceLabels( final Collection<X> uris, IEngine eng ) {
		if ( uris.isEmpty() ) {
			return new HashMap<>();
		}

		final Map<Resource, String> retHash = new HashMap<>();

		StringBuilder sb
				= new StringBuilder( "SELECT ?s ?label WHERE { ?s rdfs:label ?label }" );
		sb.append( " VALUES ?s {" );
		for ( Resource uri : uris ) {
			if ( null == uri ) {
				log.warn( "trying to find the label of a null Resource? (probably a bug)" );
			}
			else {
				sb.append( " <" ).append( uri.stringValue() ).append( ">\n" );
			}
		}
		sb.append( "}" );

		VoidQueryAdapter vqa = new VoidQueryAdapter( sb.toString() ) {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				String lbl = set.getValue( "label" ).stringValue();
				retHash.put( fac.createURI( set.getValue( "s" ).stringValue() ), lbl );
			}
		};
		try {
			eng.query( vqa );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( sb, e );
		}

		// add any URIs that don't have a label, but were in the argument collection
		Set<Resource> todo = new HashSet<>( uris );
		todo.removeAll( retHash.keySet() );
		for ( Resource u : todo ) {
			if ( u instanceof URI ) {
				retHash.put( u, URI.class.cast( u ).getLocalName() );
			}
			else if ( u instanceof BNode ) {
				retHash.put( u, BNode.class.cast( u ).getID() );
			}
			else {
				retHash.put( u, u.stringValue() );
			}
		}

		return (Map<X, String>) retHash;
	}

	/**
	 * Increases the counter and gets the next ID for a URI.
	 *
	 * @return Next ID
	 */
	public static String getNextID() {
		id++;
		return Constants.BLANK_URL + "/" + id;
	}

	/**
	 * Displays error message.
	 *
	 * @param text to be displayed.
	 */
	public static void showError( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		JOptionPane.showMessageDialog( playPane, text, "Error",
				JOptionPane.ERROR_MESSAGE );
	}

	/**
	 * Displays option message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Yes, 1 No, 2 Cancel, -1 message
	 * closed
	 */
	public static int showOptionsYesNoCancel( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		return JOptionPane.showConfirmDialog( playPane, text );
	}

	/**
	 * Displays warning message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Ok, 2 Cancel, -1 message closed
	 */
	public static int showWarningOkCancel( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		return JOptionPane.showConfirmDialog( playPane, text, "Select An Option",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
	}

	/**
	 * Displays confirmation message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Ok, 2 Cancel, -1 message closed
	 */
	public static int showConfirmOkCancel( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		return JOptionPane.showConfirmDialog( playPane, text, "Select An Option",
				JOptionPane.OK_CANCEL_OPTION );
	}

	/**
	 * Displays a message on the screen.
	 *
	 * @param text to be displayed.
	 */
	public static void showMessage( String text ) {
		JFrame playPane = DIHelper.getInstance().getPlayPane();
		JOptionPane.showMessageDialog( playPane, text );
	}

	public static void showExportMessage( Frame frame, String message, String title,
			File exportloc ) {
		if ( Desktop.isDesktopSupported() ) {
			String options[] = { "Open Location", "Close" };
			int opt = JOptionPane.showOptionDialog( frame, message, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0] );

			if ( 0 == opt ) {
				try {
					Desktop.getDesktop().open( exportloc.getParentFile() );
				}
				catch ( Exception e ) {
					log.error( e, e );
				}
			}
		}
		else {
			JOptionPane.showMessageDialog( frame, message );
		}
	}

	/**
	 * Loads an engine - sets the core properties, loads base data engine and
	 * ontology file.
	 *
	 * @param smssfile
	 *
	 * @return Loaded engine.
	 *
	 * @throws java.io.IOException
	 */
	public static IEngine loadEngine( File smssfile ) throws IOException {
		Properties props;
		if ( smssfile.getName().toLowerCase().endsWith( "jnl" ) ) {
			// we're loading a BigData journal file, so make up our properties
			props = BigDataEngine.generateProperties( smssfile );
		}
		else {
			props = Utility.loadProp( smssfile );
		}

		IEngine engine = null;

		log.debug( "In Utility file name is " + smssfile );
		String engineName = props.getProperty( Constants.ENGINE_NAME,
				FilenameUtils.getBaseName( smssfile.getAbsolutePath() ) );

		String smssloc = smssfile.getCanonicalPath();
		props.setProperty( Constants.SMSS_LOCATION, smssloc );

		String engineClass = props.getProperty( Constants.ENGINE_IMPL );
		engineClass = engineClass.replaceAll( "prerna", "gov.va.semoss" );
		try {
			Class<IEngine> theClass = (Class<IEngine>) Class.forName( engineClass );
			engine = (IEngine) theClass.getConstructor( Properties.class ).newInstance( props );
			log.info( "Engine created." );

			if( null == engine.getEngineName() ){
				engine.setEngineName( engineName );
			}
		}
		catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			log.error( e );
		}
		
		DIHelper.getInstance().registerEngine( engine );
		return engine;
	}

	public static void closeEngine( IEngine eng ) {
		eng.closeDB();
		DIHelper.getInstance().unregisterEngine( eng );
	}

	/**
	 * Tries to load an image by first checking the filesystem, then the jar
	 * itself. The filesystem location is &lt;CWD&gt;/pictures/&lt;filename&gt;
	 * while the jar location is jar:/images/&lt;filename&gt
	 *
	 * @param imagename
	 *
	 * @return the image, or null if anything went wrong
	 */
	public static BufferedImage loadImage( String imagename ) {
		try {
			return ImageIO.read( new File( "pictures", imagename ) );
		}
		catch ( IOException ignored ) {
		}

		try {
			return ImageIO.read( GuiUtility.class.getResourceAsStream(
					"/images/" + imagename ) );
		}
		catch ( IOException | IllegalArgumentException ie ) {
			log.warn( "could not load file: " + imagename );
		}

		return null;
	}

	/**
	 * Loads the image, scales it to Icon size, and creates an ImageIcon to return
	 *
	 * @param imagename
	 * @return the loaded ImageIcon, or blank ImageIcon if anything went wrong
	 */
	public static ImageIcon loadImageIcon( String imagename ) {
		try {
			Image img = loadImage( imagename );
			Image newimg = img.getScaledInstance( 15, 15, java.awt.Image.SCALE_SMOOTH );
			return new ImageIcon( newimg );
		}
		catch ( Exception e ) {
			log.warn( "Error loading image icon for imagename " + imagename + ": " + e, e );
		}

		return new ImageIcon();
	}

	public static GraphPlaySheet getActiveGraphPlaysheet() {
		JInternalFrame jif = DIHelper.getInstance().getDesktop().getSelectedFrame();
		if ( jif instanceof PlaySheetFrame ) {
			PlaySheetFrame psf = PlaySheetFrame.class.cast( jif );
			return GraphPlaySheet.class.cast( psf.getActivePlaySheet() );
		}
		return null;
	}

	public static void repaintActiveGraphPlaysheet() {
		getActiveGraphPlaysheet().fireGraphUpdated();
	}

	public static void addModelToJTable( AbstractTableModel tableModel, String tableKey ) {
		JTable table = DIHelper.getJTable( tableKey );
		table.setModel( tableModel );
		tableModel.fireTableDataChanged();

		for ( int i = 0; i < tableModel.getColumnCount(); i++ ) {
			if ( Boolean.class.equals( tableModel.getColumnClass( i ) ) ) {
				TableColumnModel columnModel = table.getColumnModel();

				if ( i < columnModel.getColumnCount() ) {
					columnModel.getColumn( i ).setPreferredWidth( 35 );
				}
			}
		}
	}

	public static void resetJTable( String tableKey ) {
		DIHelper.getJTable( tableKey ).setModel( new DefaultTableModel() );
		log.debug( "Resetting the " + tableKey + " table model." );
	}

	public static ImportData createImportData( IEngine eng ) {
		ImportMetadata metas = null;
		if ( null == eng ) {
			metas = new ImportMetadata();
		}
		else {
			metas = new ImportMetadata( eng.getBaseUri(), eng.getSchemaBuilder(),
					eng.getDataBuilder() );
			metas.setNamespaces( eng.getNamespaces() );

			try {
				MetadataQuery mq = new MetadataQuery();
				eng.query( mq );
				metas.setExtras( mq.asStrings() );
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
				log.error( e, e );
			}
		}

		return new ImportData( metas );
	}
}
