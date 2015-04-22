package gov.va.semoss.poi.main;

import gov.va.semoss.model.vocabulary.SEMOSS;
import gov.va.semoss.model.vocabulary.VAC;
import gov.va.semoss.model.vocabulary.VAS;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.model.impl.ValueFactoryImpl;

public abstract class AbstractFileReader {

	protected static final Pattern NAMEPATTERN
			= Pattern.compile( "(?:(?:\"([^\"]+)\")|([^@]+))@([a-z-A-Z]{1,8})" );
	protected static final Pattern DTPATTERN
			= Pattern.compile( "\"([^\\\\^]+)\"\\^\\^(.*)" );
	protected static final Pattern URISTARTPATTERN
			= Pattern.compile( "(^[A-Za-z_-]+://).*" );
	private static final Logger logger = Logger.getLogger( AbstractFileReader.class );

	public AbstractFileReader() {
	}

	/**
	 * Method to add the common namespaces into the namespace hash of our
	 * RepositoryConnection. This function starts and commits a transaction.
	 *
	 * @param conn the place to add the namespaces
	 *
	 */
	public static void initNamespaces( ImportData conn ) {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( RDF.PREFIX, RDF.NAMESPACE );
		namespaces.put( RDFS.PREFIX, RDFS.NAMESPACE );
		namespaces.put( OWL.PREFIX, OWL.NAMESPACE );
		namespaces.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		namespaces.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		namespaces.put( FOAF.PREFIX, FOAF.NAMESPACE );
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		namespaces.put( VAC.PREFIX, VAC.NAMESPACE );
		namespaces.put( SEMOSS.PREFIX, SEMOSS.NAMESPACE );

		conn.getMetadata().setNamespaces( namespaces );
	}

	/**
	 * Imports the file into the given repository connection. It is not necessary
	 * for this function to call {@link RepositoryConnection#commit()}, as the
	 * caller will do so upon completion.
	 *
	 * @param f the file to load
	 * @param rcOWL the connection to load it into
	 *
	 * @throws java.io.IOException
	 * @throws RepositoryException
	 */
	protected abstract void importOneFile( File f, RepositoryConnection rcOWL )
			throws IOException, RepositoryException;

	protected static URI getUriFromRawString( String raw, ImportData id ) {
		//resolve namespace
		ValueFactory vf = new ValueFactoryImpl();
		URI uri = null;

		if ( raw.startsWith( "<" ) && raw.endsWith( ">" ) ) {
			uri = vf.createURI( raw.substring( 1, raw.length() - 1 ) );
			return uri;
		}

		// if raw starts with <something>://, then assume it's just a URI
		Matcher m = URISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return vf.createURI( raw );
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = id.getMetadata().getNamespaces().get( pieces[0] );
				if ( null == namespace || namespace.trim().isEmpty() ) {
					logger.warn( "No namespace found for raw value: " + raw );
				}
				else {
					uri = vf.createURI( namespace, pieces[1] );
				}
			}
			else {
				logger.error( "cannot resolve namespace for: " + raw + " (too many colons)" );
			}
		}
		else {
			uri = vf.createURI( raw );
		}

		return uri;
	}

	protected static Value getRDFStringValue( String rawval, ImportData id, ValueFactory vf ) {
		// if rawval looks like a URI, assume it is
		Matcher urimatcher = URISTARTPATTERN.matcher( rawval );
		if ( urimatcher.matches() ) {
			return vf.createURI( rawval );
		}

		Matcher m = NAMEPATTERN.matcher( rawval );
		String val;
		String lang;
		if ( m.matches() ) {
			String g1 = m.group( 1 );
			String g2 = m.group( 2 );
			val = ( null == g1 ? g2 : g1 );
			lang = m.group( 3 );
		}
		else {
			val = rawval;
			lang = "";

			m = DTPATTERN.matcher( rawval );
			if ( m.matches() ) {
				val = m.group( 1 );
				String typestr = m.group( 2 );
				try {
					URI type = getUriFromRawString( typestr, id );
					return vf.createLiteral( val, type );
				}
				catch ( Exception e ) {
					logger.warn( "probably misinterpreting as string(unknown type URI?) :"
							+ rawval, e );
				}
			}
		}

		return ( lang.isEmpty() ? vf.createLiteral( val )
				: vf.createLiteral( val, lang ) );
	}
}
