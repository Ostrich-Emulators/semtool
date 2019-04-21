package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 * Minimal vocabulary of the SPIN Standard Modules Library.
 *
 */
public class SPL {

	/**
	 * SPIN Standard Modules Library Base URI: hhttp://uispin.org/ui
	 */
	public final static String BASE_URI = "http://spinrdf.org/spl";
	
	/**
	 * SPIN Standard Modules Library schema namespace: http://spinrdf.org/spl#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN Standard Modules Library namespace: "spl"
	 */
	public final static String PREFIX = "spl";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN Standard
	 * Modules Library namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);


	// ----- Classes ------
	
	/**
	 * http://spinrdf.org/spl#Argument
	 */
    public final static URI Argument;
	
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/spl#predicate
	 */
	public final static URI predicate;
	public final static URI valueType;
	public static final URI defaultValue;
	
	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Argument = factory.createURI(NAMESPACE, "Argument");
		
		// ----- Properties ------
		predicate = factory.createURI(NAMESPACE, "predicate");
		
		valueType = factory.createURI(NAMESPACE, "valueType");
		
		defaultValue = factory.createURI(NAMESPACE, "defaultValue" );
		
	}
}
