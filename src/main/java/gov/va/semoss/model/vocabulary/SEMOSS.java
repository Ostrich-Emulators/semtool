package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary of the SEMOSS Metamodel.
 *
 */
public class SEMOSS {

	/**
	 * The SEMOSS Metamodel Base URI: http://semoss.org/ontologies
	 */
	public final static String BASE_URI = "http://semoss.org/ontologies";
	
	/**
	 * The SEMOSS Metamodel namespace: http://semoss.org/ontologies/
	 */
	public final static String NAMESPACE = BASE_URI + "/";

	/**
	 * Recommend prefix for the V-CAMP SEMOSS Tool namespace: "semoss"
	 */
	public final static String PREFIX = "semoss";

	/**
	 * An immutable {@link Namespace} constant that represents the SEMOSS
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	

	// ----- Classes ------
	
	/**
	 * http://semoss.org/ontologies/Concept
	 */
	public final static URI Concept;
	
	/**
	 * http://semoss.org/ontologies/Relation
	 */
	public final static URI Relation;

    
	// ----- Properties ------
	
	/**
	 * http://semoss.org/ontologies/has
	 */
	public static final URI has;

	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Concept  = factory.createURI(NAMESPACE, "Concept");
		Relation = factory.createURI(NAMESPACE, "Relation");
		
		// ----- Properties ------
		has = factory.createURI(NAMESPACE, "has");
	}
}
