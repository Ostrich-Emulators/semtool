package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Minimal vocabulary of the Ordered List Ontology.
 *
 */
public class OLO {

	/**
	 * Ordered List Ontology Base URI: http://purl.org/ontology/olo/core
	 */
	public final static String BASE_URI = "http://purl.org/ontology/olo/core";
	
	/**
	 * V-CAMP SEMOSS Tool namespace: http://purl.org/ontology/olo/core#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the V-CAMP SEMOSS Tool namespace: "vst"
	 */
	public final static String PREFIX = "olo";

	/**
	 * An immutable {@link Namespace} constant that represents the V-CAMP SEMOSS
	 * Tool namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	

	// ----- Properties ------
	
	/**
	 * http://purl.org/ontology/olo/core#slot
	 */
	public final static URI slot;
	
	/**
	 * http://purl.org/ontology/olo/core#item
	 */
	public final static URI item;
	
	/**
	 * http://purl.org/ontology/olo/core#index
	 */
	public final static URI index;

	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Properties ------
		slot  = factory.createURI(NAMESPACE, "slot");
		item  = factory.createURI(NAMESPACE, "item");
		index = factory.createURI(NAMESPACE, "index");
	}
}
