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


import gov.va.semoss.rdf.engine.api.IEngine;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateAction;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * Holds the database in memory, and uses the Jena API to facilitate querying of RDF data sources.
 */
public class InMemoryJenaEngine extends AbstractEngine implements IEngine {
  private static final Logger log = Logger.getLogger(InMemoryJenaEngine.class);
	Model jenaModel = null;

  @Override
  public void openDB( Properties props ) {
    // no meaning to this now
  }

	/**
	 * Closes the data base associated with the engine.  This will prevent further changes from being made in the data store and 
	 * safely ends the active transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		// do nothing
	}

	/**
	 * Runs the passed string query against the engine and returns graph query results.  The query passed must be in the structure 
	 * of a CONSTRUCT SPARQL query.  The exact format of the results will be 
	 * dependent on the type of the engine, but regardless the results are able to be graphed.
	 * @param query the string version of the query to be run against the engine
	
	 * @return the graph query results */
	@Override
	public Object execGraphQuery(String query) {
		Model model = null;
		try{
			com.hp.hpl.jena.query.Query q2 = QueryFactory.create(query); 
			QueryExecution qex = QueryExecutionFactory.create(q2, jenaModel);
			model = qex.execConstruct();
		}catch(Exception e){
			// TODO: Specify exception
			log.error( e );
		}
		
		return model;
	}

	/**
	 * Runs the passed string query against the engine as a SELECT query.  The query passed must be in the structure of a SELECT 
	 * SPARQL query and the result format will depend on the engine type.
	 * @param query the string version of the SELECT query to be run against the engine
	
	 * @return triple query results that can be displayed as a grid */
	@Override
	public Object execSelectQuery(String query) {
		ResultSet rs = null;
		try{
			//QueryExecutionFactory.
			com.hp.hpl.jena.query.Query q2 = QueryFactory.create(query); 
			QueryExecution qex = QueryExecutionFactory.create(q2, jenaModel);
			rs = qex.execSelect();
		}catch (Exception e){
			log.error( e );
		}
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
		UpdateAction.parseExecute(query, jenaModel);

	}

	/**
	 * Method setModel. Sets the jena Model to the parameter jena model.
	 * @param jenaModel Model - Name of the model that this is being set to.
	 */
	public void setModel(Model jenaModel) {
		this.jenaModel = jenaModel;

	}
	
	/**
	 * Gets the type of the engine.  The engine type is often used to determine what API to use while running queries against the 
	 * engine.	
	 * @return the type of the engine */
	@Override
	public ENGINE_TYPE getEngineType() {
		return ENGINE_TYPE.JENA;
	}

	@Override
	public Collection<URI> getEntityOfType(String sparqlQuery) {
		// TODO: Don't return null
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	/**
	 * Returns whether or not an engine is currently connected to the data store.  The connection becomes true when {@link #openDB(String)} 
	 * is called and the connection becomes false when {@link #closeDB()} is called.
	
	 * @return true if the engine is connected to its data store and false if it is not */
	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean execAskQuery(String query) {
		// TODO: Don't return null
    throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	protected void loadLegacyOwl( String ontoloc ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected InsightManager createInsightManager() throws RepositoryException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected void updateLastModifiedDate() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean supportsSparqlBindings() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addStatement( String subject, String predicate, Object object, boolean concept ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void removeStatement( String subject, String predicate, Object object, boolean concept ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Map<String, String> getNamespaces() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Collection<Statement> getOwlData() {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setOwlData( Collection<Statement> stmts ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addOwlData( Collection<Statement> stmts ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void addOwlData( Statement stmt ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void removeOwlData( Statement stmt ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T> T query( QueryExecutor<T> exe ) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public org.openrdf.model.Model construct( String query ) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void execute( ModificationExecutor exe ) throws RepositoryException {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}

}
