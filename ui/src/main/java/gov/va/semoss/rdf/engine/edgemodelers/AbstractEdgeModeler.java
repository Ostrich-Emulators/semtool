/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.model.vocabulary.SEMOSS;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.ImportValidationException;
import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.rdf.engine.util.QaChecker;
import gov.va.semoss.rdf.engine.util.QaChecker.RelationCacheKey;
import static gov.va.semoss.util.RDFDatatypeTools.URISTARTPATTERN;
import static gov.va.semoss.util.RDFDatatypeTools.getRDFStringValue;
import static gov.va.semoss.util.RDFDatatypeTools.getUriFromRawString;
import gov.va.semoss.util.UriBuilder;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public abstract class AbstractEdgeModeler implements EdgeModeler {

	private static final Logger log = Logger.getLogger( AbstractEdgeModeler.class );
	private final Set<URI> duplicates;
	private QaChecker qaer;

	public AbstractEdgeModeler() {
		this( new QaChecker() );
	}

	public AbstractEdgeModeler( QaChecker qa ) {
		qaer = qa;
		duplicates = qaer.getKnownUris();
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
			ImportMetadata metas, RepositoryConnection myrc, boolean checkCacheFirst )
			throws RepositoryException {

		boolean nodeIsAlreadyUri = isUri( rawlabel, namespaces );

		if ( nodeIsAlreadyUri ) {
			URI subject = getUriFromRawString( rawlabel, namespaces );
			cacheInstance( subject, typename, rawlabel );
		}
		else {
			if ( ( checkCacheFirst && !hasCachedInstance( typename, rawlabel ) )
					|| !checkCacheFirst ) {
				URI subject = ( nodeIsAlreadyUri
						? getUriFromRawString( rawlabel, namespaces )
						: metas.getDataBuilder().add( rawlabel ).build() );
				subject = ensureUnique( subject );
				cacheInstance( subject, typename, rawlabel );
			}
		}

		URI subject = getCachedInstance( typename, rawlabel );
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
	public URI addNode( LoadingSheetData.LoadingNodeAndPropertyValues nap,
			Map<String, String> namespaces, LoadingSheetData sheet, ImportMetadata metas,
			RepositoryConnection myrc ) throws RepositoryException {

		String typename = nap.getSubjectType();
		String rawlabel = nap.getSubject();

		URI subject = addSimpleNode( typename, rawlabel, namespaces, metas, myrc, true );

		ValueFactory vf = myrc.getValueFactory();
		boolean savelabel = metas.isAutocreateMetamodel();
		if ( rawlabel.contains( ":" ) ) {
			// we have something with a colon in it, so we need to figure out if it's
			// a namespace-prefixed string, or just a string with a colon in it

			Value val = getRDFStringValue( rawlabel, namespaces, vf );
			// check if we have a prefixed URI
			URI u = getUriFromRawString( rawlabel, namespaces );
			savelabel = ( savelabel && null == u );
			rawlabel = val.stringValue();
		}

		// if we have a label property, skip this label-making
		// (it'll get handled in the addProperties function later)
		if ( savelabel && !nap.hasProperty( RDFS.LABEL, namespaces ) ) {
			myrc.add( subject, RDFS.LABEL, vf.createLiteral( rawlabel ) );
		}

		addProperties( subject, nap, namespaces, sheet, metas, myrc );

		return subject;
	}

	@Override
	public void addProperties( URI subject, Map<String, Value> properties,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection myrc )
			throws RepositoryException {

		for ( Map.Entry<String, Value> entry : properties.entrySet() ) {
			String propname = entry.getKey();
			URI predicate = getCachedPropertyClass( propname );

			Value value = entry.getValue();
			if ( sheet.isLink( propname ) ) {
				// our "value" is really the label of another node, so find that node
				value = addSimpleNode( propname, value.stringValue(), namespaces,
						metas, myrc, true );
				predicate = getCachedRelationClass( sheet.getSubjectType()
						+ sheet.getObjectType() + propname );
			}

			myrc.add( subject, predicate, value );
		}
	}

	@Override
	public void createMetamodel( ImportData alldata, Map<String, String> namespaces,
			RepositoryConnection myrc ) throws RepositoryException {
		ImportMetadata metas = alldata.getMetadata();
		UriBuilder schema = metas.getSchemaBuilder();
		boolean save = metas.isAutocreateMetamodel();

		ValueFactory vf = myrc.getValueFactory();

		for ( LoadingSheetData sheet : alldata.getSheets() ) {
			String stype = sheet.getSubjectType();
			if ( !hasCachedInstanceClass( stype ) ) {
				boolean nodeAlreadyMade = isUri( stype, namespaces );

				URI uri = ( nodeAlreadyMade
						? getUriFromRawString( stype, namespaces )
						: schema.build( stype ) );
				cacheInstanceClass( uri, stype );

				if ( save && !nodeAlreadyMade ) {
					myrc.add( uri, RDF.TYPE, OWL.CLASS );
					myrc.add( uri, RDFS.LABEL, vf.createLiteral( stype ) );
					myrc.add( uri, RDFS.SUBCLASSOF, schema.getConceptUri().build() );
				}
			}

			if ( sheet.isRel() ) {
				String otype = sheet.getObjectType();
				if ( !hasCachedInstanceClass( otype ) ) {
					boolean nodeAlreadyMade = isUri( otype, namespaces );

					URI uri = ( nodeAlreadyMade
							? getUriFromRawString( otype, namespaces )
							: schema.build( otype ) );

					cacheInstanceClass( uri, otype );

					if ( save && !nodeAlreadyMade ) {
						myrc.add( uri, RDF.TYPE, OWL.CLASS );
						myrc.add( uri, RDFS.LABEL, vf.createLiteral( otype ) );
						myrc.add( uri, RDFS.SUBCLASSOF, schema.getConceptUri().build() );
					}
				}

				String rellabel = sheet.getRelname();

				if ( !hasCachedRelationClass( rellabel ) ) {
					boolean relationAlreadyMade = isUri( rellabel, namespaces );

					URI ret = ( relationAlreadyMade
							? getUriFromRawString( rellabel, namespaces )
							: schema.build( rellabel ) );
					cacheRelationClass( ret, rellabel );

					if ( save && !relationAlreadyMade ) {
						myrc.add( ret, RDFS.LABEL, vf.createLiteral( rellabel ) );
						myrc.add( ret, RDF.TYPE, OWL.OBJECTPROPERTY );
					}
				}
			}
		}

		for ( LoadingSheetData sheet : alldata.getSheets() ) {
			for ( String propname : sheet.getProperties() ) {
				// check to see if we're actually a link to some
				// other node (and not really a new property
				if ( sheet.isLink( propname ) || hasCachedInstanceClass( propname ) ) {
					log.debug( "linking " + propname + " as a " + SEMOSS.has
							+ " relationship to " + getCachedInstanceClass( propname ) );

					cacheRelationClass( SEMOSS.has,
							sheet.getSubjectType() + sheet.getObjectType() + propname );
					continue;
				}

				boolean alreadyMadeProp = isUri( propname, namespaces );

				if ( !hasCachedPropertyClass( propname ) ) {
					URI predicate = ( alreadyMadeProp
							? getUriFromRawString( propname, namespaces )
							: schema.build( propname ) );
					cachePropertyClass( predicate, propname );
				}

				URI predicate = getCachedPropertyClass( propname );

				if ( save && !alreadyMadeProp ) {
					myrc.add( predicate, RDFS.LABEL, vf.createLiteral( propname ) );
					myrc.add( predicate, RDF.TYPE, OWL.DATATYPEPROPERTY );
				}
			}
		}
	}

	@Override
	public void setQaChecker( QaChecker q ) {
		qaer = q;
	}

	public URI getCachedRelation( RelationCacheKey key ) {
		return qaer.getCachedRelation( key );
	}

	public URI getCachedInstance( String typename, String rawlabel ) {
		return qaer.getCachedInstance( typename, rawlabel );
	}

	public URI getCachedInstanceClass( String name ) {
		return qaer.getCachedInstanceClass( name );
	}

	public URI getCachedRelationClass( String rel ) {
		return qaer.getCachedRelationClass( rel );
	}

	public URI getCachedPropertyClass( String name ) {
		return qaer.getCachedPropertyClass( name );
	}

	public boolean hasCachedPropertyClass( String name ) {
		return qaer.hasCachedPropertyClass( name );
	}

	public boolean hasCachedRelationClass( String rel ) {
		return qaer.hasCachedRelationClass( rel );
	}

	public boolean hasCachedRelation( String stype, String otype, String relname,
			String slabel, String olabel ) {
		return qaer.hasCachedRelation( stype, otype, relname, slabel, olabel );
	}

	public boolean hasCachedRelation( RelationCacheKey key ) {
		return qaer.hasCachedRelation( key );
	}

	public boolean hasCachedInstance( String typename, String rawlabel ) {
		return qaer.hasCachedInstance( typename, rawlabel );
	}

	public boolean hasCachedInstanceClass( String name ) {
		return qaer.hasCachedInstanceClass( name );
	}

	public void cacheInstanceClass( URI uri, String label ) {
		qaer.cacheInstanceClass( uri, label );
		duplicates.add( uri );
	}

	public void cacheRelationNode( URI uri, String stype, String otype,
			String relname, String slabel, String olabel ) {
		qaer.cacheRelationNode( uri, stype, otype, relname, slabel, olabel );
		duplicates.add( uri );
	}

	public void cacheRelationNode( URI uri, RelationCacheKey key ) {
		qaer.cacheRelationNode( uri, key );
		duplicates.add( uri );
	}

	public void cacheRelationClass( URI uri, String rel ) {
		qaer.cacheRelationClass( uri, rel );
		duplicates.add( uri );
	}

	public void cacheInstance( URI uri, String typelabel, String rawlabel ) {
		qaer.cacheInstance( uri, typelabel, rawlabel );
		duplicates.add( uri );
	}

	public void cachePropertyClass( URI uri, String name ) {
		qaer.cachePropertyClass( uri, name );
		duplicates.add( uri );
	}
}
