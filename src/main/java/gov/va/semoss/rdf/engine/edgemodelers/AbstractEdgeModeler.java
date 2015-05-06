/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import gov.va.semoss.rdf.engine.util.QaChecker;
import gov.va.semoss.rdf.engine.util.QaChecker.RelationClassCacheKey;
import gov.va.semoss.util.UriBuilder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public abstract class AbstractEdgeModeler implements EdgeModeler {

	private static final Logger log = Logger.getLogger( AbstractEdgeModeler.class );
	protected static final Pattern NAMEPATTERN
			= Pattern.compile( "(?:(?:\"([^\"]+)\")|([^@]+))@([a-z-A-Z]{1,8})" );
	protected static final Pattern DTPATTERN
			= Pattern.compile( "\"([^\\\\^]+)\"\\^\\^(.*)" );
	protected static final Pattern URISTARTPATTERN
			= Pattern.compile( "(^[A-Za-z_-]+://).*" );
	private final Set<URI> duplicates = new HashSet<>();
	private QaChecker qaer;

	public AbstractEdgeModeler() {
		this( new QaChecker() );
	}

	public AbstractEdgeModeler( QaChecker qa ) {
		qaer = qa;
	}

	public static boolean isUri( String raw, Map<String, String> namespaces ) {
		if ( raw.startsWith( "<" ) && raw.endsWith( ">" ) ) {
			raw = raw.substring( 1, raw.length() - 1 );
		}

		Matcher m = URISTARTPATTERN.matcher( raw );
		if ( m.matches() ) {
			return true;
		}

		if ( raw.contains( ":" ) ) {
			String[] pieces = raw.split( ":" );
			if ( 2 == pieces.length ) {
				String namespace = namespaces.get( pieces[0] );
				if ( !( null == namespace || namespace.trim().isEmpty() ) ) {
					return true;
				}
			}
		}
		return false;
	}

	public static URI getUriFromRawString( String raw, Map<String, String> namespaces ) {
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
				String namespace = namespaces.get( pieces[0] );
				if ( null == namespace || namespace.trim().isEmpty() ) {
					log.warn( "No namespace found for raw value: " + raw );
				}
				else {
					uri = vf.createURI( namespace, pieces[1] );
				}
			}
			else {
				log.error( "cannot resolve namespace for: " + raw + " (too many colons)" );
			}
		}
		//else {
		// since this will will always throw an error (it can't be an absolute URI)
		// we'll just return null, as usual
		//uri = vf.createURI( raw );
		//}

		return uri;
	}

	public static Value getRDFStringValue( String rawval, Map<String, String> namespaces,
			ValueFactory vf ) {
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
					URI type = getUriFromRawString( typestr, namespaces );
					if ( null == type ) {
						// will get caught immediately
						throw new NullPointerException( "unknown type URI" );
					}
					else {
						return vf.createLiteral( val, type );
					}
				}
				catch ( Exception e ) {
					log.warn( "probably misinterpreting as string (unknown type URI?) :"
							+ rawval, e );
					val = rawval;
				}
			}
		}

		return ( lang.isEmpty() ? vf.createLiteral( val )
				: vf.createLiteral( val, lang ) );
	}

	/**
	 * Checks that the given ImportMetadata is valid for importing data
	 * (basically, does it have a {@link ImportMetadata#databuilder} set).
	 *
	 * @param metas the data to check
	 * @return true, if the ImportMetadata can be used for importing data
	 */
	public static boolean isValidMetadata( ImportMetadata metas ) {
		return ( null != metas.getDataBuilder() );
	}

	/**
	 * Same as {@link #isValidMetadata(gov.va.semoss.poi.main.ImportMetadata)},
	 * but throw an exception if
	 * {@link #isValidMetadata(gov.va.semoss.poi.main.ImportMetadata)} returns
	 * <code>false</code>
	 *
	 * @param metas the data to check
	 */
	public static void isValidMetadataEx( ImportMetadata metas ) throws ImportValidationException {
		if ( !isValidMetadata( metas ) ) {
			throw new ImportValidationException( ErrorType.MISSING_DATA,
					"Invalid metadata" );
		}
	}

	/**
	 * Adds just a node to the dataset (no properties, nothing else)
	 *
	 * @param typename
	 * @param rawlabel
	 * @param namespaces
	 * @param metas
	 * @param myrc
	 * @return
	 * @throws org.openrdf.repository.RepositoryException
	 */
	protected URI addSimpleNode( String typename, String rawlabel, Map<String, String> namespaces,
			ImportMetadata metas, RepositoryConnection myrc ) throws RepositoryException {

		boolean nodeIsAlreadyUri = isUri( rawlabel, namespaces );

		if ( !qaer.hasCachedInstance( typename, rawlabel ) ) {
			URI subject;

			if ( nodeIsAlreadyUri ) {
				subject = getUriFromRawString( rawlabel, namespaces );
			}
			else {
				if ( metas.isAutocreateMetamodel() ) {
					UriBuilder nodebuilder = metas.getDataBuilder().getConceptUri();
					if ( !typename.contains( ":" ) ) {
						nodebuilder.add( typename );
					}
					subject = nodebuilder.add( rawlabel ).build();
				}
				else {
					subject = metas.getDataBuilder().add( rawlabel ).build();
				}

				subject = ensureUnique( subject );
			}
			qaer.cacheInstance( subject, typename, rawlabel );
		}

		URI subject = qaer.getCachedInstance( typename, rawlabel );
		myrc.add( subject, RDF.TYPE, qaer.getCachedInstanceClass( typename ) );
		return subject;
	}

	protected URI ensureUnique( URI uri ) {
		if ( duplicates.contains( uri ) ) {
			UriBuilder dupefixer = UriBuilder.getBuilder( uri.getNamespace() );
			uri = dupefixer.uniqueUri();
			duplicates.add( uri );
		}
		return uri;
	}

	@Override
	public void setQaChecker( QaChecker q ) {
		qaer = q;
	}

	public URI getCachedRelation( String name ) {
		return qaer.getCachedRelation( name );
	}

	public URI getCachedInstance( String typename, String rawlabel ) {
		return qaer.getCachedInstance( typename, rawlabel );
	}

	public URI getCachedInstanceClass( String name ) {
		return qaer.getCachedInstanceClass( name );
	}

	public URI getCachedRelationClass( String s, String o, String p ) {
		return qaer.getCachedRelationClass( s, o, p );
	}

	public URI getCachedPropertyClass( String name ) {
		return qaer.getCachedPropertyClass( name );
	}

	public boolean hasCachedPropertyClass( String name ) {
		return qaer.hasCachedPropertyClass( name );
	}

	public boolean hasCachedRelationClass( String s, String o, String p ) {
		return qaer.hasCachedRelationClass( s, o, p );
	}

	public boolean hasCachedRelation( String name ) {
		return qaer.hasCachedRelation( name );
	}

	public boolean hasCachedInstance( String typename, String rawlabel ) {
		return qaer.hasCachedInstance( typename, rawlabel );
	}

	public boolean hasCachedInstanceClass( String name ) {
		return qaer.hasCachedInstanceClass( name );
	}

	public void cacheInstanceClass( URI uri, String label ) {
		qaer.cacheInstanceClass( uri, label );
	}

	public void cacheRelationNode( URI uri, String label ) {
		qaer.cacheRelationNode( uri, label );
	}

	public void cacheRelationClass( URI uri, String sub, String obj, String rel ) {
		qaer.cacheRelationClass( uri, sub, obj, rel );
	}

	public void cacheInstance( URI uri, String typelabel, String rawlabel ) {
		qaer.cacheInstance( uri, typelabel, rawlabel );
	}

	public void cachePropertyClass( URI uri, String name ) {
		qaer.cachePropertyClass( uri, name );
	}

	public void cacheRelationClass( URI uri, RelationClassCacheKey key ) {
		qaer.cacheRelationClass( uri, key );
	}
}
