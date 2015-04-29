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
	 * V-CAMP SEMOSS Tool Base URI: http://va.gov/ontologies/semoss
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
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS
	 * Tool namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	// ----- Classes ------
	
	/**
	 * http://va.gov/ontologies/semoss#Database
	 */
	public final static URI Database;
	
	/**
	 * http://va.gov/ontologies/semoss#DataView
	 */
	public final static URI DataView;
	
	/**
	 * http://va.gov/ontologies/semoss#Functions
	 */
	public final static URI InsightProperties;
	
	/**
	 * http://va.gov/ontologies/semoss#Perspective
	 */
	public final static URI Perspective;
	
	/**
	 * http://va.gov/ontologies/semoss#ReificationModel
	 */
	public final static URI ReificationModel;
    
	// ----- Individuals ------
	
	/**
	 * http://va.gov/ontologies/semoss#RDR-Reification
	 */
	public final static URI RDR_Reification;		
	
	/**
	 * http://va.gov/ontologies/semoss#RDR-Reification
	 */
	public final static URI VASEMOSS_Reification;
	
	/**
	 * http://va.gov/ontologies/semoss#RDR-Reification
	 */
	public final static URI W3C_Reification;	
    
	// ----- Properties ------
	
	/**
	 * http://va.gov/ontologies/semoss#insight
	 */
	public static final URI insight;

	/**
	 * http://va.gov/ontologies/semoss#icon
	 */
	public static final URI icon;

	/**
	 * http://va.gov/ontologies/semoss#isLegacy
	 */
	public static final URI isLegacy;
	
	/**
	 * http://va.gov/ontologies/semoss#rendererClass
	 */
	public static final URI rendererClass;
	
	/**
	 * http://va.gov/ontologies/semoss#rendererClass
	 */
	public static final URI reification;

	
	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		Database = factory.createURI(NAMESPACE, "Database");
		DataView = factory.createURI(NAMESPACE, "DataView");
		InsightProperties = factory.createURI(NAMESPACE, "InsightProperties");
		Perspective = factory.createURI(NAMESPACE, "Perspective");
		ReificationModel = factory.createURI(NAMESPACE, "ReificationModel");
		
		// ----- Individuals ------
		RDR_Reification = factory.createURI(NAMESPACE, "RDR-Reification");
		VASEMOSS_Reification = factory.createURI(NAMESPACE, "VASEMOSS-Reification");
		W3C_Reification = factory.createURI(NAMESPACE, "W3C-Reification");
		
		// ----- Properties ------
		insight = factory.createURI(NAMESPACE, "insight");
		icon = factory.createURI(NAMESPACE, "icon");
		isLegacy = factory.createURI(NAMESPACE, "isLegacy");
		reification = factory.createURI(NAMESPACE, "reification");
		rendererClass = factory.createURI(NAMESPACE, "rendererClass");
	}
}
