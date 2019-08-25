package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SEMPERS {
//	public static final String VA_INSIGHTS_PREFIX = "insights";
//	public static final String VA_INSIGHTS_NS = "http://os-em.com/dataset/insights#";
//	public static final URI VA_INSIGHTS = new URIImpl( "http://os-em.com/dataset/insights" );

	public static final IRI INSIGHT_CORE_TYPE;

	public final static String BASE_URI = "http://os-em.com/ontologies/semtool/insights";
	public static final IRI INSIGHT_DB;

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "sempers";

	public static final Namespace NS = new SimpleNamespace( PREFIX, NAMESPACE );

	public final static IRI InsightProperties;

	public final static IRI Perspective;

	public final static IRI INSIGHT_OUTPUT_TYPE;

	public static final IRI insight;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		INSIGHT_DB = factory.createIRI( BASE_URI );
		INSIGHT_CORE_TYPE = factory.createIRI( "http://os-em.com/ontologies/semtool/core#InsightDataset" );

		InsightProperties = factory.createIRI( NAMESPACE, "InsightProperties" );
		Perspective = factory.createIRI( NAMESPACE, "Perspective" );
		insight = factory.createIRI( NAMESPACE, "insight" );
		INSIGHT_OUTPUT_TYPE = factory.createIRI( NAMESPACE, "outputType" );
	}

}
