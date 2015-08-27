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

import gov.va.semoss.model.vocabulary.SEMOSS;
import gov.va.semoss.model.vocabulary.VAC;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.rdf.engine.api.MetadataConstants;

import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * The GuiUtility class contains a variety of miscellaneous functions
 * implemented extensively throughout SEMOSS. Some of these functionalities
 * include getting concept names, printing messages, loading engines, and
 * writing Excel workbooks.
 */
public class Utility {

	private static final Logger log = Logger.getLogger( Utility.class );
	public static final Map<String, String> DEFAULTNAMESPACES = new HashMap<>();

	static {
		DEFAULTNAMESPACES.put( RDF.PREFIX, RDF.NAMESPACE );
		DEFAULTNAMESPACES.put( RDFS.PREFIX, RDFS.NAMESPACE );
		DEFAULTNAMESPACES.put( OWL.PREFIX, OWL.NAMESPACE );
		DEFAULTNAMESPACES.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		DEFAULTNAMESPACES.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		DEFAULTNAMESPACES.put( FOAF.PREFIX, FOAF.NAMESPACE );
		DEFAULTNAMESPACES.put( MetadataConstants.VOID_PREFIX, MetadataConstants.VOID_NS );
		DEFAULTNAMESPACES.put( VAS.PREFIX, VAS.NAMESPACE );
		DEFAULTNAMESPACES.put( VAC.PREFIX, VAC.NAMESPACE );
		DEFAULTNAMESPACES.put( SEMOSS.PREFIX, SEMOSS.NAMESPACE );
	}

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
	 * Extracts parameter bindings from the passed-in query of the form,
	 * "<@name-http:value@>", into a hash of variable names, types, and parameter
	 * queries (created from types), suitable for Insight parameter drop-downs on
	 * the left-pane of the tool.
	 *
	 * @param query -- (String) The Insight's Sparql query.
	 *
	 * @return -- (Map<String, Map<String, String>>) The hash described above.
	 */
	public static Map<String, Map<String, String>> getParamTypeQueryHash( String query ) {
		Map<String, Map<String, String>> paramQueryHash = new HashMap<>();
		Pattern pattern = Pattern.compile( "[@]{1}\\w+[-]*[\\w/.:]+[@]" );

		Matcher matcher = pattern.matcher( query );
		while ( matcher.find() ) {
			String data = matcher.group();
			data = data.substring( 1, data.length() - 1 );
			String paramVariable = data.substring( 0, data.indexOf( "-" ) );
			String paramType = data.substring( data.indexOf( "-" ) + 1 );
			String paramQuery = "SELECT ?entity WHERE{ ?entity a <" + paramType + "> .}";

			log.debug( "paramTypeQueryhash row: " + paramVariable + ", " + paramType + ", " + paramQuery );
			Map<String, String> paramElement = new HashMap<>();
			paramElement.put( "parameterValueType", paramType );
			paramElement.put( "parameterQuery", paramQuery );
			paramQueryHash.put( paramVariable, paramElement );
		}
		return paramQueryHash;
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
	 * A convenience for {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) }, but returns a sorted map with
	 * consistent iteration pattern
	 *
	 * @param urilabels a mapping of URIs to their labels. Say, the results of {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) }
	 *
	 * @return the results
	 */
	public static <X extends Resource> Map<X, String> sortUrisByLabel( Map<X, String> urilabels ) {
		List<ResourceLabelPair> pairs = new ArrayList<>();

		for ( Map.Entry<X, String> p : urilabels.entrySet() ) {
			Resource r = p.getKey();

			pairs.add( new ResourceLabelPair( r, p.getValue() ) );
		}
		Collections.sort( pairs );

		LinkedHashMap<Resource, String> ret = new LinkedHashMap<>();
		for ( ResourceLabelPair ulp : pairs ) {
			ret.put( ulp.r, ulp.l );
		}

		return (Map<X, String>) ret;
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
	 * Rounds the given value to the given number of decimal places
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

	public static String getSaveFilename( String base, String extension ) {
		SimpleDateFormat sdf = new SimpleDateFormat( "_MMM dd, yyyy HHmm." );
		return base + sdf.format( new Date() )
				+ ( extension.startsWith( "." ) ? extension.substring( 1 ) : extension );
	}

	public static void extractHTML() throws IOException {
		// check for html directory
		// extract

		// this should look for an "html" folder under the current working (execution) directory of the tool:
		Path localHtmlPath = Paths.get( "html" );
		if ( Files.exists( localHtmlPath ) ) {
			return;
		}

		try ( InputStream htmlIs = Utility.class.getResourceAsStream( "/html.zip" ) ) {
			unzip( new ZipInputStream( htmlIs ), new File( "html" ) );
		}
	}

	public static void unzip( String zipFilePath, String destDir ) throws IOException {
		try ( ZipInputStream zips
				= new ZipInputStream( new FileInputStream( new File( zipFilePath ) ) ) ) {
			unzip( zips, new File( destDir ) );
		}
	}

	public static void unzip( ZipInputStream zis, File destDir ) throws IOException {
		ZipEntry zipEntry;
		while ( null != ( zipEntry = zis.getNextEntry() ) ) {
			String name = zipEntry.getName();
			/* For debugging when needed
			 *
			 long size = zipEntry.getSize();
			 long compressedSize = zipEntry.getCompressedSize();
			 System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
			 name, size, compressedSize);
			 */

			File file = new File( destDir, name );
			if ( name.endsWith( "/" ) ) {
				file.mkdirs();
				continue;
			}

			File parent = file.getParentFile();
			if ( parent != null ) {
				parent.mkdirs();
			}

			try ( FileOutputStream fos = new FileOutputStream( file ) ) {
				byte[] bytes = new byte[1024];
				int length;
				while ( ( length = zis.read( bytes ) ) >= 0 ) {
					fos.write( bytes, 0, length );
				}
				zis.closeEntry();
			}
		}
	}



	/**
	 * Implodes the given collection, appending <code>start</code> before each
	 * element, and <code>stop</code> after each one, and <code>sep</code> in
	 * between
	 *
	 * @param collection the elements to implode. {@link Object#toString()} will
	 * be called on each one
	 * @param start what to put before each element
	 * @param stop what to put after each element
	 * @param sep what to put between elements
	 * @return a string representation of the given collection. If the collection
	 * is empty or null, an empty string will be returned.
	 */
	public static String implode( Collection<?> collection, String start, String stop,
			String sep ) {
		if ( null == collection ) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for ( Object o : collection ) {
			if ( 0 != sb.length() ) {
				sb.append( sep );
			}
			sb.append( start ).append( o.toString() ).append( stop );
		}

		return sb.toString();
	}
	
	/**   Prepares a string for use in a dynamic Sparql query, where " is a string
	* delimiter. The double-quote, ", is changed to ', and existing single-quotes
	* are left alone. Also, replaces newline characters, "\n" with "\\n", and carriage-
	* returns, "\r", with "". 
	* 
	* Note: This method may be modified to handle other replacements required in strings
	*       for SPARQL.
	*
	* @param quotedString -- (String) The string containing double and single
	* quotes.
	*
	* @return legalizeStringForSparql -- (String) The cleaned string, as described above.
	*/
    public static String legalizeStringForSparql(String quotedString) {
	   String strReturnValue = quotedString;
	
	   strReturnValue = strReturnValue.replace( "\"", "'" ).replace("\n", "\\n").replace("\r", "");
	
	   return strReturnValue;
	}
	
	private static class ResourceLabelPair implements Comparable<ResourceLabelPair> {

		public final Resource r;
		public final String l;

		public ResourceLabelPair( Resource r, String l ) {
			this.r = r;
			this.l = l;
		}

		@Override
		public int compareTo( ResourceLabelPair t ) {
			return l.compareTo( t.l );
		}
	}

}
