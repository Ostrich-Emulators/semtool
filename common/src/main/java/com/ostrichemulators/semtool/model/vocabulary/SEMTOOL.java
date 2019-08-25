package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Minimal vocabulary to support Insights.
 *
 */
public class SEMTOOL {

	public final static String BASE_URI = "http://os-em.com/ontologies/semtool";

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "semtool";

	/**
	 * An immutable {@link Namespace} constant that represents the VA SEMOSS Tool
	 * namespace.
	 */
	public static final Namespace NS = new SimpleNamespace( PREFIX, NAMESPACE );

	// ----- Classes ------
	public final static IRI Database;

	public final static IRI DataView;

	public final static IRI ReificationModel;

	public final static IRI RDR_Reification;

	public final static IRI SEMTOOL_Reification;

	public final static IRI W3C_Reification;

	public final static IRI Custom_Reification;

	public static final IRI isLegacy;

	public static final IRI rendererClass;

	public static final IRI reification;

	public static final IRI ConceptsSparql;

	public static final IRI EdgesSparql;
	public static final IRI Structure;

	static {
		final ValueFactory factory = SimpleValueFactory.getInstance();

		// ----- Classes ------
		Database = factory.createIRI( NAMESPACE, "Database" );
		DataView = factory.createIRI( NAMESPACE, "DataView" );
		ReificationModel = factory.createIRI( NAMESPACE, "ReificationModel" );

		// ----- Individuals ------
		RDR_Reification = factory.createIRI( NAMESPACE, "RDR-Reification" );
		SEMTOOL_Reification = factory.createIRI( NAMESPACE, "SEMTOOL-Reification" );
		W3C_Reification = factory.createIRI( NAMESPACE, "W3C-Reification" );
		Custom_Reification = factory.createIRI( NAMESPACE, "Custom-Reification" );
		ConceptsSparql = factory.createIRI( NAMESPACE, "Custom-Concept-Sparql" );
		EdgesSparql = factory.createIRI( NAMESPACE, "Custom-Edge-Sparql" );
		Structure = factory.createIRI( NAMESPACE, "StructureData" );

		// ----- Properties ------
		isLegacy = factory.createIRI( NAMESPACE, "isLegacy" );
		reification = factory.createIRI( NAMESPACE, "reification" );
		rendererClass = factory.createIRI( NAMESPACE, "rendererClass" );
	}
}
