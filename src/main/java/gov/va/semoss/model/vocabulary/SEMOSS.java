package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Minimal vocabulary of the VA SEMOSS Insights.
 *
 */
public class SEMOSS {

	public final static String BASE_URI = "http://semoss.org/ontologies";
	
	/**
	 * V-CAMP SEMOSS Tool namespace: http://semoss.org/ontologie/
	 */
	public final static String NAMESPACE = BASE_URI + "/";

	/**
	 * Recommend prefix for the V-CAMP SEMOSS Tool namespace: "semoss"
	 */
	public final static String PREFIX = "semoss";

	/**
	 * An immutable {@link Namespace} constant that represents the SEMOSS
	 * Tool namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	

	// ----- Classes ------
	
	/**
	 * http://va.gov/ontologies/Concept
	 */
	public final static URI Concept;
	
	/**
	 * http://va.gov/ontologies/Relation
	 */
	public final static URI Relation;

    
	// ----- Properties ------
	
	/**
	 * http://va.gov/ontologies/has
	 */
	public static final URI has;

	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Concept = factory.createURI(NAMESPACE, "Concept");
		Relation = factory.createURI(NAMESPACE, "Relation");
		
		// ----- Properties ------
		has = factory.createURI(NAMESPACE, "has");
	}
}
