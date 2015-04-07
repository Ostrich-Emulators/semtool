/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util;

import gov.va.semoss.rdf.engine.api.IEngine;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * A Query Adapter that returns the metadata for an engine. The metadata is in
 * the form of a map of URI =&gt; String. For simplicity, this map contains a
 * key of {@link MetadataConstants#VOID_DS} pointing to the baseuri of the
 * metadata.
 *
 * @author ryan
 */
public class MetadataQuery extends QueryExecutorAdapter<Map<URI, String>> {

	private static final SimpleDateFormat SDF
			= new SimpleDateFormat( "yyyy-mm-dd'T'hh:MM:ss.SSS'Z'" );

	public MetadataQuery() {
		super( "SELECT ?db ?p ?o WHERE { ?db a ?dataset . ?db ?p ?o }" );
		result = new HashMap<>();
		bind( "dataset", MetadataConstants.VOID_DS );
	}

	public MetadataQuery( URI uri ) {
		super( "SELECT ?db ?p ?o WHERE { ?db a ?dataset . ?db ?p ?o }" );
		result = new HashMap<>();
		bind( "dataset", MetadataConstants.VOID_DS );
		// special handling when we're trying to figure out the base uri
		bind( "p", ( MetadataConstants.VOID_DS.equals( uri ) ? RDF.TYPE : uri ) );
	}

	/**
	 * Returns the one value desired, if this query was given a URI at
	 * instantiation, or the first (random) key from the result map, or null
	 *
	 * @return the first (random) key of the result map, or null if the map is
	 * empty
	 */
	public String getOne() {
		// if we used the one-URI ctor, get the (only) value, or null
		return ( result.isEmpty() ? null : result.values().iterator().next() );
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		URI pred = fac.createURI( set.getValue( "p" ).stringValue() );
		String val = set.getValue( "o" ).stringValue();

    // for baseuri, we need the subject, not the object
		// and also, we use the VOID_DS as the key elsewhere in the code
		if ( RDF.TYPE.equals( pred ) ) {
			pred = MetadataConstants.VOID_DS;
			val = set.getValue( "db" ).stringValue();
		}
		else if ( pred.getNamespace().equals( DC.NAMESPACE ) ) {
			// silently handle the old DC namespace (ignore our DC-specific URIs)
			if ( !( MetadataConstants.DCT_CREATED.equals( pred )
					|| MetadataConstants.DCT_MODIFIED.equals( pred ) ) ) {
				pred = fac.createURI( DCTERMS.NAMESPACE, pred.getLocalName() );
			}
		}

		result.put( pred, val );
	}

	public static String getEngineLabel( IEngine engine ) {
		String label = engine.getEngineName();
		MetadataQuery mq = new MetadataQuery( RDFS.LABEL );
		try {
			engine.query( mq );
			String str = mq.getOne();
			if( null != str ){
				label = str;
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			// don't care
		}
		return label;
	}
}
