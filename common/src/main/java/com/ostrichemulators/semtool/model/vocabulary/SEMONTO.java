package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SEMONTO {

	/**
	 * The SEMONTO Metamodel Base IRI: http://semoss.org/ontologies
	 */
	public final static String BASE_IRI = "http://os-em.com/ontologies/semtool";

	/**
	 * The SEMONTO Metamodel schema namespace: http://semoss.org/ontologies/
	 */
	public final static String NAMESPACE = BASE_IRI + "/";

	/**
	 * Recommend prefix for the V-CAMP SEMONTO Tool namespace: "semoss"
	 */
	public final static String PREFIX = "semonto";

	/**
	 * An immutable {@link Namespace} constant that represents the SEMONTO
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace( PREFIX, NAMESPACE );

	// ----- Classes ------
	/**
	 * http://semoss.org/ontologies/Concept
	 */
	public final static IRI Concept;

	/**
	 * http://semoss.org/ontologies/Relation
	 */
	public final static IRI Relation;

	// ----- Properties ------
	/**
	 * http://semoss.org/ontologies/has
	 */
	public static final IRI has;

	static {
		final ValueFactory factory = SimpleValueFactory.getInstance();

		// ----- Classes ------
		Concept = factory.createIRI( NAMESPACE, "Concept" );
		Relation = factory.createIRI( NAMESPACE, "Relation" );

		// ----- Properties ------
		has = factory.createIRI( NAMESPACE, "has" );
	}
}
