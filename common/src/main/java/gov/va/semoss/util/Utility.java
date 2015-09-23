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
import java.io.Writer;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;

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

	/**
	 * Gets the appropriate exporter for the given filename. "Appropriate" means
	 * the file's suffix determines exporter. If no appropriate handler can be
	 * found, an NTriples one is returned. Handled suffixes (case insensitive):
	 * <li>nt</li>
	 * <li>rdf</li>
	 * <li>ttl</li>
	 *
	 * @param file the filename to determine the handler to use
	 * @param out the output writer to use to create the handler
	 * @return a handler (always)
	 */
	public static RDFHandler getExporterFor( String filename, Writer out ) {
		String suffix = FilenameUtils.getExtension( filename ).toLowerCase();
		switch ( suffix ) {
			case "rdf":
				return new RDFXMLWriter( out );
			case "ttl":
				return new TurtleWriter( out );
			default:
				return new NTriplesWriter( out );
		}
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
