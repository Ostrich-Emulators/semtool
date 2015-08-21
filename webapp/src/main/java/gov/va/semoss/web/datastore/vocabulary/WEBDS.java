package gov.va.semoss.web.datastore.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

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

	/**
	 * http://semoss.org/ontologies/Relation
	 */
	public final static URI DBINFO;

	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();

		// ----- Classes ------
		DBINFO = factory.createURI( NAMESPACE, "DbInfo" );
	}
}
