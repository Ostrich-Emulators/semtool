/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.edgemodelers;

import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import static com.ostrichemulators.semtool.rdf.engine.edgemodelers.AbstractEdgeModeler.isUri;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker.RelationCacheKey;
import static com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter.getCal;
import com.ostrichemulators.semtool.util.Constants;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getIriFromRawString;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getRDFStringValue;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class LegacyEdgeModeler extends AbstractEdgeModeler {

	private static final Logger log = Logger.getLogger( LegacyEdgeModeler.class );

	public LegacyEdgeModeler( QaChecker qa ) {
		super( qa );
	}

	@Override
	public IRI addRel( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
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
		IRI subject = getCachedInstance( stype, srawlabel );

		if ( !hasCachedInstance( otype, orawlabel ) ) {
			LoadingSheetData lsd = LoadingSheetData.nodesheet( sheet.getName(), otype );
			LoadingNodeAndPropertyValues filler = lsd.add( orawlabel );
			addNode( filler, namespaces, lsd, metas, myrc );
		}
		IRI object = getCachedInstance( otype, orawlabel );

		boolean alreadyMadeRel = isUri( sheet.getRelname(), namespaces );

		// ... and get a relationship that ties them together
		RelationCacheKey lkey = new RelationCacheKey( nap.getSubjectType(),
				nap.getObjectType(), sheet.getRelname(), nap.getSubject(), nap.getObject() );

		if ( !hasCachedRelation( lkey ) ) {
			IRI connector;
			String rellocalname;
			if ( alreadyMadeRel ) {
				rellocalname = srawlabel + Constants.RELATION_IRI_CONCATENATOR + orawlabel;
				connector = metas.getDataBuilder().getRelationIri().build( rellocalname );
			}
			else {
				UriBuilder typebuilder
						= metas.getDataBuilder().getRelationIri().add( sheet.getRelname() );
				rellocalname = srawlabel + Constants.RELATION_IRI_CONCATENATOR + orawlabel;
				connector = typebuilder.add( rellocalname ).build();
			}

			connector = ensureUnique( connector );
			cacheRelationNode( connector, lkey );
		}

		IRI relClassBaseURI = getCachedRelationClass( sheet.getRelname() );

		IRI connector = getCachedRelation( lkey );
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
	public IRI addNode( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
			LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection myrc ) throws RepositoryException {

		String typename = nap.getSubjectType();
		String rawlabel = nap.getSubject();
		IRI subject = addSimpleNode( typename, rawlabel, namespaces, metas, myrc );

		ValueFactory vf = myrc.getValueFactory();
		boolean savelabel = metas.isAutocreateMetamodel();
		if ( !metas.isLegacyMode() && rawlabel.contains( ":" ) ) {
			// we have something with a colon in it, so we need to figure out if it's
			// a namespace-prefixed string, or just a string with a colon in it

			Value val = getRDFStringValue( rawlabel, namespaces, vf );
			// check if we have a prefixed URI
			IRI u = getIriFromRawString( rawlabel, namespaces );
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
	public void addProperties( IRI subject, Map<String, Value> properties,
			Map<String, String> namespaces, LoadingSheetData sheet,
			ImportMetadata metas, RepositoryConnection myrc ) throws RepositoryException {

		ValueFactory vf = myrc.getValueFactory();

		for ( Map.Entry<String, Value> entry : properties.entrySet() ) {
			String propname = entry.getKey();
			IRI predicate = getCachedPropertyClass( propname );

			Value value = entry.getValue();
			if ( sheet.isLink( propname ) ) {
				// our "value" is really the label of another node, so find that node
				value = addSimpleNode( propname, value.stringValue(), namespaces, metas, myrc );
				predicate = getCachedRelationClass( sheet.getSubjectType()
						+ sheet.getObjectType() + propname );
			}

			// not sure if we even use these values anymore
			switch ( value.toString() ) {
				case Constants.PROCESS_CURRENT_DATE:
					myrc.add( subject, predicate,
							vf.createLiteral( getCal( new Date() ) ) );
					break;
				case Constants.PROCESS_CURRENT_USER:
					myrc.add( subject, predicate,
							vf.createLiteral( System.getProperty( "user.name" ) ) );
					break;
				default:
					myrc.add( subject, predicate, value );
			}
		}
	}

	protected IRI addSimpleNode( String typename, String rawlabel, Map<String, String> namespaces,
			ImportMetadata metas, RepositoryConnection myrc ) throws RepositoryException {

		boolean nodeIsAlreadyUri = isUri( rawlabel, namespaces );

		if ( !hasCachedInstance( typename, rawlabel ) ) {
			IRI subject;

			if ( nodeIsAlreadyUri ) {
				subject = getIriFromRawString( rawlabel, namespaces );
			}
			else {
				if ( metas.isAutocreateMetamodel() ) {
					UriBuilder nodebuilder = metas.getDataBuilder().getConceptIri();
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
			cacheInstance( subject, typename, rawlabel );
		}

		IRI subject = getCachedInstance( typename, rawlabel );
		myrc.add( subject, RDF.TYPE, getCachedInstanceClass( typename ) );
		return subject;
	}

	@Override
	public Model createMetamodel( ImportData alldata, Map<String, String> namespaces,
			ValueFactory vf ) throws RepositoryException {
		Model model = new TreeModel();
		ImportMetadata metas = alldata.getMetadata();
		UriBuilder schema = metas.getSchemaBuilder();
		boolean save = metas.isAutocreateMetamodel();

		if ( null == vf ) {
			vf = SimpleValueFactory.getInstance();
		}

		for ( LoadingSheetData sheet : alldata.getSheets() ) {
			String stype = sheet.getSubjectType();
			if ( !hasCachedInstanceClass( stype ) ) {
				boolean nodeAlreadyMade = isUri( stype, namespaces );

				IRI uri = ( nodeAlreadyMade
						? getIriFromRawString( stype, namespaces )
						: schema.build( stype ) );
				cacheInstanceClass( uri, stype );

				if ( save && !nodeAlreadyMade ) {
					model.add( uri, RDF.TYPE, OWL.CLASS );
					model.add( uri, RDFS.LABEL, vf.createLiteral( stype ) );
					model.add( uri, RDFS.SUBCLASSOF, schema.getConceptIri().build() );
				}
			}

			if ( sheet.isRel() ) {
				String otype = sheet.getObjectType();
				if ( !hasCachedInstanceClass( otype ) ) {
					boolean nodeAlreadyMade = isUri( otype, namespaces );

					IRI uri = ( nodeAlreadyMade
							? getIriFromRawString( otype, namespaces )
							: schema.build( otype ) );

					cacheInstanceClass( uri, otype );

					if ( save && !nodeAlreadyMade ) {
						model.add( uri, RDF.TYPE, OWL.CLASS );
						model.add( uri, RDFS.LABEL, vf.createLiteral( otype ) );
						model.add( uri, RDFS.SUBCLASSOF, schema.getConceptIri().build() );
					}
				}

				String rellabel = sheet.getRelname();

				if ( !hasCachedRelationClass( rellabel ) ) {
					boolean relationAlreadyMade = isUri( rellabel, namespaces );

					IRI ret = ( relationAlreadyMade
							? getIriFromRawString( rellabel, namespaces )
							: schema.getRelationIri( rellabel ) );
					IRI relation = schema.getRelationIri().build();

					cacheRelationClass( ret, rellabel );

					if ( save ) {
						if ( !relationAlreadyMade ) {
							model.add( ret, RDF.TYPE, OWL.OBJECTPROPERTY );
							model.add( ret, RDFS.LABEL, vf.createLiteral( rellabel ) );
							model.add( ret, RDFS.SUBPROPERTYOF, relation );
						}
						// myrc.add( suri, ret, schemaNodes.get( ocachekey ) );

						model.add( schema.getConceptIri().build(), RDF.TYPE, RDFS.CLASS );

						model.add( schema.getContainsIri(), RDFS.SUBPROPERTYOF, schema.getContainsIri() );
						model.add( relation, RDF.TYPE, RDF.PROPERTY );
					}
				}
			}
		}

		for ( LoadingSheetData sheet : alldata.getSheets() ) {
			for ( String propname : sheet.getProperties() ) {
				// check to see if we're actually a link to some
				// other node (and not really a new property
				if ( sheet.isLink( propname ) || hasCachedInstanceClass( propname ) ) {
					log.debug( "linking " + propname + " as a " + SEMONTO.has
							+ " relationship to " + getCachedInstanceClass( propname ) );

					cacheRelationClass( SEMONTO.has, sheet.getSubjectType()
							+ sheet.getObjectType() + propname );
					continue;
				}

				boolean alreadyMadeProp = isUri( propname, namespaces );

				if ( !hasCachedPropertyClass( propname ) ) {
					IRI predicate;
					if ( alreadyMadeProp ) {
						predicate = getIriFromRawString( propname, namespaces );
					}
					else {
						// UriBuilder bb = schema.getRelationIri().add( Constants.CONTAINS );
						predicate = schema.build( propname );
					}
					cachePropertyClass( predicate, propname );
				}
				IRI predicate = getCachedPropertyClass( propname );

				if ( save && !alreadyMadeProp ) {
					model.add( predicate, RDFS.LABEL, vf.createLiteral( propname ) );
					// myrc.add( predicate, RDF.TYPE, schema.getContainsIri() );
					model.add( predicate, RDFS.SUBPROPERTYOF, schema.getRelationIri().build() );

					if ( !metas.isLegacyMode() ) {
						model.add( predicate, RDFS.SUBPROPERTYOF, schema.getContainsIri() );
					}
				}
			}
		}

		return model;
	}
}
