/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.rdf.engine.util.QaChecker;
import gov.va.semoss.rdf.engine.util.QaChecker.RelationCacheKey;
import static gov.va.semoss.util.RDFDatatypeTools.getUriFromRawString;
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
public class SemossEdgeModeler extends AbstractEdgeModeler {

	private static final Logger log = Logger.getLogger( SemossEdgeModeler.class );

	public SemossEdgeModeler( QaChecker qa ) {
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

		boolean relIsAlreadyUri = isUri( relname, namespaces );

		// ... and get a relationship that ties them together
		RelationCacheKey connectorkey = new RelationCacheKey( nap.getSubjectType(),
				nap.getObjectType(), relname, nap.getSubject(), nap.getObject() );

		if ( relIsAlreadyUri ) {
			URI connector = getUriFromRawString( relname, namespaces );
			cacheRelationClass( connector, relname );
		}

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

			myrc.add( connector, RDF.TYPE, metas.getSchemaBuilder().getRelationUri().build() );
			myrc.add( connector, RDFS.LABEL, vf.createLiteral( srawlabel + " "
					+ sheet.getRelname() + " " + orawlabel ) );
			URI pred = getCachedRelationClass( sheet.getRelname() );
			myrc.add( connector, RDF.PREDICATE, pred );
		}

		myrc.add( subject, connector, object );

		addProperties( connector, nap, namespaces, sheet, metas, myrc );

		return connector;
	}
}
