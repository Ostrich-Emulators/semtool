package com.ostrichemulators.semtool.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.URIImpl;

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
