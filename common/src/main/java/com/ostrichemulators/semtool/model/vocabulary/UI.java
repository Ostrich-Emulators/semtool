package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;


/**
 * Minimal vocabulary of the UISPIN Modeling Vocabulary.
 *
 * For now we go with "ui:" as the namespace prefix in keeping with the SPIN namespace convention applied
 * by TopBraid Composer. Later we might move this namespace to "uispin:" if we create our own UI vocabulary
 * for VA SEMOSS.
 */
public class UI {

	/**
	 * UI SPIN Base URI: hhttp://uispin.org/ui
	 */
	public final static String BASE_URI = "http://uispin.org/ui";
	
	/**
	 * UI SPIN schema namespace: http://uispin.org/ui#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the UISPIN Modeling Vocabulary namespace: "ui"
	 */
	public final static String PREFIX = "ui";

	/**
	 * An immutable {@link Namespace} constant that represents the UISPIN 
	 * Modeling Vocabulary namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	
	// ----- Properties ------
    
	/**
	 * http://uispin.org/ui#element
	 */
	public final static URI element;
    
	/**
	 * http://uispin.org/ui#dataView
	 */
	public final static URI dataView;
    
	/**
	 * http://uispin.org/ui#viewClass
	 */
	public final static URI viewClass;
	
	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Properties ------
		element = factory.createURI(NAMESPACE, "element");
		dataView = factory.createURI(NAMESPACE, "dataView");
		viewClass = factory.createURI(NAMESPACE, "viewClass");
	}
}
