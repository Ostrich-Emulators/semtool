package com.ostrichemulators.semtool.model.vocabulary;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

public class SEMPERS {
//	public static final String VA_INSIGHTS_PREFIX = "insights";
//	public static final String VA_INSIGHTS_NS = "http://os-em.com/dataset/insights#";
//	public static final URI VA_INSIGHTS = new URIImpl( "http://os-em.com/dataset/insights" );

	public static final URI INSIGHT_CORE_TYPE
			= new URIImpl( "http://os-em.com/ontologies/semtool/core#InsightDataset" );

	public final static String BASE_URI = "http://os-em.com/ontologies/semtool/insights";
	public static final URI INSIGHT_DB = new URIImpl( BASE_URI );

	public final static String NAMESPACE = BASE_URI + "#";

	public final static String PREFIX = "sempers";

	public static final Namespace NS = new NamespaceImpl( PREFIX, NAMESPACE );

	public final static URI InsightProperties;

	public final static URI Perspective;

	public final static URI INSIGHT_OUTPUT_TYPE;

	public static final URI insight;

	static {
		ValueFactory factory = new ValueFactoryImpl();

		InsightProperties = factory.createURI( NAMESPACE, "InsightProperties" );
		Perspective = factory.createURI( NAMESPACE, "Perspective" );
		insight = factory.createURI( NAMESPACE, "insight" );
		INSIGHT_OUTPUT_TYPE = factory.createURI( NAMESPACE, "outputType" );
	}

}
