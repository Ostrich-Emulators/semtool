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
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker.RelationCacheKey;
import static com.ostrichemulators.semtool.util.RDFDatatypeTools.getUriFromRawString;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class SemtoolEdgeModeler extends AbstractEdgeModeler {

	private static final Logger log = Logger.getLogger( SemtoolEdgeModeler.class );

	public SemtoolEdgeModeler( QaChecker qa ) {
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
		URI relclass = getCachedRelationClass( relname );

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

		URI connector = null;
		if ( relIsAlreadyUri ) {
			connector = getUriFromRawString( relname, namespaces );
			cacheRelationClass( connector, relname );
		}

		if ( !hasCachedRelation( connectorkey ) ) {
			if ( nap.isEmpty() ) {
				connector = metas.getDataBuilder().build( relname );
				connector = ensureUnique( connector );
				myrc.add( connector, RDFS.SUBCLASSOF, relclass );
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
		else {
			connector = getCachedRelation( connectorkey );
		}

		myrc.add( subject, connector, object );

		// we're going to be adding properties to our new edge type
		if ( metas.isAutocreateMetamodel() && !nap.isEmpty() ) {
			ValueFactory vf = myrc.getValueFactory();

			// our new edge is the same as our old type
			myrc.add( connector, RDFS.SUBCLASSOF, relclass );
			myrc.add( connector, RDFS.LABEL, vf.createLiteral( relname ) );
		}

		addProperties( connector, nap, namespaces, sheet, metas, myrc );

		return connector;
	}
}
