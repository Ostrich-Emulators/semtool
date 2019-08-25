package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Minimal vocabulary of the SPIN Standard Modules Library.
 *
 */
public class SPL {

	/**
	 * SPIN Standard Modules Library Base IRI: hhttp://uispin.org/ui
	 */
	public final static String BASE_IRI = "http://spinrdf.org/spl";
	
	/**
	 * SPIN Standard Modules Library schema namespace: http://spinrdf.org/spl#
	 */
	public final static String NAMESPACE = BASE_IRI + "#";

	/**
	 * Recommend prefix for the SPIN Standard Modules Library namespace: "spl"
	 */
	public final static String PREFIX = "spl";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN Standard
	 * Modules Library namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);


	// ----- Classes ------
	
	/**
	 * http://spinrdf.org/spl#Argument
	 */
    public final static IRI Argument;
	
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/spl#predicate
	 */
	public final static IRI predicate;
	public final static IRI valueType;
	public static final IRI defaultValue;
	
	
	static {
		final ValueFactory factory = SimpleValueFactory.getInstance();
		
		// ----- Classes ------
		Argument = factory.createIRI(NAMESPACE, "Argument");
		
		// ----- Properties ------
		predicate = factory.createIRI(NAMESPACE, "predicate");
		
		valueType = factory.createIRI(NAMESPACE, "valueType");
		
		defaultValue = factory.createIRI(NAMESPACE, "defaultValue" );
		
	}
}
