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
package com.ostrichemulators.semtool.rdf.engine.api;

import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.util.Constants;
import java.util.Properties;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.util.Map;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

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
	 * Opens a database as defined by these properties. What is included in the
	 * properties is dependent on the type of engine that is being initiated. This
	 * is the function that first initializes an engine. One property in
	 * particular should ALWAYS be included in the Properties argument:
	 * {@link Constants#SMSS_LOCATION}.
	 *
	 * @param props contains all information regarding the data store and how the
	 * engine should be instantiated. Dependent on what type of engine is being
	 * instantiated.
	 * @throws org.openrdf.repository.RepositoryException if something goes wrong
	 */
	public void openDB( Properties props ) throws RepositoryException;

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	public void closeDB();

	/**
	 * Returns whether or not an engine is currently connected to the data store.
	 * The connection becomes true when {@link #openDB(java.util.Properties)} is
	 * called and the connection becomes false when {@link #closeDB()} is called.
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
	 * data statements (e.g., http://os-em.com/ontologies/semtool/)
	 *
	 * @param bldr the start of the URI. If it doesn't end in a trailing slash,
	 * one will be added
	 */
	public void setSchemaBuilder( UriBuilder bldr );

	/**
	 * Sets the start of the URI to be used for separating data statements from
	 * OWL statements. Defaults to using the namespace of {@link #getBaseUri()}
	 *
	 * @param bldr
	 */
	public void setDataBuilder( UriBuilder bldr );

	/**
	 * Gets a URI builder for constructing OWL metamodel URIs
	 *
	 * @return the default uri for OWL statements for this engine, or null, if one
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
	 * Gets a single property from {@link #getProperties()}
	 *
	 * @param key
	 * @return
	 */
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
	 * Runs an update on the engine.
	 *
	 * @param ue
	 * @throws org.openrdf.repository.RepositoryException
	 * @throws org.openrdf.query.MalformedQueryException
	 * @throws org.openrdf.query.UpdateExecutionException
	 * @throws SecurityException if the user does not have enough permissions to
	 * run the update
	 */
	public void update( UpdateExecutor ue ) throws RepositoryException,
			MalformedQueryException, UpdateExecutionException;

	public Model construct( QueryExecutor<Model> query )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException;

	public Model constructNoEx( QueryExecutor<Model> query );

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
	 * Gets the base URI for this engine. Usually, this will be set in the
	 * properties file.
	 *
	 * @return the base uri for this engine, or null, if one has not been set
	 */
	public URI getBaseUri();

	/**
	 * Updates to disk the Insights for this engine
	 *
	 * @param insmgr
	 * @throws
	 * com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException
	 */
	public void updateInsights( InsightManager insmgr ) throws EngineManagementException;

	// gets the insight database
	public InsightManager getInsightManager();

	public void setInsightManager( InsightManager eng );

	/**
	 * Is this database accessed through a network?
	 * @return
	 */
	public boolean isRemote();
}
