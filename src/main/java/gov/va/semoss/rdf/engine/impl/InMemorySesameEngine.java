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
package gov.va.semoss.rdf.engine.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
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

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.Namespace;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Holds the database in memory, and uses the Sesame API to facilitate querying
 * of RDF data sources.
 */
public class InMemorySesameEngine extends AbstractEngine implements IEngine {

	private static final Logger log = Logger.getLogger( InMemorySesameEngine.class );
	private RepositoryConnection rc = null;
	private boolean connected = false;
	private boolean iControlMyRc = false;

	public InMemorySesameEngine() {
		SailRepository repo = new SailRepository( new MemoryStore() );
		try {
			repo.initialize();
			rc = repo.getConnection();
		}
		catch ( Exception e ) {
			try {
				repo.shutDown();
			}
			catch ( Exception ex ) {
				log.error( ex, ex );
			}
		}

		setRepositoryConnection( rc );
		iControlMyRc = true;
	}

	public InMemorySesameEngine( RepositoryConnection rc ) {
		setRepositoryConnection( rc );
	}

	/**
	 * Method setRepositoryConnection. Sets the repository connection.
	 *
	 * @param rc RepositoryConnection. The repository connection that this is
	 * being set to.
	 */
	public final void setRepositoryConnection( RepositoryConnection rc ) {
		this.rc = rc;

		try {
			startLoading( new Properties() );
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}
	}

	public void setBuilders( UriBuilder data, UriBuilder schema ) {
		this.setDataBuilder( data );
		this.setSchemaBuilder( schema );
	}

	@Override
	public void setDataBuilder( UriBuilder data ) {
		super.setDataBuilder( data );
	}

	/**
	 * Method getRepositoryConnection. Gets the repository connection.
	 *
	 * @return RepositoryConnection - the connection to the repository.
	 */
	public RepositoryConnection getRepositoryConnection() {
		return this.rc;
	}

	@Override
	public void openDB( Properties props ) {
		// no meaning to this now
	}

	@Override
	public Map<String, String> getNamespaces() {
		Map<String, String> ret = new HashMap<>();
		try {
			for ( Namespace ns : Iterations.asList( rc.getNamespaces() ) ) {
				ret.put( ns.getPrefix(), ns.getName() );
			}
		}
		catch ( RepositoryException re ) {
			log.warn( "could not retrieve namespaces", re );
		}
		return ret;
	}

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		connected = false;

		if ( iControlMyRc ) {
			try {
				rc.close();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}

			Repository repo = rc.getRepository();
			try {
				repo.shutDown();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}
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
			GraphQuery sagq = rc.prepareGraphQuery( QueryLanguage.SPARQL,
					query );
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
			tq.setIncludeInferred( true /*
			 * includeInferred
			 */ );
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
	public void execInsertQuery( String query ) {
		Update up;
		try {
			up = rc.prepareUpdate( QueryLanguage.SPARQL, query );
			//rc.add(vf.createURI("<http://semoss.org/ontologies/Concept/Service/tom2>"),vf.createURI("<http://semoss.org/ontologies/Relation/Exposes>"),vf.createURI("<http://semoss.org/ontologies/Concept/BusinessLogicUnit/tom1>"));
			log.debug( "SPARQL: " + query );
			//tq.setIncludeInferred(true /* includeInferred */);
			//tq.evaluate();
			rc.begin();
			up.execute();
			rc.commit();
		}
		catch ( RepositoryException | UpdateExecutionException | MalformedQueryException e ) {
			try {
				rc.rollback();
			}
			catch ( Exception e2 ) {
				// ignore this
			}
			log.error( e );
		}

	}

	/**
	 * Gets the type of the engine. The engine type is often used to determine
	 * what API to use while running queries against the engine.
	 *
	 * @return the type of the engine
	 */
	@Override
	public ENGINE_TYPE getEngineType() {
		return IEngine.ENGINE_TYPE.SESAME;
	}

	@Override
	public Collection<URI> getEntityOfType( String sparqlQuery ) {
		try {
			TupleQuery tq = rc.prepareTupleQuery( QueryLanguage.SPARQL, sparqlQuery );
			log.debug( "SPARQL: " + sparqlQuery );
			tq.setIncludeInferred( true /*
			 * includeInferred
			 */ );
			TupleQueryResult sparqlResults = tq.evaluate();
			List<URI> strVector = new ArrayList<>();
			while ( sparqlResults.hasNext() ) {
				strVector.add( new URIImpl( sparqlResults.next().getValue( Constants.ENTITY ) + "" ) );
			}

			return strVector;
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e );
		}
		return null;
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
	public void addStatement( String subject, String predicate, Object object, boolean concept ) {
		try {
			URI newSub;
			URI newPred;
			ValueFactory vf = rc.getValueFactory();

			//subString = Utility.cleanString(sub, false);
			newSub = vf.createURI( subject );

			//predString = Utility.cleanString(pred, false);
			newPred = vf.createURI( predicate );

			if ( !concept ) {
				if ( object.getClass() == Double.class ) {
					log.info(
							"Found Double " + object );
					rc.add( newSub, newPred, vf.createLiteral( ( (Double) object ) ) );
				}

				else if ( object.getClass() == Date.class ) {
					log.info(
							"Found Date " + object );
					rc.add( newSub, newPred, vf.createLiteral( Date.class.cast( object ) ) );
				}
				else {
					log.info( "Found String " + object );
					String value = object + "";
					// try to see if it already has properties then add to it
					String cleanValue = value.replaceAll( "/", "-" ).replaceAll( "\"", "'" );
					rc.add( newSub, newPred, vf.createLiteral( cleanValue ) );
				}
			}
			else {
				if ( object instanceof Literal ) {
					rc.add( newSub, newPred, (Literal) object );
				}
				else if ( object instanceof URI ) {
					rc.add( newSub, newPred, (URI) object );
				}
				else {
					rc.add( newSub, newPred, (Value) object );
				}
				//else if(object instanceof URI && object.toString().startsWith("http://"))
				//	rc.add(newSub, newPred, (URI)object);

			}

		}
		catch ( Exception e ) {
			log.error( e );
		}
	}

	@Override
	public boolean execAskQuery( String query ) {
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	@Override
	public <T> T query( QueryExecutor<T> exe )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		return AbstractEngine.getSelect( exe, rc, true );
	}

	@Override
	public void execute( ModificationExecutor exe ) throws RepositoryException {
		try {
			if ( exe.execInTransaction() ) {
				rc.begin();
			}

			exe.exec( rc );

			if ( exe.execInTransaction() ) {
				rc.commit();
			}
		}
		catch ( Exception e ) {
			log.error( e );
			if ( exe.execInTransaction() ) {
				rc.rollback();
			}
		}
	}
}
