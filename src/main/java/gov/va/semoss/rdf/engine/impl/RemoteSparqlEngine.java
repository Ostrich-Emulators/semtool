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
 *****************************************************************************
 */
package gov.va.semoss.rdf.engine.impl;

import java.util.Properties;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.sail.SailException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import com.bigdata.rdf.rules.InferenceEngine;
import com.bigdata.rdf.sail.BigdataSail;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;

/**
 * References the RDF source from a remote engine and uses the Sesame API to
 * query a database stored in an RDF file (.jnl file).
 */
public abstract class RemoteSparqlEngine extends AbstractEngine {

	private static final Logger log = Logger.getLogger( RemoteSparqlEngine.class );
	private final BigdataSail bdSail = null;
	private Properties rdfMap = null;
	private RepositoryConnection rc = null;
	private boolean connected = false;

	@Override
	public void finishLoading( Properties props ) throws RepositoryException {
		String sparqlQEndpoint = prop.getProperty( Constants.SPARQL_QUERY_ENDPOINT );

		//com.bigdata.rdf.sail.webapp.client.RemoteRepository repo = new com.bigdata.rdf.sail.webapp.client.RemoteRepository(sparqlQEndpoint, null, null);
		//repo.
		SPARQLRepository repo = new SPARQLRepository( sparqlQEndpoint );
		Map<String, String> myMap = new HashMap<>();
		myMap.put( "apikey", "d0184dd3-fb6b-4228-9302-1c6e62b01465" );
		//repo.setAdditionalHttpHeaders(myMap);
		rc = new SPARQLConnection( repo );

		rdfMap = DIHelper.getInstance().getCoreProp();

		this.connected = true;
	}

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		// ng.stopTransaction(Conclusion.SUCCESS);
		try {
			bdSail.shutDown();
			connected = false;
		}
		catch ( SailException e ) {
			log.error( e );
		}
		// ng.shutdown();
	}

	/**
	 * Runs the passed string query against the engine and returns graph query
	 * results. The query passed must be in the structure of a CONSTRUCT SPARQL
	 * query. The exact format of the results will be dependent on the type of the
	 * engine, but regardless the results are able to be graphed.
	 *
	 * @param query the string version of the query to be run against the engine
	 *
	 * @return the graph query results
	 */
	@Override
	public GraphQueryResult execGraphQuery( String query ) {
		GraphQueryResult res = null;
		try {
			GraphQuery sagq
					= rc.prepareGraphQuery( QueryLanguage.SPARQL, query );
			res = sagq.evaluate();
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e );
		}
		return res;
	}

	/**
	 * Runs the passed string query against the engine as a SELECT query. The
	 * query passed must be in the structure of a SELECT SPARQL query and the
	 * result format will depend on the engine type.
	 *
	 * @param query the string version of the SELECT query to be run against the
	 * engine
	 *
	 * @return triple query results that can be displayed as a grid
	 */
	@Override
	public TupleQueryResult execSelectQuery( String query ) {

		TupleQueryResult sparqlResults = null;

		try {
			TupleQuery tq = rc.prepareTupleQuery( QueryLanguage.SPARQL, query );
			log.debug( "SPARQL: " + query );
			//tq.setIncludeInferred(true /* includeInferred */);
			sparqlResults = tq.evaluate();
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e );
		}
		return sparqlResults;
	}

	/**
	 * Runs the passed string query against the engine as an INSERT query. The
	 * query passed must be in the structure of an INSERT SPARQL query or an
	 * INSERT DATA SPARQL query and there are no returned results. The query will
	 * result in the specified triples getting added to the data store.
	 *
	 * @param query the INSERT or INSERT DATA SPARQL query to be run against the
	 * engine
	 */
	@Override
	public void execInsertQuery( String query ) throws SailException, UpdateExecutionException, RepositoryException, MalformedQueryException {

		Update up = rc.prepareUpdate( QueryLanguage.SPARQL, query );
		//sc.addStatement(vf.createURI("<http://semoss.org/ontologies/Concept/Service/tom2>"),vf.createURI("<http://semoss.org/ontologies/Relation/Exposes>"),vf.createURI("<http://semoss.org/ontologies/Concept/BusinessLogicUnit/tom1>"));
		log.debug( "SPARQL: " + query );
		//tq.setIncludeInferred(true /* includeInferred */);
		//tq.evaluate();
		rc.begin();
		InferenceEngine ie = bdSail.getInferenceEngine();
		ie.computeClosure( null );
		rc.commit();

	}

	@Override
	public boolean execAskQuery( String query ) {
		BooleanQuery bq;
		boolean response = false;
		try {
			bq = rc.prepareBooleanQuery( QueryLanguage.SPARQL, query );
			log.debug( "SPARQL: " + query );
			response = bq.evaluate();
		}
		catch ( MalformedQueryException | RepositoryException | QueryEvaluationException e ) {
			log.error( e );
		}

		return response;
	}

	/**
	 * Gets the type of the engine. The engine type is often used to determine
	 * what API to use while running queries agains the engine.
	 *
	 * @return the type of the engine
	 */
	@Override
	public ENGINE_TYPE getEngineType() {
		return IEngine.ENGINE_TYPE.SESAME;
	}

	@Override
	public Collection<URI> getEntityOfType( String sparqlQuery ) {
		OneVarListQueryAdapter<URI> uris
				= OneVarListQueryAdapter.getUriList( sparqlQuery, Constants.ENTITY );
		return AbstractSesameEngine.getSelectNoEx( uris, rc, false );
	}

	/**
	 * Returns whether or not an engine is currently connected to the data store.
	 * The connection becomes true when {@link #openDB(String)} is called and the
	 * connection becomes false when {@link #closeDB()} is called.
	 *
	 * @return true if the engine is connected to its data store and false if it
	 * is not
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}
}
