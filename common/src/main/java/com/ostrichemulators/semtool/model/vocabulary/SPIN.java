package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


/**
 * Minimal vocabulary of the SPIN Modeling Vocabulary.
 *
 */
public class SPIN {

	/**
	 * SPIN SPARQL Syntax Base URI: hhttp://uispin.org/ui
	 */	
	public final static String BASE_URI = "http://spinrdf.org/spin";
	
	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/spin#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN SPARQL Syntax schema namespace: "spin"
	 */
	public final static String PREFIX = "spin";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN
	 * Modeling Vocabulary namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);


	// ----- Classes ------

	/**
	 * http://spinrdf.org/spin#Function
	 */
    public final static IRI Function;

	/**
	 * http://spinrdf.org/spin#MagicProperties
	 */
    
    public final static IRI MagicProperty;
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/spin#body
	 */
	public final static IRI body;
    
	/**
	 * http://spinrdf.org/spin#constraint
	 */
	public final static IRI constraint;
	
	
	static {
		final ValueFactory factory = SimpleValueFactory.getInstance();
		
		// ----- Classes ------
		Function = factory.createIRI(NAMESPACE, "Function");
		
		MagicProperty = factory.createIRI(NAMESPACE, "MagicProperty");

		// ----- Properties ------
		body = factory.createIRI(NAMESPACE, "body");
		
		constraint = factory.createIRI(NAMESPACE, "constraint");
	}
}
