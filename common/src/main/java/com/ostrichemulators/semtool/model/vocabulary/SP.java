package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Minimal vocabulary of the SPIN SPARQL Syntax schema.
 *
 */
public class SP {

	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/spin#
	 */
	public final static String BASE_URI = "http://spinrdf.org/sp";

	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/spl#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN SPARQL Syntax schema namespace: "sp"
	 */
	public final static String PREFIX = "sp";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN SPARQL
	 * Syntax schema namespace.
	 */
	public static final Namespace NS = new SimpleNamespace( PREFIX, NAMESPACE );

	// ----- Classes ------
	/**
	 * http://spinrdf.org/sp#Construct
	 */
	public final static IRI Construct;

	/**
	 * http://spinrdf.org/sp#Select
	 */
	public final static IRI Select;

	/**
	 * http://spinrdf.org/sp#Describe
	 */
	public final static IRI Describe;

	// ----- Properties ------
	/**
	 * http://spinrdf.org/sp#text
	 */
	public final static IRI text;

	/**
	 * http://spinrdf.org/sp#query
	 */
	public final static IRI query;

	static {
		final ValueFactory factory = SimpleValueFactory.getInstance();

		// ----- Classes ------
		Construct = factory.createIRI( NAMESPACE, "Construct" );

		Select = factory.createIRI( NAMESPACE, "Select" );
		Describe = factory.createIRI( NAMESPACE, "Describe" );

		// ----- Properties ------
		text = factory.createIRI( NAMESPACE, "text" );

		query = factory.createIRI( NAMESPACE, "query" );
	}
}
