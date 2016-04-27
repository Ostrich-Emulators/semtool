package com.ostrichemulators.semtool.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.URIImpl;

/**
 * Minimal vocabulary to support Insights.
 *
 */
public class VAC {

	public final static String BASE_URI = "http://os-em.com/ontologies/core";

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "vac";

	public final static URI SOFTWARE_AGENT = new URIImpl( NAMESPACE + "softwareAgent" );

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

}
