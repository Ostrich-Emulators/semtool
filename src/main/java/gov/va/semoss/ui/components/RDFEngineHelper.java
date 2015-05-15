/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package gov.va.semoss.ui.components;

import org.apache.log4j.Logger;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import gov.va.semoss.om.GraphDataModel;
import gov.va.semoss.om.SEMOSSVertex;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.AbstractSesameEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructWrapper;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectWrapper;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.ModelQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.DIHelper;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.rio.ntriples.NTriplesWriter;

/**
 * This class is responsible for loading many of the hierarchies available in
 * RDF through the repository connection.
 */
public class RDFEngineHelper {

	private static final Logger logger = Logger.getLogger( RDFEngineHelper.class );

	/**
	 * Loads the concept hierarchy.
	 *
	 * @param engine	Engine where data is stored.
	 * @param subjects Subject.
	 * @param objects Object.
	 * @param gdm Graph playsheet that allows properties to be added to the
	 * repository connection.
	 */
	public static void loadConceptHierarchy( IEngine engine, String subjects,
			String objects, GraphDataModel gdm ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object } WHERE {"
				+ "  {?Subject a ?Object}"
				+ "  {?Subject ?Predicate ?Object}"
				+ "} BINDINGS ?Subject { " + subjects + objects + " } ";

		int numResults = addResultsToRC( engine, query, gdm );
		logger.debug( "loadConceptHierarchy added " + numResults + " results to the sesame rc." );
	}

	/**
	 * Loads the relation hierarchy.
	 *
	 * @param engine	Engine where data is stored.
	 * @param predicates Predicate.
	 * @param gdm Graph playsheet that allows properties to be added to the
	 * repository connection.
	 */
	public static void loadRelationHierarchy( IEngine engine, String predicates, GraphDataModel gdm ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {"
				+ "  {?Subject ?Predicate ?Object}"
				+ "  {?Subject rdfs:subPropertyOf ?Object}"
				+ "} BINDINGS ?Subject { " + predicates + " } ";

		int numResults = addResultsToRC( engine, query, gdm );
		logger.debug( "loadRelationHierarchy added " + numResults + " results to the sesame rc." );
	}

	/**
	 * Loads the property hierarchy.
	 *
	 * @param engine Engine where data is stored.
	 * @param predicates Predicate.
	 * @param containsRelation String that shows the relation.
	 * @param gdm Graph playsheet that allows properties to be added to the
	 * repository connection.
	 */
	public static void loadPropertyHierarchy( IEngine engine, String predicates, String containsRelation, GraphDataModel gdm ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {"
				+ "  {?Subject ?Predicate ?Object}"
				+ "  {?Subject a " + containsRelation + " }"
				+ "} "
				+ "BINDINGS ?Subject { " + predicates + " } ";

		int numResults = addResultsToRC( engine, query, gdm );
		logger.debug( "loadPropertyHierarchy added " + numResults + " results to the sesame rc." );
	}

	/**
	 * Gets general properties given a subject, object, predicate, and
	 * relationship.
	 *
	 * @param engine	Engine where data is stored.
	 * @param subjects Subject.
	 * @param objects Object.
	 * @param predicates Predicate.
	 * @param containsRelation String that shows the relation for the property
	 * query.
	 * @param gdm Graph playsheet that allows properties to be added to repository
	 * connection.
	 */
	public static void genPropertiesRemote( IEngine engine, String subjects, String objects, String predicates, String containsRelation, GraphDataModel gdm ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object . ?Predicate ?type ?contains} WHERE {"
				+ "  BIND(<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> AS ?type)"
				+ "  BIND(" + containsRelation + " as ?contains)"
				+ "  {?Predicate a " + containsRelation + ";}"
				+ "  {?Subject ?Predicate ?Object}"
				+ "}"
				+ "BINDINGS ?Subject { " + subjects + " " + predicates + " " + objects + " }";

		int numResults = addResultsToRC( engine, query, gdm );
		logger.debug( "genPropertiesRemote added " + numResults + " results to the sesame rc." );
	}

	/**
	 * Gets node properties from a local repository connection.
	 *
	 * @param rc Repository connection: main interface for updating data in and
	 * performing queries on a Sesame repository.
	 * @param containsRelation String that shows the relation for the property
	 * query.
	 * @param gdm Graph playsheet where edge properties are added.
	 */
	public static void genNodePropertiesLocal( RepositoryConnection rc, String containsRelation, GraphDataModel gdm ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {"
				+ "  {?Predicate a " + containsRelation + ";}"
				+ "  {?Subject   a <" + DIHelper.getConceptURI().stringValue() + ">;}"
				+ "  {?Subject   ?Predicate ?Object}"
				+ "}";

//		try( FileWriter fw = new FileWriter( "/tmp/graph.nt" ) ){
//			rc.export( new NTriplesWriter( fw ) );
//		}
//		catch( Exception e ){
//			logger.error( e,e);
//		}

		int numResults = 0;
		Collection<SesameJenaConstructStatement> sjsc = runSesameJenaConstruct( rc, query );
		for ( SesameJenaConstructStatement s : sjsc ) {
			SEMOSSVertex vertex = gdm.createOrRetrieveVertex( s.getSubject() );
			vertex.setProperty( s.getPredicate(), s.getObject() );
			gdm.storeVertex( vertex );

			numResults++;
		}
		logger.debug( "genNodePropertiesLocal added " + numResults + " node properties." );
	}

	/**
	 * Gets edge properties from a local repository connection.
	 *
	 * @param rc Repository connection: main interface for updating data in and
	 * performing queries on a Sesame repository.
	 * @param containsRelation String that shows the relation for the property
	 * query.
	 * @param gdm Graph playsheet where edge properties are added.
	 */
	public static void genEdgePropertiesLocal( RepositoryConnection rc, String containsRelation, GraphDataModel gdm ) {
		String query
				= "SELECT ?edge ?prop ?value ?outNode ?inNode WHERE {"
				+ "  {?prop     a                  " + containsRelation + ";}"
				+ "  {?edge     rdfs:subPropertyOf <" + DIHelper.getRelationURI().stringValue() + ">;}"
				+ "  {?outNode  a                  <" + DIHelper.getConceptURI().stringValue() + ">; }"
				+ "  {?inNode   a                  <" + DIHelper.getConceptURI().stringValue() + ">; }"
				+ "  {?edge     ?prop              ?value} "
				+ "  {?outNode  ?edge              ?inNode} "
				+ "}";

		final int size[] = { 0 };
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				gdm.addEdgeProperty( set.getValue( "edge" ).toString(),
						set.getValue( "value" ).toString(),
						set.getValue( "prop" ).toString(),
						set.getValue( "outNode" ).toString(),
						set.getValue( "inNode" ).toString() );
				size[0]++;
			}
		};

		AbstractSesameEngine.getSelectNoEx( vqa, rc, true );
		logger.debug( "genEdgePropertiesLocal added " + size[0] + " edge properties." );
	}

	/**
	 * Add results from a query on an engine to the respository connection.
	 *
	 * @param fromEngine Engine where data is stored.
	 * @param query Query to be run.
	 * @param ps Graph playsheet where sesame construct statement is stored.
	 */
	private static int addResultsToRC( IEngine engine, String query, GraphDataModel gdm ) {
		int numResults = 0;

		Collection<SesameJenaConstructStatement> sjsc = runSesameJenaConstruct( engine, query );
		for ( SesameJenaConstructStatement st : sjsc ) {
			gdm.addToSesame( st, false );
			numResults++;
		}

		return numResults;
	}

	/**
	 * Adds data to a specified engine
	 *
	 * @param engine Engine where data is stored.
	 * @param toRC Main interface for updating data in and performing queries on a
	 * Sesame repository.
	 */
	public static void addAllData( IEngine engine, RepositoryConnection toRC ) {
		addOrRemoveAllData( engine, toRC, true );
	}

	/**
	 * Removes all data from a certain engine.
	 *
	 * @param engine Engine where data is stored.
	 * @param toRC Main interface for updating data in and performing queries on a
	 * Sesame repository.
	 */
	public static void removeAllData( IEngine engine, RepositoryConnection toRC ) {
		addOrRemoveAllData( engine, toRC, false );
	}

	private static void addOrRemoveAllData( IEngine engine, RepositoryConnection toRC, boolean add ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {"
				+ "  {?Subject ?Predicate ?Object} "
				+ "}";

		try {
			Model m = engine.construct( new ModelQueryAdapter( query ) );
			if ( add ) {
				toRC.add( m );
			}
			else {
				toRC.remove( m );
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e );
		}
	}

	public static SesameJenaSelectWrapper runSesameJenaSelectWrapper( RepositoryConnection rc, String query ) {
		IEngine engine = new InMemorySesameEngine( rc );
		return runSesameJenaSelectWrapper( engine, query );
	}

	public static SesameJenaSelectWrapper runSesameJenaSelectWrapper( IEngine engine, String query ) {
		logger.debug( "Running query in runSesameJenaSelectWrapper:" + query );
		SesameJenaSelectWrapper sjsc = new SesameJenaSelectWrapper();

		try {
			sjsc.setEngine( engine );
			sjsc.setQuery( query );
			sjsc.executeQuery();
			sjsc.getVariables();
		}
		catch ( Exception ex ) {
			logger.error( ex );
		}

		return sjsc;
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameJenaSelectCheater( RepositoryConnection rc, String query ) {
		IEngine engine = new InMemorySesameEngine( rc );
		Collection<SesameJenaConstructStatement> list
				= runSesameJenaSelectCheater( engine, query );
		engine.closeDB();
		return list;
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameJenaConstruct( RepositoryConnection rc, String query ) {
		IEngine engine = new InMemorySesameEngine( rc );
		Collection<SesameJenaConstructStatement> stmt
				= runSesameJenaConstruct( engine, query );
		engine.closeDB();
		return stmt;
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameConstructOrSelectQuery( IEngine engine, String query ) {
		if ( query.toUpperCase().startsWith( "CONSTRUCT" ) ) {
			return runSesameJenaConstruct( engine, query );
		}

		return runSesameJenaSelectCheater( engine, query );
	}

	public static Collection<SesameJenaConstructStatement> runSesameJenaSelectCheater( IEngine engine, String query ) {
		logger.debug( "Running query in runSesameJenaSelectCheater: " + query );

		ListQueryAdapter<SesameJenaConstructStatement> lqa
				= new ListQueryAdapter<SesameJenaConstructStatement>( query ) {

					@Override
					public void handleTuple( BindingSet set, ValueFactory fac ) {
						// we're simulating a CONSTRUCT query, so just assume the 
						// first binding is subject, second is predicate, and third is object
						SesameJenaConstructStatement stmt = new SesameJenaConstructStatement();

						Iterator<Binding> it = set.iterator();

						stmt.setSubject( it.next().toString() );
						stmt.setPredicate( it.next().toString() );
						stmt.setObject( it.next().toString() );
						add( stmt );
					}
				};

		try {
			return engine.query( lqa );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}

		return new ArrayList<>();
	}

	public static Collection<SesameJenaConstructStatement> 
		runSesameJenaConstruct( IEngine engine, String query ) {
		logger.debug( "Running query in runSesameJenaConstruct: " + query );

		List<SesameJenaConstructStatement> list = new ArrayList<>();
		try {
			Model model = engine.construct( new ModelQueryAdapter( query ) );
			for ( Statement s : model ) {
				SesameJenaConstructStatement sjcs = new SesameJenaConstructStatement();
				sjcs.setSubject( s.getSubject().toString() );
				sjcs.setPredicate( s.getPredicate().toString() );
				sjcs.setObject( s.getObject().toString() );
				list.add( sjcs );
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}

		return list;
	}

	private static SesameJenaConstructWrapper runSesameQuery( SesameJenaConstructWrapper sjsc, IEngine engine, String query ) {
		try {
			sjsc.setEngine( engine );
			sjsc.setQuery( query );
			sjsc.execute();

			if ( !sjsc.hasNext() ) {
				logger.debug( "Came into not having ANY data" );
			}
		}
		catch ( Exception ex ) {
			logger.error( ex, ex );
		}

		return sjsc;
	}
}
