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

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectWrapper;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.ui.components.RepositoryList;
import gov.va.semoss.ui.components.UriComboBox.UriLabelPair;
import gov.va.semoss.ui.components.playsheets.GraphPlaySheet;
import info.aduna.io.IOUtil;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.ibm.icu.util.Calendar;

/**
 * The Utility class contains a variety of miscellaneous functions implemented
 * extensively throughout SEMOSS. Some of these functionalities include getting
 * concept names, printing messages, loading engines, and writing Excel
 * workbooks.
 */
public class Utility {

	private static final Logger log = Logger.getLogger( Utility.class );
	// private static final Pattern VALIDCHARS = Pattern.compile( "[A-Za-z0-9_.-]+" );
	// updating to allow for unicode letter chars
	private static final Pattern VALIDCHARS = Pattern.compile( "[\\w.-]+", Pattern.UNICODE_CHARACTER_CLASS );
	private static int id = 0;

	/**
	 * Matches the given query against a specified pattern. While the next
	 * substring of the query matches a part of the pattern, set substring as the
	 * key with EMPTY constants (@@) as the value
	 *
	 * @param query.
	 *
	 * @return map of queries to be replaced
	 */
	public static Map<String, String> getParams( String query ) {
		Map<String, String> paramHash = new HashMap<>();
		Pattern pattern = Pattern.compile( "[@]{1}\\w+[-]*[\\w/.:]+[@]" );

		Matcher matcher = pattern.matcher( query );
		while ( matcher.find() ) {
			String data = matcher.group();
			data = data.substring( 1, data.length() - 1 );
			log.debug( "get params matched: " + data );
			// put something to strip the @
			paramHash.put( data, Constants.EMPTY );
		}

		return paramHash;
	}

	/**
	 * Matches the given query against a specified pattern. While the next
	 * substring of the query matches a part of the pattern, set substring as the
	 * key with EMPTY constants (@@) as the value
	 *
	 * @param query.
	 *
	 * @return Hashtable of queries to be replaced
	 */
	public static Map<String, String> getParamTypeHash( String query ) {
		Map<String, String> paramHash = new HashMap<>();
		Pattern pattern = Pattern.compile( "[@]{1}\\w+[-]*[\\w/.:]+[@]" );

		Matcher matcher = pattern.matcher( query );
		while ( matcher.find() ) {
			String data = matcher.group();
			data = data.substring( 1, data.length() - 1 );
			String paramName = data.substring( 0, data.indexOf( "-" ) );
			String paramValue = data.substring( data.indexOf( "-" ) + 1 );

			log.debug( "paramtypehash data: " + data );
			// put something to strip the @
			paramHash.put( paramName, paramValue );
		}

		return paramHash;
	}

	/**
	 * Matches the given query against a specified pattern. While the next
	 * substring of the query matches a part of the pattern, set substring as the
	 * key with EMPTY constants (@@) as the value
	 *
	 * @param query the query to fix (?)
	 *
	 * @return Hashtable of queries to be replaced
	 */
	public static String normalizeParam( String query ) {
		Map<String, String> paramHash = new HashMap<>();
		Pattern pattern = Pattern.compile( "[@]{1}\\w+[-]*[\\w/.:]+[@]" );

		Matcher matcher = pattern.matcher( query );
		while ( matcher.find() ) {
			String data = matcher.group();
			data = data.substring( 1, data.length() - 1 );
			String paramName = data.substring( 0, data.indexOf( "-" ) );

			log.debug( "normalizeparam data: " + data );
			// put something to strip the @
			paramHash.put( data, "@" + paramName + "@" );
		}

		return fillParam( query, paramHash );
	}

	/**
	 * Gets the param hash and replaces certain queries
	 *
	 * @param query
	 * @param paramHash
	 *
	 * @return If applicable, returns the replaced query
	 */
	public static String fillParam( String query, Map<String, String> paramHash ) {
		// Hashtable is of pattern <String to be replaced> <replacement>
		// key will be surrounded with @ just to be in sync
		log.debug( "fillparam raw query is: " + query );
		log.debug( "param hash is " + paramHash );

		Map<String, String> map = new HashMap<>( paramHash );
		for ( Map.Entry<String, String> e : map.entrySet() ) {
			String key = e.getKey();
			String value = e.getValue();
			log.debug( "Replacing " + key + "<<>>" + value + query.indexOf(
					"@" + key + "@" ) );
			if ( !value.equalsIgnoreCase( Constants.EMPTY ) ) {
				query = query.replace( "@" + key + "@", value );
			}
		}

		return query;
	}

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
	public static String getInstanceLabel( URI uri, IEngine eng ) {
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
	public static Map<URI, String> getInstanceLabels( final Collection<URI> uris,
			IEngine eng ) {
		if ( uris.isEmpty() ) {
			return new HashMap<>();
		}

		final Map<URI, String> retHash = new HashMap<>();

		StringBuilder sb
				= new StringBuilder( "SELECT ?s ?label WHERE { ?s rdfs:label ?label }" );
		sb.append( " VALUES ?s {" );
		for ( URI uri : uris ) {
			sb.append( " <" ).append( uri.stringValue() ).append( ">\n" );
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
		Set<URI> todo = new HashSet<>( uris );
		todo.removeAll( retHash.keySet() );
		for ( URI u : todo ) {
			retHash.put( u, u.getLocalName() );
		}

		return retHash;
	}

	/**
	 * A convenience for {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) }, but returns a sorted map with
	 * consistent iteration pattern
	 *
	 * @param urilabels a mapping of URIs to their labels. Say, the results of
	 * null	null	null	null null null null null null	null	null	null	null	null	null
	 * null	null	 {@link #getInstanceLabels(java.util.Collection, 
   * gov.va.semoss.rdf.engine.api.IEngine) }
	 *
	 * @return the results
	 */
	public static Map<URI, String> sortUrisByLabel( Map<URI, String> urilabels ) {
		List<UriLabelPair> pairs = new ArrayList<>();

		for ( Map.Entry<URI, String> p : urilabels.entrySet() ) {
			pairs.add( new UriLabelPair( p.getKey(), p.getValue() ) );
		}
		Collections.sort( pairs );

		LinkedHashMap<URI, String> ret = new LinkedHashMap<>();
		for ( UriLabelPair ulp : pairs ) {
			ret.put( ulp.getUri(), ulp.getLabel() );
		}

		return ret;
	}

	/**
	 * Executes a query on a specific engine, iterates through variables from the
	 * sesame wrapper, and uses logic to obtain the concept URI.
	 *
	 * @param engine engine.
	 * @param subjectURI.
	 *
	 * @return Concept URI.
	 */
	public static String getConceptType( IEngine engine, String subjectURI ) {
		if ( !subjectURI.startsWith( "http://" ) ) {
			return "";
		}

		String query = DIHelper.getInstance().getProperty(
				Constants.SUBJECT_TYPE_QUERY );
		Map<String, String> paramHash = new HashMap<>();
		paramHash.put( "ENTITY", subjectURI );
		query = Utility.fillParam( query, paramHash );
		SesameJenaSelectWrapper sjw = new SesameJenaSelectWrapper();
		sjw.setEngine( engine );
		sjw.setEngineType( engine.getEngineType() );
		sjw.setQuery( query );
		sjw.executeQuery();
		String[] vars = sjw.getVariables();
		String returnType = null;
		while ( sjw.hasNext() ) {
			SesameJenaSelectStatement stmt = sjw.next();
			String objURI = stmt.getRawVar( vars[0] ) + "";
			if ( !objURI.equals( engine.getSchemaBuilder().getConceptUri().toString() ) ) {
				returnType = objURI;
			}
		}
		if ( returnType == null ) {
			returnType = engine.getSchemaBuilder().getConceptUri().toString();
		}

		return returnType;
	}

	/**
	 * Splits up a URI into tokens based on "/" delimiter and uses logic to return
	 * the class name.
	 *
	 * @param uri
	 *
	 * @return Name of class.
	 */
	public static String getClassName( String uri ) {
		String[] strs = uri.split( "/" );
		if ( strs.length > 1 ) {
			return strs[strs.length - 2];
		}
		return null;
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
	 * Gets the instance and class names for a specified URI and creates the
	 * qualified class name.
	 *
	 * @param uri.
	 *
	 * @return Qualified URI.
	 */
	public static String getQualifiedClassName( String uri ) {
		// there are three patterns
		// one is the /
		// the other is the #
		// need to have a check upfront to see 

		String instanceName = getInstanceName( uri );

		String className = getClassName( uri );
		String qualUri;
		if ( uri.contains( "/" ) ) {
			instanceName = "/" + instanceName;
		}

		// remove this in the end
		if ( className == null ) {
			qualUri = uri.replace( instanceName, "" );
		}
		else {
			qualUri = uri.replace( className + instanceName, className );
		}

		return qualUri;
	}

	/**
	 * Checks to see if a string contains a particular pattern. Used when adding
	 * relations.
	 *
	 * @param pattern
	 * @param string to compare to the pattern
	 *
	 * @return True if the next token is greater than or equal to zero.
	 */
	public static boolean checkPatternInString( String pattern, String string ) {
		// ok.. before you think that this is so stupid why wont you use the regular java.lang methods.. consider the fact that this could be a ; delimited pattern
		if ( null != pattern ) {
			for ( String str : string.split( ";" ) ) {
				if ( str.contains( pattern ) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Runs a check to see if calculations have already been performed.
	 *
	 * @param query (calculation) to be run on a specific engine.
	 *
	 * @return True if calculations have been performed.
	 */
	public static boolean runCheck( String query ) {
		boolean check = true;

		RepositoryList list = DIHelper.getInstance().getRepoList();
		// get the selected repository
		List<IEngine> repos = list.getSelectedValuesList();

		SesameJenaSelectWrapper selectWrapper = null;
		for ( IEngine engine : repos ) {
			selectWrapper = new SesameJenaSelectWrapper();
			selectWrapper.setEngine( engine );
			selectWrapper.setQuery( query );
			selectWrapper.executeQuery();
		}

		//if the wrapper is not empty, calculations have already been performed.
		if ( null != selectWrapper && !selectWrapper.hasNext() ) {
			check = false;
		}

		return check;
	}

	/**
	 * Displays error message.
	 *
	 * @param text to be displayed.
	 */
	public static void showError( String text ) {
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp(
				Constants.MAIN_FRAME );
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
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp(
				Constants.MAIN_FRAME );
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
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp(
				Constants.MAIN_FRAME );
		return JOptionPane.showConfirmDialog( playPane, text, "Select An Option", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
	}

	/**
	 * Displays confirmation message.
	 *
	 * @param text to be displayed.
	 *
	 * @return int value of choice selected: 0 Ok, 2 Cancel, -1 message closed
	 */
	public static int showConfirmOkCancel( String text ) {
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp(
				Constants.MAIN_FRAME );
		return JOptionPane.showConfirmDialog( playPane, text, "Select An Option",
				JOptionPane.OK_CANCEL_OPTION );
	}

	/**
	 * Displays a message on the screen.
	 *
	 * @param text to be displayed.
	 */
	public static void showMessage( String text ) {
		JFrame playPane = (JFrame) DIHelper.getInstance().getLocalProp(
				Constants.MAIN_FRAME );
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
	 * Method round.
	 *
	 * @param valueToRound double
	 * @param numberOfDecimalPlaces int
	 *
	 * @return double
	 */
	public static double round( double valueToRound, int numberOfDecimalPlaces ) {
		double multipicationFactor = Math.pow( 10, numberOfDecimalPlaces );
		double interestedInZeroDPs = valueToRound * multipicationFactor;
		return Math.round( interestedInZeroDPs ) / multipicationFactor;
	}

	/**
	 * Used to round a value to a specific number of decimal places.
	 *
	 * @param valueToRound to round.
	 *
	 * @return Rounded value.
	 */
	public static String sciToDollar( double valueToRound ) {
		double roundedValue = Math.round( valueToRound );
		DecimalFormat df = new DecimalFormat( "#0" );
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		df.format( roundedValue );
		String retString = formatter.format( roundedValue );
		return retString;
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
			props = loadProp( smssfile );
		}

		IEngine engine = null;

		log.debug( "In Utility file name is " + smssfile );
		String engineName = props.getProperty( Constants.ENGINE_NAME,
				FilenameUtils.getBaseName( smssfile.getAbsolutePath() ) );

		try {
			String smssloc = smssfile.getCanonicalPath();
			props.setProperty( Constants.SMSS_LOCATION, smssloc );

			String engineClass = props.getProperty( Constants.ENGINE_IMPL );
			engineClass = engineClass.replaceAll( "prerna", "gov.va.semoss" );
			engine = (IEngine) Class.forName( engineClass ).newInstance();
			engine.setEngineName( engineName );
			//if ( props.getProperty( "MAP" ) != null ) {
			//	engine.setMap( props.getProperty( "MAP" ) );
			//}

			engine.openDB( props );
			DIHelper.getInstance().registerEngine( engine );
		}
		catch ( InstantiationException | IllegalAccessException | ClassNotFoundException e ) {
			log.error( e );
		}
		return engine;
	}

	public static void closeEngine( IEngine eng ) {
		eng.closeDB();
		DIHelper.getInstance().unregisterEngine( eng );
	}

	/**
	 * Copies the <code>newones</code> to <code>original</code>.
	 *
	 * @param original the properties "base"
	 * @param newones the properties to add to "base"
	 * @param appendClashes when true, if both properties have the same keys,
	 * append <code>newones</code> values to those already in
	 * <code>original</code>. if false, replace the original values with the new
	 * ones
	 * @param delimiter The delimiter to use when appending two values together
	 */
	public static void mergeProperties( Properties original, Properties newones,
			boolean appendClashes, String delimiter ) {
		for ( String prop : newones.stringPropertyNames() ) {
			String newval = newones.getProperty( prop );

			if ( original.containsKey( prop ) && appendClashes ) {
				newval = original.getProperty( prop ) + delimiter + newval;
			}
			original.setProperty( prop, newval );
		}
	}

	/**
	 * Loads the Properties from the given file
	 *
	 * @param file The properties file to be loaded.
	 * @param props the properties object to load
	 *
	 * @throws java.io.IOException
	 */
	public static void loadProp( File file, Properties props ) throws IOException {
		try ( FileReader fis = new FileReader( file ) ) {
			props.load( fis );

			if ( log.isDebugEnabled() ) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter( sw );
				props.list( pw );
				log.debug( sw );
			}
		}
	}

	/**
	 * Loads a Properties object from the given file
	 *
	 * @param file The properties file to be loaded.
	 *
	 * @return a new properties object
	 *
	 * @throws java.io.IOException
	 *
	 */
	public static Properties loadProp( File file ) throws IOException {
		Properties p = new Properties();
		loadProp( file, p );
		return p;
	}

	/**
	 * Copies the given properties into a new Properties object. This function
	 * does NOT do a deep-copy of the argument's supporting Properties, if any
	 *
	 * @param from the source properties
	 *
	 * @return the new copy
	 */
	public static Properties copyProperties( Properties from ) {
		Properties to = new Properties();
		for ( String key : from.stringPropertyNames() ) {
			to.setProperty( key, from.getProperty( key ) );
		}
		return to;
	}

	public static boolean isValidUriChars( String raw ) {
		return VALIDCHARS.matcher( raw ).matches();
	}

	/**
	 * Generates a URI-compatible string
	 *
	 * @param original string
	 * @param replaceForwardSlash if true, makes the whole string URI compatible.
	 * If false, splits the string on /, and URI-encodes the intervening
	 * characters
	 *
	 * @return Cleaned string
	 */
	public static String getUriCompatibleString( String original,
			boolean replaceForwardSlash ) {
		String trimmed = original.trim();
		if ( trimmed.isEmpty() ) {
			return trimmed;
		}
		StringBuilder sb = new StringBuilder();
		try {
			if ( replaceForwardSlash || !trimmed.contains( "/" ) ) {
				if ( isValidUriChars( trimmed ) ) {
					sb.append( trimmed );
				}
				else {
					sb.append( RandomStringUtils.randomAlphabetic( 1 ) )
							.append( UUID.randomUUID().toString() );
				}
			}
			else {
				Pattern pat = Pattern.compile( "([A-Za-z0-9-_]+://)(.*)" );
				Matcher m = pat.matcher( trimmed );
				String extras;
				if ( m.matches() ) {
					sb.append( m.group( 1 ) );
					extras = m.group( 2 );
				}
				else {
					extras = trimmed;
				}
				boolean first = true;
				for ( String part : extras.split( "/" ) ) {
					String add = ( isValidUriChars( part ) ? part
							: RandomStringUtils.randomAlphabetic( 1 )
							+ UUID.randomUUID().toString() );

					if ( first ) {
						first = false;
					}
					else {
						sb.append( "/" );
					}
					sb.append( add );
				}
			}
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}

		return sb.toString();
	}

	/**
	 * Creates an excel workbook
	 *
	 * @param wb XSSFWorkbook to write to
	 * @param fileLoc String containing the path to save the workbook
	 */
	public static void writeWorkbook( XSSFWorkbook wb, String fileLoc ) {
		try ( FileOutputStream newExcelFile = new FileOutputStream( fileLoc ) ) {

			wb.write( newExcelFile );
		}
		catch ( IOException e ) {
			showMessage( "Could not create file " + fileLoc
					+ ".\nPlease check directory structure/permissions." );
			log.error( e );
		}
	}

	public static Map<String, Object> getParamsFromString( String params ) {
		Map<String, Object> paramHash = new HashMap<>();
		if ( params != null ) {
			for ( String thisToken : params.split( "~" ) ) {
				int index = thisToken.indexOf( "$" );
				String key = thisToken.substring( 0, index );
				String value = thisToken.substring( index + 1 );
				// attempt to see if 
				boolean found = false;
				try {
					double dub = Double.parseDouble( value );
					paramHash.put( key, dub );
					found = true;
				}
				catch ( Exception ignored ) {
				}
				if ( !found ) {
					try {
						int dub = Integer.parseInt( value );
						paramHash.put( key, dub );
					}
					catch ( Exception ignored ) {
					}
				}
				//if(!found)
				paramHash.put( key, value );
			}
		}
		return paramHash;
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
			return ImageIO.read( Utility.class.getResourceAsStream(
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

	/**
	 * Creates a formatted time string of the difference between the input
	 * parameters, "startTime" and "stopTime", appropriate for display in the
	 * status bar and in popup message boxes.
	 *
	 * @param startTime -- (Date) Beginning of duration.
	 * @param stopTime -- (Date) End of duration.
	 *
	 * @return getDuration -- (String) Formatted time string described above.
	 */
	public static String getDuration( Date startTime, Date stopTime ) {
		Calendar starter = Calendar.getInstance();
		starter.setTime( startTime );
		Calendar stopper = Calendar.getInstance();
		stopper.setTime( stopTime );

		int msecs = stopper.get( Calendar.MILLISECOND ) - starter.get(
				Calendar.MILLISECOND );
		int secs = stopper.get( Calendar.SECOND ) - starter.get( Calendar.SECOND );
		int mins = stopper.get( Calendar.MINUTE ) - starter.get( Calendar.MINUTE );

		if ( msecs < 0 ) {
			msecs += 1000;
			secs--;
		}
		if ( secs < 0 ) {
			secs += 60;
			mins--;
		}
		if ( 0 == mins ) {
			// don't print "00m" if we don't have any minutes to report
			return String.format( "%02d.%02ds", secs, msecs / 10 );
		}
		return String.format( "%02dm, %02d.%02ds", mins, secs, msecs / 10 );
	}

	private static int sintRowCount = 0;

	/**
	 * Convenience method to set a Grid's row count into a static variable of this
	 * class.
	 *
	 * @param intRowCount -- (int) Current Grid's row count.
	 */
	public static void setRowCount( int intRowCount ) {
		sintRowCount = intRowCount;
	}

	/**
	 * Convenience method to get the current Grid's row count.
	 *
	 * @return getRowCount -- (int) Described above.
	 */
	public static int getRowCount() {
		return sintRowCount;
	}

	public static String getSaveFilename( String base, String extension ) {
		SimpleDateFormat sdf = new SimpleDateFormat( "_MMM dd, yyyy HHmm." );
		return base + sdf.format( new Date() )
				+ ( extension.startsWith( "." ) ? extension.substring( 1 ) : extension );
	}

	public static GraphPlaySheet getActiveGraphPlaysheet() {
		return (GraphPlaySheet) QuestionPlaySheetStore.getInstance().getActiveSheet();
	}

	public static void repaintActiveGraphPlaysheet() {
		GraphPlaySheet playSheet = getActiveGraphPlaysheet();
		playSheet.getLegendPanel().drawLegend();
		playSheet.repaint();
	}

	public static void extractHTML() throws IOException {
		// check for html directory
		// copy out html.zip
		// extract
		// remove html.zip

		// this should look for an "html" folder under the current working (execution) directory of the tool:
		Path localHtmlPath = Paths.get( "html" );
		if ( Files.exists( localHtmlPath ) ) {
			return;
		}

		try {
			IOUtil.writeStream( Utility.class.getResourceAsStream( "/html.zip" ), new File( "html.zip" ) );
			unzip( "html.zip", "html" );
			Files.delete( Paths.get( "html.zip" ) );
		}
		catch ( IOException ioe ) {
			// success or fail we need to delete the html.zip file
			Files.delete( Paths.get( "html.zip" ) );
			throw ( ioe );
		}

	}

	public static void unzip( String zipFilePath, String destDir ) throws IOException {

		ZipFile zipFile = new ZipFile( zipFilePath );
		Enumeration<?> enu = zipFile.entries();
		while ( enu.hasMoreElements() ) {
			ZipEntry zipEntry = (ZipEntry) enu.nextElement();

			String name = zipEntry.getName();
			/* For debugging when needed
			 * 
			 long size = zipEntry.getSize();
			 long compressedSize = zipEntry.getCompressedSize();
			 System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n", 
			 name, size, compressedSize);
			 */

			File file = new File( destDir + File.separator + name );
			if ( name.endsWith( "/" ) ) {
				file.mkdirs();
				continue;
			}

			File parent = file.getParentFile();
			if ( parent != null ) {
				parent.mkdirs();
			}

			InputStream is = zipFile.getInputStream( zipEntry );
			FileOutputStream fos = new FileOutputStream( file );
			byte[] bytes = new byte[1024];
			int length;
			while ( ( length = is.read( bytes ) ) >= 0 ) {
				fos.write( bytes, 0, length );
			}
			is.close();
			fos.close();
		}
		zipFile.close();
	}
	
	public static void addModelToJTable(AbstractTableModel tableModel, String tableKey) {
		DIHelper.getJTable( tableKey ).setModel( tableModel );
		tableModel.fireTableDataChanged();
		log.debug( "Added the " + tableKey + " table model." );
	}

	public static void resetJTable(String tableKey) {
		DIHelper.getJTable( tableKey ).setModel( new DefaultTableModel() );
		log.debug( "Resetting the " + tableKey + " table model." );
	}
}