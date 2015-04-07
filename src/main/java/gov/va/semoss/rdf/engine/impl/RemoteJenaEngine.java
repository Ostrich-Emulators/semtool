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
package gov.va.semoss.rdf.engine.impl;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

/**
 * References the RDF source from a remote engine and uses the Jena API to query a database stored in an RDF file (.jnl file).
 */
public class RemoteJenaEngine extends AbstractEngine implements IEngine {
	
	Model jenaModel = null;
	Logger logger = Logger.getLogger(getClass());
	String propFile = null;
	String serviceURI = null;
	boolean connected = false;

	/**
	 * Closes the data base associated with the engine.  This will prevent further changes from being made in the data store and 
	 * safely ends the active transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		jenaModel.close();
		logger.info("Closing the database to the file " + propFile);		
	}

	/**
	 * Runs the passed string query against the engine and returns graph query results.  The query passed must be in the structure 
	 * of a CONSTRUCT SPARQL query.  The exact format of the results will be 
	 * dependent on the type of the engine, but regardless the results are able to be graphed.
	 * @param query the string version of the query to be run against the engine
	
	 * @return the graph query results */
	@Override
	public Object execGraphQuery(String query) {
	
		com.hp.hpl.jena.query.Query queryVar = QueryFactory.create(query) ;
		
		QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(this.serviceURI, queryVar);
		String params = prop.getProperty(Constants.URL_PARAM);
		StringTokenizer paramTokens = new StringTokenizer(params, ";");
		while(paramTokens.hasMoreTokens())
		{
			String token = paramTokens.nextToken();
			qexec.addParam(token, prop.getProperty(token));			
		}
		Model resultModel = qexec.execConstruct() ;
		logger.info("Executing the RDF File Graph Query " + query);
		return resultModel;
		//qexec.close() ;
	}

	/**
	 * Runs the passed string query against the engine as a SELECT query.  The query passed must be in the structure of a SELECT 
	 * SPARQL query and the result format will depend on the engine type.
	 * @param query the string version of the SELECT query to be run against the engine
	
	 * @return triple query results that can be displayed as a grid */
	@Override
	public Object execSelectQuery(String query) {
		
		com.hp.hpl.jena.query.Query q2 = QueryFactory.create(query); 
		com.hp.hpl.jena.query.Query queryVar = QueryFactory.create(query) ;
		
		QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(this.serviceURI, queryVar);
		String params = prop.getProperty(Constants.URL_PARAM);
		StringTokenizer paramTokens = new StringTokenizer(params, ";");
		while(paramTokens.hasMoreTokens())
		{
			String token = paramTokens.nextToken();
			qexec.addParam(token, prop.getProperty(token));			
		}
		com.hp.hpl.jena.query.ResultSet rs = qexec.execSelect();
		return rs;
	}

	/**
	 * Runs the passed string query against the engine as an INSERT query.  The query passed must be in the structure of an INSERT 
	 * SPARQL query or an INSERT DATA SPARQL query 
	 * and there are no returned results.  The query will result in the specified triples getting added to the 
	 * data store.
	 * @param query the INSERT or INSERT DATA SPARQL query to be run against the engine
	 */
	@Override
	public void execInsertQuery(String query) {
		// TODO Auto-generated method stub		
	}

	/**
	 * Gets the type of the engine.  The engine type is often used to determine what API to use while running queries against the 
	 * engine.
	
	 * @return the type of the engine */
	@Override
	public ENGINE_TYPE getEngineType() {
		return IEngine.ENGINE_TYPE.JENA;
	}

	@Override
	public Collection<URI> getEntityOfType(String sparqlQuery) {
		// run the query 
		// convert to string
		List<URI> retString = new ArrayList<>();
    ResultSet rs = (ResultSet) execSelectQuery( sparqlQuery );

    // gets only the first variable
    Iterator varIterator = rs.getResultVars().iterator();
    String varName = (String) varIterator.next();
    while ( rs.hasNext() ) {
      QuerySolution row = rs.next();
      retString.add( new URIImpl( row.get( varName ).toString() ) );
    }
    return retString;
	}

	/**
	 * Returns whether or not an engine is currently connected to the data store.  The connection becomes true when {@link #openDB(String)} 
	 * is called and the connection becomes false when {@link #closeDB()} is called.	
	 * @return true if the engine is connected to its data store and false if it is not */
	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void startLoading(Properties props ) throws RepositoryException {
		this.serviceURI = prop.getProperty(Constants.SPARQL_QUERY_ENDPOINT);
		this.connected = true;
	}

	@Override
	public boolean execAskQuery(String query) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}
}
