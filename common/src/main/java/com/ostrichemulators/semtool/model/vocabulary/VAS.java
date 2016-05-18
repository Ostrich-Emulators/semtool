package com.ostrichemulators.semtool.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Minimal vocabulary to support Insights.
 *
 */
public class VAS {

	public final static String BASE_URI = "http://os-em.com/ontologies/semoss";

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "vas";

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

	// ----- Classes ------
	public final static URI Database;

	public final static URI DataView;

	public final static URI InsightProperties;

	public final static URI Perspective;

	public final static URI ReificationModel;

	// ----- Individuals ------
	public final static URI RDR_Reification;

	public final static URI VASEMOSS_Reification;

	public final static URI W3C_Reification;

	public final static URI Custom_Reification;

	public final static URI INSIGHT_OUTPUT_TYPE;
	// ----- Properties ------

	public static final URI insight;

	public static final URI icon;

	public static final URI isLegacy;

	public static final URI rendererClass;

	public static final URI reification;

	public static final URI ConceptsSparql;

	public static final URI EdgesSparql;

	static {
		final ValueFactory factory = ValueFactoryImpl.getInstance();

		// ----- Classes ------
		Database = factory.createURI( NAMESPACE, "Database" );
		DataView = factory.createURI( NAMESPACE, "DataView" );
		InsightProperties = factory.createURI( NAMESPACE, "InsightProperties" );
		Perspective = factory.createURI( NAMESPACE, "Perspective" );
		ReificationModel = factory.createURI( NAMESPACE, "ReificationModel" );

		// ----- Individuals ------
		RDR_Reification = factory.createURI( NAMESPACE, "RDR-Reification" );
		VASEMOSS_Reification = factory.createURI( NAMESPACE, "VASEMOSS-Reification" );
		W3C_Reification = factory.createURI( NAMESPACE, "W3C-Reification" );
		Custom_Reification = factory.createURI( NAMESPACE, "Custom-Reification" );
		ConceptsSparql = factory.createURI( NAMESPACE, "Custom-Concept-Sparql" );
		EdgesSparql = factory.createURI( NAMESPACE, "Custom-Edge-Sparql" );

		// ----- Properties ------
		insight = factory.createURI( NAMESPACE, "insight" );
		icon = factory.createURI( NAMESPACE, "icon" );
		isLegacy = factory.createURI( NAMESPACE, "isLegacy" );
		reification = factory.createURI( NAMESPACE, "reification" );
		rendererClass = factory.createURI( NAMESPACE, "rendererClass" );
		INSIGHT_OUTPUT_TYPE = factory.createURI( NAMESPACE, "outputType" );
	}
}
