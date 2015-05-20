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
import gov.va.semoss.rdf.engine.impl.SesameJenaConstructStatement;
import gov.va.semoss.rdf.engine.impl.SesameJenaSelectStatement;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.ModelQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;

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
	public static void loadConceptHierarchy( IEngine engine, Collection<URI> subjects,
			GraphDataModel gdm ) {

		String subs = Utility.implode( subjects, "<", ">", " " );

		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object } WHERE {"
				+ "  ?Subject a ?Object ."
				+ "  ?Subject ?Predicate ?Object ."
				+ "  FILTER ( isURI( ?Object ) )"
				+ "} VALUES ?Subject { " + subs + " }";

		int numResults = addResultsToRC( engine, query, gdm );
		logger.debug( "loadConceptHierarchy added " + numResults + " results to the sesame rc." );
	}

	public static void loadRelationHierarchy( IEngine engine, Collection<URI> predicates,
			GraphDataModel gdm ) {
		String preds = Utility.implode( predicates, "<", ">", " " );

		String query
				= "CONSTRUCT { ?s ?p ?o } WHERE {"
				+ "  ?s ?p ?o ."
				+ "  ?s a owl:ObjectProperty ."
				+ "} VALUES ?s { " + preds + " } ";
		int n2 = addResultsToRC( engine, query, gdm );

		logger.debug( "loadRelationHierarchy added " + n2 + " results to the sesame rc." );
	}

	public static void loadPropertyHierarchy( IEngine engine, Collection<URI> predicates,
			GraphDataModel gdm ) {

		// RPB: I don't know what this function is for. It's basically
		// the same as loadRelationHierarchy(), above
		String rel = engine.getSchemaBuilder().getRelationUri().toString();
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {"
				+ "  ?Subject ?Predicate ?Object ."
				+ "} "
				+ "VALUES ?Subject { " + Utility.implode( predicates, "<", ">", " " ) + " } ";

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
	public static void genPropertiesRemote( IEngine engine, Collection<URI> subjects,
			GraphDataModel gdm ) {

		String query
				= "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER ( isLiteral( ?o ) ) } "
				+ "VALUES ?s { " + Utility.implode( subjects, "<", ">", " " ) + " }";

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
	public static void genNodePropertiesLocal( RepositoryConnection rc,
			GraphDataModel gdm ) {
		String query
				= "CONSTRUCT { ?Subject ?Predicate ?Object} WHERE {"
				+ "  ?Predicate a <" + DIHelper.getRelationURI().stringValue() + "> ."
				+ "  ?Subject   a <" + DIHelper.getConceptURI().stringValue() + "> ."
				+ "  ?Subject   ?Predicate ?Object"
				+ "}";
		// ModelQueryAdapter mqa = new ModelQueryAdapter( query );

		Collection<SesameJenaConstructStatement> sjsc = runSesameJenaConstruct( rc, query );
		for ( SesameJenaConstructStatement s : sjsc ) {
			SEMOSSVertex vertex = gdm.createOrRetrieveVertex( s.getSubject() );
			vertex.setProperty( s.getPredicate(), s.getObject() );
			gdm.storeVertex( vertex );
		}
		logger.debug( "genNodePropertiesLocal added " + sjsc.size() + " node properties." );
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
	public static void genEdgePropertiesLocal( RepositoryConnection rc,
			GraphDataModel gdm ) {
		String query
				= "SELECT ?edge ?prop ?value ?outNode ?inNode WHERE {"
				+ "  ?edge     a     ?reltype ."
				+ "  ?outNode  a     ?concept ."
				+ "  ?inNode   a     ?concept ."
				+ "  ?edge     ?prop ?value . "
				+ "  ?outNode  ?edge ?inNode . "
				+ "}";

		final int size[] = { 0 };
		VoidQueryAdapter vqa = new VoidQueryAdapter( query ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				gdm.addEdgeProperty( set.getValue( "edge" ).stringValue(),
						set.getValue( "value" ).stringValue(),
						set.getValue( "prop" ).stringValue(),
						set.getValue( "outNode" ).stringValue(),
						set.getValue( "inNode" ).stringValue() );
				size[0]++;
			}
		};

		vqa.bind( "reltype", DIHelper.getRelationURI() );
		vqa.bind( "concept", DIHelper.getConceptURI() );

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
			gdm.addToSesame( st );
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
				+ "  ?Subject ?Predicate ?Object"
				+ "}";

		try {
			ModelQueryAdapter mqa = new ModelQueryAdapter( query );
			mqa.useInferred( false );
			Model m = engine.construct( mqa );
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

	public static Collection<SesameJenaSelectStatement>
			runSesameJenaSelectWrapper( RepositoryConnection rc, String query ) {
		return AbstractSesameEngine.getSelectNoEx( getSjssAdapter( query ), rc, true );
	}

	public static Collection<SesameJenaSelectStatement>
			runSesameJenaSelectWrapper( IEngine engine, String query ) {
		logger.debug( "Running query in runSesameJenaSelectWrapper:" + query );

		try {
			return engine.query( getSjssAdapter( query ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException ex ) {
			logger.error( ex );
		}

		return new ArrayList<>();
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameJenaSelectCheater( RepositoryConnection rc, String query ) {
		return AbstractSesameEngine.getSelectNoEx( getSjssAdapter_c( query ), rc, true );
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameJenaConstruct( RepositoryConnection rc, String query ) {
		try {
			ModelQueryAdapter mqa = new ModelQueryAdapter( query );
			mqa.useInferred( false );
			return toList( AbstractSesameEngine.getConstruct( mqa, rc ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}
		return new ArrayList<>();
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameConstructOrSelectQuery( IEngine engine, String query ) {
		if ( query.toUpperCase().startsWith( "CONSTRUCT" ) ) {
			return runSesameJenaConstruct( engine, query );
		}

		return runSesameJenaSelectCheater( engine, query );
	}

	/**
	 * Runs a SELECT query, but returns data as if it were a CONSTRUCT query
	 *
	 * @param engine
	 * @param query
	 * @return
	 */
	public static Collection<SesameJenaConstructStatement>
			runSesameJenaSelectCheater( IEngine engine, String query ) {
		logger.debug( "Running query in runSesameJenaSelectCheater: " + query );

		try {
			return engine.query( getSjssAdapter_c( query ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}

		return new ArrayList<>();
	}

	public static Collection<SesameJenaConstructStatement>
			runSesameJenaConstruct( IEngine engine, String query ) {
		logger.debug( "Running query in runSesameJenaConstruct: " + query );

		try {
			ModelQueryAdapter mqa = new ModelQueryAdapter( query );
			mqa.useInferred( false );
			return toList( engine.construct( mqa ) );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			logger.error( e, e );
		}

		return new ArrayList<>();
	}

	private static ListQueryAdapter<SesameJenaSelectStatement> getSjssAdapter( String query ) {
		ListQueryAdapter<SesameJenaSelectStatement> q
				= new ListQueryAdapter<SesameJenaSelectStatement>( query ) {

					@Override
					public void handleTuple( BindingSet set, ValueFactory fac ) {
						SesameJenaSelectStatement stmt = new SesameJenaSelectStatement();
						for ( Binding b : set ) {
							stmt.setRawVar( b.getName(), b.getValue() );
						}
						add( stmt );
					}
				};
		q.useInferred( false );
		return q;
	}

	private static ListQueryAdapter<SesameJenaConstructStatement> getSjssAdapter_c( String query ) {
		ListQueryAdapter<SesameJenaConstructStatement> q
				= new ListQueryAdapter<SesameJenaConstructStatement>( query ) {

					@Override
					public void handleTuple( BindingSet set, ValueFactory fac ) {
						if ( 3 == set.size() ) {
					// we're simulating a CONSTRUCT query, so just assume the 
							// first binding is subject, second is predicate, and third is object
							SesameJenaConstructStatement stmt = new SesameJenaConstructStatement();

							Iterator<Binding> it = set.iterator();

							stmt.setSubject( it.next().getValue().toString() );
							stmt.setPredicate( it.next().getValue().toString() );
							stmt.setObject( it.next().getValue() );
							add( stmt );
						}
						else {
							logger.error( "Could not simulate a CONSTRUCT statement (vars!=3) from: "
									+ query );
						}
					}
				};
		
		q.useInferred( false );
		return q;
	}

	private static SesameJenaConstructStatement toSjcs( Statement s ) {
		SesameJenaConstructStatement sjcs = new SesameJenaConstructStatement();
		sjcs.setSubject( s.getSubject().stringValue() );
		sjcs.setPredicate( s.getPredicate().stringValue() );
		sjcs.setObject( s.getObject() );
		return sjcs;
	}

	private static List<SesameJenaConstructStatement> toList( Model m ) {
		List<SesameJenaConstructStatement> list = new ArrayList<>();
		for ( Statement s : m ) {
			list.add( toSjcs( s ) );
		}
		return list;
	}
}
