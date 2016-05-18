package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.VAS;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneValueQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 * This class is responsible for providing a number of utility methods for the
 * SEMOSS system, specifically in the area of producing concept collections from
 * gross RDF content, and deriving predicates.
 *
 * @author Wayne Warren
 *
 */
public class NodeDerivationTools {

	/**
	 * The logger for this class
	 */
	private static final Logger logger = Logger.getLogger( NodeDerivationTools.class );

	/**
	 * Private singleton default constructor
	 */
	private NodeDerivationTools() {

	}

	public static String getConceptQuery( IEngine engine ) {
		MetadataQuery mq = new MetadataQuery( VAS.ReificationModel );
		engine.queryNoEx( mq );
		URI reif = URI.class.cast( mq.getOne() );

		if ( VAS.Custom_Reification.equals( reif ) ) {
			OneValueQueryAdapter<String> qq = OneValueQueryAdapter.
					getString( "SELECT ?val WHERE { ?base ?pred ?val }" );
			qq.bind( "base", engine.getBaseUri() );
			qq.bind( "pred", VAS.ConceptsSparql );

			return engine.queryNoEx( qq );
		}
		else {
			String query = "SELECT ?concept WHERE "
					+ "{ "
					+ "  ?concept rdfs:subClassOf+ ?top ."
					+ "  FILTER( ?concept != ?top ) ."
					+ "  VALUES ?top {<replace-with-binding>} "
					+ " }";

			return query.replaceAll( "replace-with-binding",
					engine.getSchemaBuilder().getConceptUri().build().stringValue() );
		}
	}

	/**
	 * Produces a list of concepts based on a given engine that has digested a RDF
	 * knowledgebase.
	 *
	 * @param engine The RDF knowledgebase
	 * @return A list of concepts in URI form
	 */
	public static List<URI> createConceptList( IEngine engine ) {
		OneVarListQueryAdapter<URI> qe
				= OneVarListQueryAdapter.getUriList( getConceptQuery( engine ) );
		final List<URI> conceptList = engine.queryNoEx( qe );
		return conceptList;
	}

	public static List<URI> createInstanceList( URI concept, IEngine engine ) {
		String query = "SELECT DISTINCT ?s WHERE { ?instance rdf:type ?concept }";
		ListQueryAdapter<URI> qa = OneVarListQueryAdapter.getUriList( query );
		qa.bind( "concept", concept );

		return engine.queryNoEx( qa );
	}

	/**
	 * Derive a query adapter capable of pulling out the predicates that connect
	 * all subject nodes of a given type and all object nodes of a given type
	 *
	 * @param subjectNodeType The type (in URI form) of the subject node
	 * @param objectNodeType The type (in URI form) of the object node
	 * @return A proper query adapter capable of querying a knowledgebase for the
	 * desired predicates
	 */
	public static ListQueryAdapter<URI> getPredicatesBetween( URI subjectNodeType,
			URI objectNodeType ) {
		String q
				= "SELECT DISTINCT ?relationship WHERE {"
				+ "?in  a ?stype . "
				+ "?out a ?otype . "
				+ "?in ?relationship ?out . "
				+ "MINUS{ ?relationship rdf:predicate ?p }"
				+ "}";
		OneVarListQueryAdapter<URI> varq = OneVarListQueryAdapter.getUriList( q, "relationship" );
		varq.useInferred( false );
		varq.bind( "stype", subjectNodeType );
		if ( !objectNodeType.equals( Constants.ANYNODE ) ) {
			varq.bind( "otype", objectNodeType );
		}
		return varq;
	}

	/**
	 * Get a list of predicates that connect subjects and objects of given types
	 *
	 * @param subjectNodeType The type of subject node
	 * @param objectNodeType The type of object node
	 * @param engine The engine, which contains a digested knowledgebase, which
	 * will run the query designed to derive the various predicates between the
	 * node types
	 * @return A list of predicates, in URI
	 */
	public static List<URI> getPredicatesBetween( URI subjectNodeType, URI objectNodeType,
			IEngine engine ) {
		List<URI> values = engine.queryNoEx( getPredicatesBetween( subjectNodeType, objectNodeType ) );
		return values;
	}

	public static URI getType( URI instance, IEngine engine ) {
		String query = "SELECT ?object "
				+ "WHERE { "
				+ "  ?subject a ?object "
				+ "  FILTER NOT EXISTS { ?subject a ?subtype . ?subtype rdfs:subClassOf ?object }"
				+ "}";

		return engine.queryNoEx( OneValueQueryAdapter.getUri( query ).bind( "subject", instance ) );
	}

	public static List<URI> getConnectedConceptTypes( URI instance, IEngine engine,
			boolean instanceIsSubject ) {
		String query = "SELECT DISTINCT ?subtype ?objtype "
				+ "WHERE { "
				+ "  ?subject ?predicate ?object . FILTER( isUri( ?object ) ) ."
				+ "  ?subject a ?subtype ."
				+ "  ?object a ?objtype ."
				+ "}";

		OneVarListQueryAdapter<URI> lqa = OneVarListQueryAdapter.getUriList( query );
		if ( instanceIsSubject ) {
			lqa.setVariableName( "objtype" );
			lqa.bind( "subject", instance );
		}
		else {
			lqa.setVariableName( "subtype" );
			lqa.bind( "object", instance );
		}

		logger.debug( "query is: " + query );
		logger.debug( "instance is: " + instance );

		return engine.queryNoEx( lqa );
	}

	public static List<URI> getConnectedConceptTypes( Collection<URI> instances,
			IEngine engine, boolean instanceIsSubject ) {

		StringBuilder query = new StringBuilder( "SELECT DISTINCT ?subtype ?objtype " )
				.append( "WHERE { " )
				.append( "  ?subject ?predicate ?object ." )
				.append( "  ?subject a ?subtype ." )
				.append( "  ?object a ?objtype . FILTER isUri( ?object ) ." )
				.append( "  MINUS { ?subject a ?object } " )
				.append( "} VALUES ?" );
		query.append( instanceIsSubject ? "subject " : "object" );
		query.append( "{" );
		query.append( Utility.implode( instances, "<", ">", " " ) );
		query.append( "}" );

		OneVarListQueryAdapter<URI> lqa
				= OneVarListQueryAdapter.getUriList( query.toString() );
		if ( instanceIsSubject ) {
			lqa.setVariableName( "objtype" );
		}
		else {
			lqa.setVariableName( "subtype" );
		}

		logger.debug( "query is: " + query );
		logger.debug( "instances are: " + instances );

		return engine.queryNoEx( lqa );
	}
}
