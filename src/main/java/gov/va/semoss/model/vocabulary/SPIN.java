package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;


/**
 * Minimal vocabulary of the SPIN Modeling Vocabulary.
 *
 */
public class SPIN {

	public final static String BASE_URI = "http://spinrdf.org/spin";
	
	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/spin#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN SPARQL Syntax schema namespace: "spin"
	 */
	public final static String PREFIX = "spin";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN
	 * Modeling Vocabulary namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);


	// ----- Classes ------

	/**
	 * http://spinrdf.org/spin#Function
	 */
    public final static URI Function;
	
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/spin#body
	 */
	public final static URI body;
    
	/**
	 * http://spinrdf.org/spin#constraint
	 */
	public final static URI constraint;
	
	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Function = factory.createURI(NAMESPACE, "Function");
		
		body = factory.createURI(NAMESPACE, "body");
		
		// ----- Properties ------
		constraint = factory.createURI(NAMESPACE, "constraint");
	}
}
