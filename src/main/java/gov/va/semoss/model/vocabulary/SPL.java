package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Minimal vocabulary of the SPIN Standard Modules Library.
 *
 */
public class SPL {

	public final static String BASE_URI = "http://spinrdf.org/spl";
	
	/**
	 * SPIN Standard Modules Library namespace: http://spinrdf.org/spl#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the SPIN Standard Modules Library namespace: "spl"
	 */
	public final static String PREFIX = "spl";

	/**
	 * An immutable {@link Namespace} constant that represents the SPIN Standard
	 * Modules Library namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);


	// ----- Classes ------
	
	/**
	 * http://spinrdf.org/spl#Argument
	 */
    public final static URI Argument;
	
    
	// ----- Properties ------
    
	/**
	 * http://spinrdf.org/spl#predicate
	 */
	public final static URI predicate;
	public final static URI valueType;
	public static final URI defaultValue;
	
	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Argument = factory.createURI(NAMESPACE, "Argument");
		
		// ----- Properties ------
		predicate = factory.createURI(NAMESPACE, "predicate");
		
		valueType = factory.createURI(NAMESPACE, "valueType");
		
		defaultValue = factory.createURI(NAMESPACE, "defaultValue" );
		
	}
}
