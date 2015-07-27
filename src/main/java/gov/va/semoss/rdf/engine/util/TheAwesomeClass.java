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
 * for the SEMOSS system.
 * @author Wayne Warren
 *
 */
public class TheAwesomeClass {
	/** The logger for this class */
	private static final Logger logger = Logger.getLogger( DBToLoadingSheetExporter.class );
	
	/** The singleton instance */
	private static TheAwesomeClass instance;
	
	/**
	 * Private singleton default constructor
	 */
	private TheAwesomeClass(){
		
	}
	
	/**
	 * The singleton access method
	 * @return
	 */
	public static TheAwesomeClass instance(){
		if (instance == null){
			instance = new TheAwesomeClass();
		}
		return instance;
	}
	
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
