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
package gov.va.semoss.rdf.engine.api;

import gov.va.semoss.util.Constants;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import org.openrdf.model.Statement;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.util.UriBuilder;
import java.util.Map;
import org.openrdf.model.Model;

/**
 * This interface standardizes the functionality of the engines to be used. All
 * current engines must implement this interface so that they can be used
 * without first recognizing what specific engine class it is. A lot of
 * different classes call on IEngine to refer to a specific engine, including,
 * most notably, ProcessQueryListener.
 *
 * @author karverma
 * @version $Revision: 1.0 $
 */
public interface IEngine {

	/**
	 * This specifies the type of the engine and determines what API should be
	 * used when processing the engine.
	 *
	 * @author karverma
	 * @version $Revision: 1.0 $
	 */
	public enum ENGINE_TYPE {

		JENA, SESAME
	};

	/**
	 * Opens a database as defined by these properties. What is included in the
	 * properties is dependent on the type of engine that is being initiated. This
	 * is the function that first initializes an engine. One property in
	 * particular should ALWAYS be included in the Properties argument:
	 * {@link Constants#SMSS_LOCATION}. Also, {@link Constants#SMSS_SEARCHPATH} is
	 * a semicolon-delimited set of paths that can be set to specify where to look
	 * for supporting files listed in the Properties.
	 *
	 * @param props contains all information regarding the data store and how the
	 * engine should be instantiated. Dependent on what type of engine is being
	 * instantiated.
	 */
	public void openDB( Properties props );

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	public void closeDB();

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
	public Object execGraphQuery( String query );

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
	public Object execSelectQuery( String query );

	/**
	 * Gets the type of the engine. The engine type is often used to determine
	 * what API to use while running queries against the engine.
	 *
	 * @return the type of the engine
	 */
	public ENGINE_TYPE getEngineType();

	/**
	 * Processes a SELECT query just like {@link #execSelectQuery(String)}, but
	 * returns a collection of URIs instead of an Object
	 *
	 * @param sparqlQuery the SELECT SPARQL query to be run against the engine
	 *
	 * @return the uris that satisfy the query
	 */
	public Collection<org.openrdf.model.URI> getEntityOfType( String sparqlQuery );

	/**
	 * Returns whether or not an engine is currently connected to the data store.
	 * The connection becomes true when {@link #openDB(String)} is called and the
	 * connection becomes false when {@link #closeDB()} is called.
	 *
	 * @return true if the engine is connected to its data store and false if it
	 * is not
	 */
	public boolean isConnected();

	/**
	 * Makes the engine calculate inferences, if the engine supports such
	 * calculations. This function also commits the current transaction
	 *
	 * @throws org.openrdf.repository.RepositoryException if anything goes wrong
	 */
	public void calculateInferences() throws RepositoryException;

	/**
	 * Sets the name of the engine. This may be a lot of times the same as the
	 * Repository Name
	 *
	 * @param engineName - Name of the engine that this is being set to
	 */
	public void setEngineName( String engineName );

	/**
	 * Gets the engine name for this engine
	 *
	 * @return Name of the engine it is being set to
	 */
	public String getEngineName();

	/**
	 * Commit the database. Commits the active transaction. This operation ends
	 * the active transaction.
	 */
	public void commit();

	/**
	 * Gets a prefix-to-namespace mapping
	 *
	 * @return
	 */
	public Map<String, String> getNamespaces();

	/**
	 * Sets the start of the URI to be used for separating OWL statements from
	 * data statements (e.g., http://semoss.org/ontologies)
	 *
	 * @param bldr the start of the URI. If it doesn't end in a trailing slash,
	 * one will be added
	 */
	public void setSchemaBuilder( UriBuilder bldr );

	public void setDataBuilder( UriBuilder bldr );

	/**
	 * Gets a URI builder for constructing OWL metamodel URIs
	 *
	 * @return the default uri for OWL statementsfor this engine, or null, if one
	 * has not been set
	 */
	public UriBuilder getSchemaBuilder();

	/**
	 * Gets the data URI builder for this engine.
	 *
	 * @return the default uri for instances for this engine, or null, if one has
	 * not been set
	 */
	public UriBuilder getDataBuilder();

	/**
	 * Gets the OWL statements for this engine
	 *
	 * @return
	 */
	public Collection<Statement> getOwlData();

	/**
	 * Sets the OWL statements for this engine
	 *
	 * @param stmts
	 */
	public void setOwlData( Collection<Statement> stmts );

	public void addOwlData( Collection<Statement> stmts );

	/**
	 * Adds the given statement to the OWL data for this engine
	 *
	 * @param stmt
	 */
	public void addOwlData( Statement stmt );

	/**
	 * Removes the given statement from the OWL data
	 *
	 * @param stmt
	 */
	public void removeOwlData( Statement stmt );

	// get property
	public String getProperty( String key );

	/**
	 * Returns a copy of this engine's properties
	 *
	 * @return
	 */
	public Properties getProperties();

	/**
	 * Sets/clears a property
	 *
	 * @param key the key to set/clear
	 * @param val the value. If null or empty, the property key is removed from
	 * the properties. Else, the property is set to this value
	 */
	public void setProperty( String key, String val );

	public <T> T query( QueryExecutor<T> exe )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException;

	/**
	 * Runs a query but does not throw exceptions. If anything exceptional
	 * happens, this function return null
	 *
	 * @param <T> the type of result
	 * @param exe the query executor to run
	 * @return the results, or null if something bad happened
	 */
	public <T> T queryNoEx( QueryExecutor<T> exe );

	/**
	 * Runs an update on the engine
	 *
	 * @param ue
	 */
	public void update( UpdateExecutor ue ) throws RepositoryException,
			MalformedQueryException, UpdateExecutionException;

	public Model construct( QueryExecutor<Model> query )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException;

	/**
	 * Executes the given modification on the repository connection. The block
	 * will be executed within a transaction.
	 *
	 * @param exe the logic to execute
	 *
	 * @throws RepositoryException
	 */
	public void execute( ModificationExecutor exe ) throws RepositoryException;

	/**
	 * Does this engine support starting a network server?
	 *
	 * @return true, if this engine can be converted to server mode
	 */
	public boolean isServerSupported();

	/**
	 * Is the network server running?
	 *
	 * @return
	 */
	public boolean serverIsRunning();

	public void startServer( int port );

	public void stopServer();

	/**
	 * If the server is running, what's its address?
	 *
	 * @return the server URI, or null, if the server isn't running
	 */
	public URI getServerUri();

	/**
	 * Gets the base URI for this engine. Usually, this will be set in the
	 * properties file.
	 *
	 * @return the base uri for this engine, or null, if one has not been set
	 */
	public org.openrdf.model.URI getBaseUri();

	public WriteableInsightManager getWriteableInsightManager();

	// gets the insight database
	public InsightManager getInsightManager();

	public void setInsightManager( InsightManager eng );
}
