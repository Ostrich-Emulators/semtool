package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Minimal vocabulary to support Insights.
 *
 */
public class SEMCORE {

	public final static String BASE_URI = "http://os-em.com/ontologies/semtool/core";

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "semcore";

	public final static IRI SOFTWARE_AGENT = SimpleValueFactory.getInstance().createIRI( NAMESPACE, "softwareAgent" );

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace( PREFIX, NAMESPACE );

}
