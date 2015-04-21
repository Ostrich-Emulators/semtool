package gov.va.semoss.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Minimal vocabulary to support VA SEMOSS Insights.
 *
 */
public class VAS {

	/**
	 * V-CAMP SEMOSS Tool Base URI: http://semoss.org/ontologies
	 */
	public final static String BASE_URI = "http://va.gov/ontologies/semoss";

	/**
	 * V-CAMP SEMOSS Tool schema namespace: http://va.gov/ontologies/semoss#
	 */
	public final static String NAMESPACE = BASE_URI + "#";

	/**
	 * Recommend prefix for the V-CAMP SEMOSS Tool namespace: "vas"
	 */
	public final static String PREFIX = "vas";

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

	// ----- Classes ------
	/**
	 * http://semoss.va.gov#DataView
	 */
	public final static URI DataView;

	/**
	 * http://semoss.va.gov#Functions
	 */
	public final static URI Functions;

	/**
	 * http://semoss.va.gov#Perspective
	 */
	public final static URI Perspective;

	public final static URI DATABASE;
	public final static URI REIFICATION;
	public final static URI SEMOSS_REIFICATION;
	public final static URI RDR_REIFICATION;

	// ----- Properties ------
	/**
	 * http://semoss.va.gov#insight
	 */
	public static final URI insight;

	/**
	 * http://semoss.va.gov#icon
	 */
	public static final URI icon;

	/**
	 * http://semoss.va.gov#isLegacy
	 */
	public static final URI isLegacy;

	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();

		// ----- Classes ------
		DataView = factory.createURI( NAMESPACE, "DataView" );
		Functions = factory.createURI( NAMESPACE, "Functions" );
		Perspective = factory.createURI( NAMESPACE, "Perspective" );

		// the vas:DATABASE type
		DATABASE = factory.createURI( NAMESPACE, "Database" );
		REIFICATION = factory.createURI( NAMESPACE, "reification" );
		SEMOSS_REIFICATION = factory.createURI( NAMESPACE, "VASEMOSS-Reification" );
		RDR_REIFICATION = factory.createURI( NAMESPACE, "RDR-Reification" );

		// ----- Properties ------
		insight = factory.createURI( NAMESPACE, "insight" );
		icon = factory.createURI( NAMESPACE, "icon" );
		isLegacy = factory.createURI( NAMESPACE, "isLegacy" );
	}
}
