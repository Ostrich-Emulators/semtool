package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.URIImpl;

/**
 * Minimal vocabulary to support VA SEMOSS Insights.
 *
 */
public class VAC {

	/**
	 * V-CAMP SEMOSS Tool Base URI: http://semoss.org/ontologies
	 */
	public final static String BASE_URI = "http://va.gov/ontologies/core";

	/**
	 * V-CAMP SEMOSS Tool schema namespace: http://va.gov/ontologies/semoss#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the V-CAMP SEMOSS Tool namespace: "vas"
	 */
	public final static String PREFIX = "vac";

	public final static URI SOFTWARE_AGENT = new URIImpl( NAMESPACE + "softwareAgent" );

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

}
