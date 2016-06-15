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
package com.ostrichemulators.semtool.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import com.ostrichemulators.semtool.model.vocabulary.SEMCORE;
import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.util.EngineLoader;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * The GuiUtility class contains a variety of miscellaneous functions
 * implemented extensively throughout SEMONTO. Some of these functionalities
 * include getting concept names, printing messages, loading engines, and
 * writing Excel workbooks.
 */
public class Utility {

	private static final Logger log = Logger.getLogger( Utility.class );
	private static final UriBuilder BUILDER = UriBuilder.getBuilder( Constants.INTERNAL_NS );
	public static final Map<String, String> DEFAULTNAMESPACES = new HashMap<>();

	static {
		DEFAULTNAMESPACES.put( RDF.PREFIX, RDF.NAMESPACE );
		DEFAULTNAMESPACES.put( RDFS.PREFIX, RDFS.NAMESPACE );
		DEFAULTNAMESPACES.put( OWL.PREFIX, OWL.NAMESPACE );
		DEFAULTNAMESPACES.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		DEFAULTNAMESPACES.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		DEFAULTNAMESPACES.put( FOAF.PREFIX, FOAF.NAMESPACE );
		DEFAULTNAMESPACES.put( MetadataConstants.VOID_PREFIX, MetadataConstants.VOID_NS );
		DEFAULTNAMESPACES.put( SEMTOOL.PREFIX, SEMTOOL.NAMESPACE );
		DEFAULTNAMESPACES.put( SEMCORE.PREFIX, SEMCORE.NAMESPACE );
		DEFAULTNAMESPACES.put( SEMONTO.PREFIX, SEMONTO.NAMESPACE );
	}

	public static URI getUniqueUri() {
		return BUILDER.uniqueUri();
	}

	public static URI makeInternalUri( String something ) {
		return BUILDER.build( something );
	}

	/**
	 * A convenience for {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) }, but returns a sorted map with
	 * consistent iteration pattern
	 *
	 * @param <X>
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

	public static void loadProp( URL file, Properties props ) throws IOException {
		try ( InputStream is = file.openStream() ) {
			props.load( is );

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

	public static Properties loadProp( URL url ) throws IOException {
		Properties p = new Properties();
		loadProp( url, p );
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

		int sval = starter.get( Calendar.MILLISECOND );
		int eval = stopper.get( Calendar.MILLISECOND );
		int msecs = eval - sval;

		sval = starter.get( Calendar.SECOND );
		eval = stopper.get( Calendar.SECOND );
		int secs = eval - sval;

		sval = starter.get( Calendar.MINUTE );
		eval = stopper.get( Calendar.MINUTE );
		int mins = eval - sval;

		if ( msecs < 0 ) {
			msecs += 1000;
			secs--;
		}

		if ( secs < 0 ) {
			secs += 60;
			mins--;
		}

		// we don't expect any duration to be more than 1 hour
		if ( mins < 0 ) {
			mins += 60;
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

	public static void unzip( String zipFilePath, String destDir ) throws IOException {
		try ( ZipInputStream zips
				= new ZipInputStream( new BufferedInputStream(
								new FileInputStream( new File( zipFilePath ) ) ) ) ) {
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
	 * Gets the implosion suitable for building a SPARQL VALUES token . This is a
	 * convenience function to {@link #implode(java.util.Collection,
	 * java.lang.String, java.lang.String, java.lang.String)
	 * implode( collection, "&lt;", "&gt;", " " ) }
	 *
	 * @param collection
	 * @return
	 */
	public static String implode( Collection<URI> collection ) {
		return implode( collection, "<", ">", " " );
	}

	/**
	 * Gets the appropriate exporter for the given filename. "Appropriate" means
	 * the file's suffix determines exporter. If no appropriate handler can be
	 * found, an NTriples one is returned. Handled suffixes (case insensitive):
	 * <ul>
	 * <li>nt
	 * <li>rdf
	 * <li>ttl
	 * </ul>
	 *
	 * @param filename the filename to determine the handler to use
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

	/**
	 * A convenience function to {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) } when you only have a single URI. If
	 * you have more than one URI, {@link #getInstanceLabels(java.util.Collection,
	 * gov.va.semoss.rdf.engine.api.IEngine) } is much more performant.
	 *
	 * @param <X>
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
	 * @param <X>
	 * @param uris the URIs to retrieve the labels from
	 * @param eng the engine to search for labels
	 *
	 * @return a map of URI=&gt;label
	 */
	public static <X extends Resource> Map<X, String>
			getInstanceLabels( final Collection<X> uris, IEngine eng ) {

		if ( null == uris || uris.isEmpty() ) {
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
				if ( !( uri instanceof BNode ) ) { // don't know how to query a BNode
					sb.append( " <" ).append( uri.stringValue() ).append( ">\n" );
				}
			}
		}
		sb.append( "}" );

		VoidQueryAdapter vqa = new VoidQueryAdapter( sb.toString() ) {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				String lbl = set.getValue( "label" ).stringValue();
				Value r = set.getValue( "s" );
				retHash.put( Resource.class.cast( r ), lbl );
			}
		};

		if ( null == eng ) {
			log.warn( "querying null engine in getInstanceLabels not likely to work" );
		}
		else {
			eng.queryNoEx( vqa );
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

	public static Properties getBuildProperties( Class<?> klass ) {
		try {
			return Utility.loadProp( klass.getResource( "/build.properties" ) );
		}
		catch ( IOException ioe ) {
			log.warn( ioe, ioe );
		}
		return new Properties();
	}

	public static void ttlToStdout( Model model ) {
		TurtleWriter tw = new TurtleWriter( System.out );
		try {
			tw.startRDF();
			for ( Map.Entry<String, String> en : DEFAULTNAMESPACES.entrySet() ) {
				tw.handleNamespace( en.getKey(), en.getValue() );
			}

			for ( Statement s : model ) {
				tw.handleStatement( s );
			}
			tw.endRDF();
		}
		catch ( Exception e ) {
			log.error( e, e );
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
