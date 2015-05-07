/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.model.vocabulary.SEMOSS;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import static gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler.getUriFromRawString;
import static gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler.isUri;
import gov.va.semoss.rdf.engine.util.QaChecker;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import java.util.Map;
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
public class SemossEdgeModeler extends AbstractEdgeModeler {

	private static final Logger log = Logger.getLogger( SemossEdgeModeler.class );

	public SemossEdgeModeler() {
	}

	public SemossEdgeModeler( QaChecker qa ) {
		super( qa );
	}

	@Override
	public URI addRel( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
			LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection myrc )
			throws RepositoryException {

		String stype = nap.getSubjectType();
		String srawlabel = nap.getSubject();

		String otype = nap.getObjectType();
		String orawlabel = nap.getObject();

		// get both ends of the relationship...
		if ( !hasCachedInstance( stype, srawlabel ) ) {
			LoadingNodeAndPropertyValues filler
					= sheet.new LoadingNodeAndPropertyValues( srawlabel );
			addNode( filler, namespaces, sheet, metas, myrc );
		}
		URI subject = getCachedInstance( stype, srawlabel );

		if ( !hasCachedInstance( otype, orawlabel ) ) {
			LoadingSheetData lsd = LoadingSheetData.nodesheet( sheet.getName(), otype );
			LoadingNodeAndPropertyValues filler = lsd.add( orawlabel );
			addNode( filler, namespaces, lsd, metas, myrc );
		}
		URI object = getCachedInstance( otype, orawlabel );

		boolean alreadyMadeRel = isUri( sheet.getRelname(), namespaces );

		// ... and get a relationship that ties them together
		StringBuilder keybuilder = new StringBuilder( nap.getSubjectType() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( nap.getSubject() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( sheet.getRelname() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( nap.getObjectType() );
		keybuilder.append( Constants.RELATION_LABEL_CONCATENATOR );
		keybuilder.append( nap.getObject() );

		String lkey = keybuilder.toString();
		if ( !hasCachedRelation( lkey ) ) {
			URI connector;
			String rellocalname;
			if ( alreadyMadeRel ) {
				rellocalname = srawlabel + Constants.RELATION_URI_CONCATENATOR + orawlabel;
				connector = metas.getDataBuilder().getRelationUri().build( rellocalname );
			}
			else {
				UriBuilder typebuilder
						= metas.getDataBuilder().getRelationUri().add( sheet.getRelname() );
				rellocalname = srawlabel + Constants.RELATION_URI_CONCATENATOR + orawlabel;
				connector = typebuilder.add( rellocalname ).build();
			}

			connector = ensureUnique( connector );
			cacheRelationNode( connector, lkey );
		}

		URI relClassBaseURI = getCachedRelationClass( stype, otype, sheet.getRelname() );

		URI connector = getCachedRelation( lkey );
		if ( metas.isAutocreateMetamodel() ) {
			ValueFactory vf = myrc.getValueFactory();

			myrc.add( connector, RDFS.SUBPROPERTYOF, relClassBaseURI );
			myrc.add( connector, RDFS.LABEL, vf.createLiteral( srawlabel + " "
					+ sheet.getRelname() + " " + orawlabel ) );
		}
		myrc.add( subject, connector, object );

		addProperties( connector, nap, namespaces, sheet, metas, myrc );

		return connector;
	}

	@Override
	public URI addNode( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
			LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection myrc )
			throws RepositoryException {

		String typename = nap.getSubjectType();
		String rawlabel = nap.getSubject();
		URI subject = addSimpleNode( typename, rawlabel, namespaces, metas, myrc );

		ValueFactory vf = myrc.getValueFactory();
		boolean savelabel = metas.isAutocreateMetamodel();
		if ( !metas.isLegacyMode() && rawlabel.contains( ":" ) ) {
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
				value = addSimpleNode( propname, value.stringValue(), namespaces, metas, myrc );
				predicate = getCachedRelationClass( sheet.getSubjectType(),
						sheet.getObjectType(), propname );
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
					}
				}

				String rellabel = sheet.getRelname();

				if ( !hasCachedRelationClass( stype, otype, rellabel ) ) {
					boolean relationAlreadyMade = isUri( rellabel, namespaces );

					URI ret = ( relationAlreadyMade
							? getUriFromRawString( rellabel, namespaces )
							: schema.getRelationUri( rellabel ) );
					URI relation = schema.getRelationUri().build();

					cacheRelationClass( ret, stype, otype, rellabel );

					if ( save && !relationAlreadyMade ) {
						myrc.add( ret, RDFS.LABEL, vf.createLiteral( rellabel ) );
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
							new QaChecker.RelationClassCacheKey( sheet.getSubjectType(),
									sheet.getObjectType(), propname ) );
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
					myrc.add( predicate, RDFS.SUBPROPERTYOF, schema.getRelationUri().build() );

					if ( !metas.isLegacyMode() ) {
						myrc.add( predicate, RDFS.SUBPROPERTYOF, schema.getContainsUri() );
					}
				}
			}
		}
	}
}
