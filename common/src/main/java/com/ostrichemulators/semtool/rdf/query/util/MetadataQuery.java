/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import org.openrdf.model.Value;

/**
 * A Query Adapter that returns the metadata for an engine. The metadata is in
 * the form of a map of URI =&gt; String. For simplicity, this map contains a
 * key of {@link MetadataConstants#VOID_DS} pointing to the baseuri of the
 * metadata.
 *
 * @author ryan
 */
public class MetadataQuery extends QueryExecutorAdapter<Map<URI, Value>> {

	public MetadataQuery() {
		super( "SELECT ?db ?p ?o WHERE { ?db a ?dataset . ?db ?p ?o }" );
		result = new HashMap<>();
		bind("dataset", SEMTOOL.Database );
	}

	public MetadataQuery( URI uri ) {
		super( "SELECT ?db ?p ?o WHERE { ?db a ?dataset . ?db ?p ?o }" );
		result = new HashMap<>();
		bind("dataset", SEMTOOL.Database );
		// special handling when we're trying to figure out the base uri
		bind("p", ( SEMTOOL.Database.equals( uri ) ? RDF.TYPE : uri ) );
	}

	public Map<URI, String> asStrings() {
		Map<URI, String> vals = new HashMap<>();

		for ( Map.Entry<URI, Value> en : super.getResults().entrySet() ) {
			vals.put( en.getKey(), en.getValue().stringValue() );
		}

		return vals;
	}

	/**
	 * Returns the one value desired, if this query was given a URI at
	 * instantiation, or the first (random) key from the result map, or null
	 *
	 * @return the first (random) key of the result map, or null if the map is
	 * empty
	 */
	public Value getOne() {
		// if we used the one-URI ctor, get the (only) value, or null
		return ( result.isEmpty() ? null : result.values().iterator().next() );
	}

	public String getString() {
		// if we used the one-URI ctor, get the (only) value, or null
		Value v = getOne();
		return ( null == v ? null : v.stringValue() );
	}

	@Override
	public void handleTuple( BindingSet set, ValueFactory fac ) {
		URI pred = fac.createURI( set.getValue( "p" ).stringValue() );
		Value val = set.getValue( "o" );

		// for baseuri, we need the subject, not the object
		// and also, we use the VOID_DS as the key elsewhere in the code
		if ( RDF.TYPE.equals( pred ) ) {
			pred = SEMTOOL.Database;
			val = set.getValue( "db" );
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
}
