package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;


/**
 * The SPIN Arg namespace.  Only the namespace and prefix need be defined.
 *
 */
public class ARG {

	public final static String BASE_URI = "http://spinrdf.org/arg";
	
	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/arg#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN SPARQL Syntax schema namespace: "arg"
	 */
	public final static String PREFIX = "arg";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN ARG
	 * schema namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

}
