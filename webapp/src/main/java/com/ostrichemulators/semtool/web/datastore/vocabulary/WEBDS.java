package com.ostrichemulators.semtool.web.datastore.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 * Vocabulary of the SEMOSS Metamodel.
 *
 */
public class WEBDS {

	/**
	 * The SEMOSS Web server Metamodel Base URI: http://web.semoss.org/ontologies
	 */
	public final static String BASE_URI = "http://web.semoss.org/ontologies";

	public final static String NAMESPACE = BASE_URI + "/";

	/**
	 * Recommend prefix for this namespace
	 */
	public final static String PREFIX = "semossweb";

	/**
	 * An immutable {@link Namespace} constant that represents the SEMOSS
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

	public final static URI DBINFO;

	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();

		// ----- Classes ------
		DBINFO = factory.createURI( NAMESPACE, "DbInfo" );
	}
}
