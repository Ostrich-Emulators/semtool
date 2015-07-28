package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.Constants;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * This class is responsible for providing a number of utility methods
 * for the SEMOSS system, specifically in the area of producing concept 
 * collections from gross RDF content, and deriving predicates.
 * @author Wayne Warren
 *
 */
public class NodeDerivationTools {
	/** The logger for this class */
	private static final Logger logger = Logger.getLogger( NodeDerivationTools.class );
	/** The singleton instance */
	private static NodeDerivationTools instance;
	
	/**
	 * Private singleton default constructor
	 */
	private NodeDerivationTools(){
		
	}
	
	/**
	 * The singleton access method
	 * @return The singleton instance
	 */
	public static NodeDerivationTools instance(){
		if (instance == null){
			instance = new NodeDerivationTools();
		}
		return instance;
	}
	
	/**
	 * Produces a list of concepts based on a given engine that has digested 
	 * a RDF knowledgebase.
	 * @param engine The RDF knowledgbase
	 * @return A list of concepts in URI form
	 */
	public List<URI> createConceptList( IEngine engine ) {
		final List<URI> conceptList = new ArrayList<>();
		String query = "SELECT ?entity WHERE "
				+ "{ ?entity rdfs:subClassOf+ ?concept . FILTER( ?entity != ?concept ) }";
		OneVarListQueryAdapter<URI> qe
				= OneVarListQueryAdapter.getUriList( query, "entity" );
		qe.bind( "concept", engine.getSchemaBuilder().getConceptUri().build() );

		try {
			conceptList.addAll( engine.query( qe ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}
		return conceptList;
	}
	
	/**
	 * Derive a query adapter capable of pulling out the predicates that connect
	 * all subject nodes of a given type and all object nodes of a given type
	 * @param subjectNodeType The type (in URI form) of the subject node
	 * @param objectNodeType The type (in URI form) of the object node
	 * @return A proper query adapter capable of querying a knowledgebase for the desired predicates
	 */
	public ListQueryAdapter<URI> getPredicatesBetween( URI subjectNodeType,
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
	 * @param subjectNodeType The type of subject node 
	 * @param objectNodeType The type of object node
	 * @param engine The engine, which contains a digested knowledgebase, which will
	 *  run the query designed to derive the various predicates between the node types
	 * @return A list of predicates, in URI
	 */
	public List<URI> getPredicatesBetween( URI subjectNodeType, URI objectNodeType,
			IEngine engine ) {
		List<URI> values;
		try {
			values = engine.query( getPredicatesBetween( subjectNodeType, objectNodeType ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			values = new ArrayList<>();
		}

		return values;
	}
}