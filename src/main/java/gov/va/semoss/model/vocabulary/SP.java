package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;


/**
 * Minimal vocabulary of the SPIN SPARQL Syntax schema.
 *
 */
public class SP {

	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/spin#
	 */
	public final static String BASE_URI = "http://spinrdf.org/sp";
	
	/**
	 * SPIN SPARQL Syntax schema namespace: http://spinrdf.org/spl#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN SPARQL Syntax schema namespace: "sp"
	 */
	public final static String PREFIX = "sp";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN SPARQL
	 * Syntax schema namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);


	// ----- Classes ------

	/**
	 * http://spinrdf.org/sp#Construct
	 */
    public final static URI Construct;
    
	/**
	 * http://spinrdf.org/sp#Select
	 */
    public final static URI Select;
	
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/sp#text
	 */
	public final static URI text;
    
	/**
	 * http://spinrdf.org/sp#query
	 */
	public final static URI query;
	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Construct = factory.createURI(NAMESPACE, "Construct");
		
		Select = factory.createURI(NAMESPACE, "Select");
		
		// ----- Properties ------
		text = factory.createURI(NAMESPACE, "text");
		
		query = factory.createURI(NAMESPACE, "query");
	}
}
