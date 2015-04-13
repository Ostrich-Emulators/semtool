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

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import de.fuberlin.wiwiss.d2rq.jena.ModelD2RQ;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Connects to an RDBMS and facilitates query execution.
 */
public abstract class RDBMSD2RQEngine extends AbstractEngine {

	private static final Logger logger = Logger.getLogger( RDBMSD2RQEngine.class );
	Model d2rqModel = null;
	String propFile = null;
	Properties map;
	boolean connected = false;

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		d2rqModel.close();
		logger.info( "Closed the RDBMS Database " + propFile );
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
	public Object execGraphQuery( String query ) {
		logger.info( "EXEC GRAPH QUERY: " + query );
		Query q = QueryFactory.create( query );
		QueryExecution qexec = QueryExecutionFactory.create( q, d2rqModel );
		Model resultModel = qexec.execConstruct();
		return resultModel;
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
	public ResultSet execSelectQuery( String query ) {
		logger.info( "EXEC SELECT QUERY: " + query );
		Query q = QueryFactory.create( query );
		QueryExecution qexec = QueryExecutionFactory.create( q, d2rqModel );
		ResultSet rs = qexec.execSelect();
		return rs;
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
	public void execInsertQuery( String query ) {
		UpdateRequest update = UpdateFactory.create( query );
		UpdateAction.execute( update, d2rqModel );
	}

	/**
	 * Gets the type of the engine. The engine type is often used to determine
	 * what API to use while running queries against the engine. D2RQ uses the
	 * JENA API so we return an engine type of JENA.
	 *
	 * @return the type of the engine
	 */
	@Override
	public ENGINE_TYPE getEngineType() {
		return IEngine.ENGINE_TYPE.JENA;
	}

	@Override
	public Collection<URI> getEntityOfType( String sparqlQuery ) {
		logger.debug( "ENTITY OF TYPE QUERY: " + sparqlQuery );

		List<URI> retString = new ArrayList<>();
		ResultSet rs = (ResultSet) execSelectQuery( sparqlQuery );

		String varName = Constants.ENTITY;
		while ( rs.hasNext() ) {
			QuerySolution row = rs.next();
			retString.add( new URIImpl( row.get( varName ) + "" ) );
		}

		return retString;
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

	@Override
	public void openDB( Properties props ) {
		if ( this.map != null ) {
			d2rqModel
					= new ModelD2RQ( DIHelper.getInstance().getProperty( Constants.BASE_FOLDER )
							+ "/" + this.map );
			if ( d2rqModel != null ) {
				this.connected = true;
			}
			super.openDB( props );
		}
	}

	@Override
	public boolean execAskQuery( String query ) {
		Query q = QueryFactory.create( query );
		QueryExecution qexec = QueryExecutionFactory.create( q, d2rqModel );
		boolean response = qexec.execAsk();
		return response;
	}
}
