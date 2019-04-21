package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;


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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);


	// ----- Classes ------

	/**
	 * http://spinrdf.org/spin#Function
	 */
    public final static URI Function;

	/**
	 * http://spinrdf.org/spin#MagicProperties
	 */
    
    public final static URI MagicProperty;
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/spin#body
	 */
	public final static URI body;
    
	/**
	 * http://spinrdf.org/spin#constraint
	 */
	public final static URI constraint;
	
	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Function = factory.createURI(NAMESPACE, "Function");
		
		MagicProperty = factory.createURI(NAMESPACE, "MagicProperty");

		// ----- Properties ------
		body = factory.createURI(NAMESPACE, "body");
		
		constraint = factory.createURI(NAMESPACE, "constraint");
	}
}
