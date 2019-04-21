package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;

/**
 * Minimal vocabulary to support Insights.
 *
 */
public class SEMCORE {

	public final static String BASE_URI = "http://os-em.com/ontologies/semtool/core";

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "semcore";

	public final static URI SOFTWARE_AGENT = new URIImpl( NAMESPACE + "softwareAgent" );

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

}
