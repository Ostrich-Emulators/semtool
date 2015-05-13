/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.ui.components;

import org.apache.log4j.Logger;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.om.GraphDataModel;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructWrapper;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectCheater;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectWrapper;
import gov.va.semoss.util.DIHelper;

/**
 * This class is responsible for loading many of the hierarchies available in RDF through the repository connection.
 */
public class RDFEngineHelper {
	private static final Logger logger = Logger.getLogger(RDFEngineHelper.class);
	
	/**
	 * Loads the concept hierarchy.
	 * @param engine	 		Engine where data is stored.
	 * @param subjects 			Subject.
	 * @param objects 			Object.
	 * @param gdm 				Graph playsheet that allows properties to be added to the repository connection.
	 */
	public static void loadConceptHierarchy(IEngine engine, String subjects, 
      String objects, GraphDataModel gdm) {
		String query =
					"CONSTRUCT { ?Subject ?Predicate ?Object } WHERE {" +
					"  {?Subject a ?Object}" +
					"  {?Subject ?Predicate ?Object}" + 
					"} BINDINGS ?Subject { " + subjects + objects + " } ";

		int numResults = addResultsToRC(engine, query, gdm);
		logger.debug("loadConceptHierarchy added " + numResults + " results to the sesame rc.");
	}

	/**
	 * Loads the relation hierarchy.
	 * @param engine	 			Engine where data is stored.
	 * @param predicates 			Predicate.
	 * @param gdm 					Graph playsheet that allows properties to be added to the repository connection.
	 */
	public static void loadRelationHierarchy(IEngine engine, String predicates, GraphDataModel gdm) {
		String query = 
				"CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {" +
				"  {?Subject ?Predicate ?Object}" + 
				"  {?Subject rdfs:subPropertyOf ?Object}" +
				"} BINDINGS ?Subject { " + predicates + " } ";

		int numResults = addResultsToRC(engine, query, gdm);
		logger.debug("loadRelationHierarchy added " + numResults + " results to the sesame rc.");
	}

	/**
	 * Loads the property hierarchy.
	 * @param engine 		Engine where data is stored.
	 * @param predicates 		Predicate.
	 * @param containsRelation 	String that shows the relation.
	 * @param gdm 				Graph playsheet that allows properties to be added to the repository connection.
	 */
	public static void loadPropertyHierarchy(IEngine engine, String predicates, String containsRelation, GraphDataModel gdm) {
		String query = 
				"CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {" +
				"  {?Subject ?Predicate ?Object}" + 
				"  {?Subject a " + containsRelation + " }" +
				"} " + 
				"BINDINGS ?Subject { " + predicates + " } ";

		int numResults = addResultsToRC(engine, query, gdm);
		logger.debug("loadPropertyHierarchy added " + numResults + " results to the sesame rc.");
	}

	/**
	 * Gets general properties given a subject, object, predicate, and relationship.
	 * @param engine	 		Engine where data is stored.
	 * @param subjects 			Subject.
	 * @param objects 			Object.
	 * @param predicates 		Predicate.
	 * @param containsRelation 	String that shows the relation for the property query.
	 * @param gdm 				Graph playsheet that allows properties to be added to repository connection.
	 */
	public static void genPropertiesRemote(IEngine engine, String subjects, String objects, String predicates, String containsRelation, GraphDataModel gdm) {
		String query = 
				"CONSTRUCT { ?Subject ?Predicate ?Object . ?Predicate ?type ?contains} WHERE {" +
				"  BIND(<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> AS ?type)" +
				"  BIND(" + containsRelation + " as ?contains)" +
				"  {?Predicate a " +  containsRelation + ";}" +
				"  {?Subject ?Predicate ?Object}" + 
				"}" +
				"BINDINGS ?Subject { " + subjects + " " + predicates + " " + objects + " }";

		int numResults = addResultsToRC(engine, query, gdm);
		logger.debug("genPropertiesRemote added " + numResults + " results to the sesame rc.");
	}

	/**
	 * Gets node properties from a local repository connection.
	 * @param rc 				Repository connection: main interface for updating data in and performing queries on a Sesame repository.
	 * @param containsRelation 	String that shows the relation for the property query.
	 * @param gdm 				Graph playsheet where edge properties are added.
	 */
	public static void genNodePropertiesLocal(RepositoryConnection rc, String containsRelation, GraphDataModel gdm) {
		String query =  
				"CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {" +
				"  {?Predicate a " +  containsRelation + ";}" +
				"  {?Subject   a <" + DIHelper.getConceptURI().stringValue() + ">;}" +
				"  {?Subject   ?Predicate ?Object}" + 
				"}";
		
		int numResults = 0;
		SesameJenaConstructWrapper sjsc = runSesameJenaConstruct(rc, query);
		while(sjsc.hasNext()) {
			SesameJenaConstructStatement s = sjsc.next();

			SEMOSSVertex vertex = gdm.createOrRetrieveVertex(s.getSubject());
			vertex.setProperty(s.getPredicate(), s.getObject());
			gdm.storeVertex(vertex);
			
			numResults++;
		}
		logger.debug("genNodePropertiesLocal added " + numResults + " node properties.");
	}
	
	/**
	 * Gets edge properties from a local repository connection.
	 * @param rc 				Repository connection: main interface for updating data in and performing queries on a Sesame repository.
	 * @param containsRelation 	String that shows the relation for the property query.
	 * @param gdm 				Graph playsheet where edge properties are added.
	 */
	public static void genEdgePropertiesLocal(RepositoryConnection rc, String containsRelation, GraphDataModel gdm) {
		String query = 
				"SELECT ?edge ?prop ?value ?outNode ?inNode WHERE {" +
				"  {?prop     a                  " +  containsRelation + ";}" +
				"  {?edge     rdfs:subPropertyOf <" + DIHelper.getRelationURI().stringValue() + ">;}" +
				"  {?outNode  a                  <" + DIHelper.getConceptURI().stringValue() + ">; }" +
				"  {?inNode   a                  <" + DIHelper.getConceptURI().stringValue() + ">; }" +
				"  {?edge     ?prop              ?value} " + 
				"  {?outNode  ?edge              ?inNode} " + 
				"}";
		
		int numResults = 0;
		SesameJenaSelectWrapper sjsc = runSesameJenaSelectWrapper(rc, query);
		while(sjsc.hasNext()) {
			SesameJenaSelectStatement sct = sjsc.next();
			gdm.addEdgeProperty(	"" + sct.getRawVar("edge"   ), 
									     sct.getRawVar("value"  ), 
									"" + sct.getRawVar("prop"   ), 
									"" + sct.getRawVar("outNode"), 
									"" + sct.getRawVar("inNode" ) );
			numResults++;
		}
		logger.debug("genEdgePropertiesLocal added " + numResults + " edge properties.");
	}

	/**
	 * Add results from a query on an engine to the respository connection.
	 * @param fromEngine 	Engine where data is stored.
	 * @param query 		Query to be run.
	 * @param ps 			Graph playsheet where sesame construct statement is stored.
	 */
	private static int addResultsToRC(IEngine engine, String query, GraphDataModel gdm) {
		int numResults=0;

		SesameJenaConstructWrapper sjsc = runSesameJenaConstruct(engine, query);
		while(sjsc.hasNext()) {
			gdm.addToSesame(sjsc.next(), false);
			numResults++;
		}
		
		return numResults;
	}

	/**
	 * Adds data to a specified engine
	 * @param engine 	Engine where data is stored.
	 * @param toRC 			Main interface for updating data in and performing queries on a Sesame repository.
	 */
	public static void addAllData(IEngine engine, RepositoryConnection toRC) {
		addOrRemoveAllData(engine, toRC, true);
	}
	
	/**
	 * Removes all data from a certain engine.
	 * @param engine 	Engine where data is stored.
	 * @param toRC 			Main interface for updating data in and performing queries on a Sesame repository.
	 */
	public static void removeAllData(IEngine engine, RepositoryConnection toRC) {
		addOrRemoveAllData(engine, toRC, false);
	}

	private static void addOrRemoveAllData(IEngine engine, RepositoryConnection toRC, boolean add) {
		String query = 
				"CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {" +
				"  {?Subject ?Predicate ?Object} " + 
				"}";

		SesameJenaConstructWrapper sjsc = runSesameJenaConstruct(engine, query);
		try {
			if (add) {
				toRC.add(sjsc.gqr);
			} else {
				toRC.remove(sjsc.gqr);
			}
		} catch (QueryEvaluationException | RepositoryException e) {
			logger.error( e );
		}
	}
	
	public static SesameJenaSelectWrapper runSesameJenaSelectWrapper(RepositoryConnection rc, String query) {
		IEngine engine = new InMemorySesameEngine( rc );
		return runSesameJenaSelectWrapper(engine, query);
	}
	
	public static SesameJenaSelectWrapper runSesameJenaSelectWrapper(IEngine engine, String query) {
		logger.debug("Running query in runSesameJenaSelectWrapper:" + query);
		SesameJenaSelectWrapper sjsc = new SesameJenaSelectWrapper();
		
		try {
			sjsc.setEngine(engine);
			sjsc.setQuery(query);
			sjsc.executeQuery();
			sjsc.getVariables();
		} catch(Exception ex) {
			logger.error( ex );
		}
		
		return sjsc;
	}
	
	public static SesameJenaConstructWrapper runSesameJenaSelectCheater(RepositoryConnection rc, String query) {
		IEngine engine = new InMemorySesameEngine( rc );
		return runSesameJenaSelectCheater(engine, query);
	}
	
	public static SesameJenaConstructWrapper runSesameJenaConstruct(RepositoryConnection rc, String query) {
		IEngine engine = new InMemorySesameEngine( rc );
		return runSesameJenaConstruct(engine, query);
	}
	
	public static SesameJenaConstructWrapper runSesameConstructOrSelectQuery(IEngine engine, String query) {
		if (query.toUpperCase().startsWith("CONSTRUCT") )
			return runSesameJenaConstruct(engine, query);

		return runSesameJenaSelectCheater(engine, query);
	}
	
	public static SesameJenaConstructWrapper runSesameJenaSelectCheater(IEngine engine, String query) {
		logger.debug("Running query in runSesameJenaSelectCheater: " + query);

		return runSesameQuery(new SesameJenaSelectCheater(), engine, query);
	}
	
	public static SesameJenaConstructWrapper runSesameJenaConstruct(IEngine engine, String query) {
		logger.debug("Running query in runSesameJenaConstruct: " + query);

		return runSesameQuery(new SesameJenaConstructWrapper(), engine, query);
	}
	
	private static SesameJenaConstructWrapper runSesameQuery(SesameJenaConstructWrapper sjsc, IEngine engine, String query) {
		try {
			sjsc.setEngine(engine);
			sjsc.setQuery(query);
			sjsc.execute();
			
			if ( !sjsc.hasNext() ) {
				logger.debug( "Came into not having ANY data" );
			}
		} catch(Exception ex) {
			logger.error( ex, ex );
		}

		return sjsc;
	}
}