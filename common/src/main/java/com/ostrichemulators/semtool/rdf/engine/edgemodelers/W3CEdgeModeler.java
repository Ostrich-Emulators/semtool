/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.edgemodelers;

import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class W3CEdgeModeler extends AbstractEdgeModeler {

	private static final Logger log = Logger.getLogger( W3CEdgeModeler.class );

	public W3CEdgeModeler( QaChecker qa ) {
		super( qa );
	}

	@Override
	public URI addRel( LoadingNodeAndPropertyValues nap, Map<String, String> namespaces,
			LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection myrc )
			throws RepositoryException {

		final String stype = nap.getSubjectType();
		final String srawlabel = nap.getSubject();

		final String otype = nap.getObjectType();
		final String orawlabel = nap.getObject();

		final String relname = sheet.getRelname();

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

		// ... and get a relationship that ties them together
		QaChecker.RelationCacheKey connectorkey = new QaChecker.RelationCacheKey( nap.getSubjectType(),
				nap.getObjectType(), sheet.getRelname(), nap.getSubject(), nap.getObject() );

		if ( !hasCachedRelation( connectorkey ) ) {
			URI connector = null;
			if ( nap.isEmpty() ) {
				connector = getCachedRelationClass( relname );
			}
			else {
				// make a new edge so we can add properties
				String rellocalname = srawlabel + "_" + sheet.getRelname() + "_"
						+ orawlabel;
				connector = metas.getDataBuilder().build( rellocalname );
				connector = ensureUnique( connector );
			}

			cacheRelationNode( connector, connectorkey );
		}

		myrc.add( subject, getCachedRelationClass( relname ), object );

		URI connector = getCachedRelation( connectorkey );
		if ( metas.isAutocreateMetamodel() && !nap.isEmpty() ) {
			ValueFactory vf = myrc.getValueFactory();

			myrc.add( connector, RDF.TYPE, RDF.STATEMENT );
			myrc.add( connector, RDFS.LABEL, vf.createLiteral( srawlabel + " "
					+ sheet.getRelname() + " " + orawlabel ) );

			URI pred = getCachedRelationClass( sheet.getRelname() );

			myrc.add( connector, RDF.SUBJECT, subject );
			myrc.add( connector, RDF.PREDICATE, pred );
			myrc.add( connector, RDF.OBJECT, object );
		}

		myrc.add( subject, connector, object );

		addProperties( connector, nap, namespaces, sheet, metas, myrc );

		return connector;
	}
}
