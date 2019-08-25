package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Minimal vocabulary of the Ordered List Ontology.
 *
 */
public class OLO {

	/**
	 * Ordered List Ontology Base URI: http://purl.org/ontology/olo/core
	 */
	public final static String BASE_URI = "http://purl.org/ontology/olo/core";

	/**
	 * V-CAMP SEMOSS Tool namespace: http://purl.org/ontology/olo/core#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the V-CAMP SEMOSS Tool namespace: "vst"
	 */
	public final static String PREFIX = "olo";

	/**
	 * An immutable {@link Namespace} constant that represents the V-CAMP SEMOSS
	 * Tool namespace.
	 */
	public static final Namespace NS = new SimpleNamespace( PREFIX, NAMESPACE );

	// ----- Properties ------
	/**
	 * http://purl.org/ontology/olo/core#slot
	 */
	public final static IRI slot;

	/**
	 * http://purl.org/ontology/olo/core#item
	 */
	public final static IRI item;

	/**
	 * http://purl.org/ontology/olo/core#index
	 */
	public final static IRI index;

	static {
		final ValueFactory factory = SimpleValueFactory.getInstance();

		// ----- Properties ------
		slot = factory.createIRI( NAMESPACE, "slot" );
		item = factory.createIRI( NAMESPACE, "item" );
		index = factory.createIRI( NAMESPACE, "index" );
	}
}
