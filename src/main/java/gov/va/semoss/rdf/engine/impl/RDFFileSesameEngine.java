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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import static gov.va.semoss.rdf.engine.impl.AbstractEngine.getSelect;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

/**
 * References the RDF source and uses the Sesame API to query a database stored in an RDF file (.jnl file).
 */
public class RDFFileSesameEngine extends AbstractEngine implements IEngine {
  private static final Logger log = Logger.getLogger(RDFFileSesameEngine.class);
	Properties rdfMap = null;
  private Repository repo = null;
	public RepositoryConnection rc = null;
	private String rdfFileType = "RDF/XML";
	public String baseURI = null;
	public String fileName = null;
	boolean connected = false;
	
	@Override
	protected void startLoading( Properties prop ) throws RepositoryException {
		rdfFileType = prop.getProperty( Constants.RDF_FILE_TYPE );
		baseURI = prop.getProperty( Constants.RDF_FILE_BASE_URI );

		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		repo = new SailRepository( inferencer );
		repo.initialize();

		File file = new File( fileName );
		rc = repo.getConnection();

		try {
			if ( null != rdfFileType && file.exists() ) {
				if ( rdfFileType.equalsIgnoreCase( "RDF/XML" ) ) {
					rc.add( file, baseURI, RDFFormat.RDFXML );
				}
				else if ( rdfFileType.equalsIgnoreCase( "TURTLE" ) ) {
					rc.add( file, baseURI, RDFFormat.TURTLE );
				}
				else if ( rdfFileType.equalsIgnoreCase( "BINARY" ) ) {
					rc.add( file, baseURI, RDFFormat.BINARY );
				}
				else if ( rdfFileType.equalsIgnoreCase( "N3" ) ) {
					rc.add( file, baseURI, RDFFormat.N3 );
				}
				else if ( rdfFileType.equalsIgnoreCase( "NTRIPLES" ) ) {
					rc.add( file, baseURI, RDFFormat.NTRIPLES );
				}
				else if ( rdfFileType.equalsIgnoreCase( "TRIG" ) ) {
					rc.add( file, baseURI, RDFFormat.TRIG );
				}
				else if ( rdfFileType.equalsIgnoreCase( "TRIX" ) ) {
					rc.add( file, baseURI, RDFFormat.TRIX );
				}
			}
			else {
				log.warn( "no file type/file given to RDFFileSesameEngine" );
			}
			this.connected = true;
		}
		catch ( IOException | RDFParseException | RepositoryException ioe ) {
			log.error( ioe, ioe );
		}
	}
  
  	@Override
	public void commit()
	{
		try {
			rc.commit();
			//closeDB();
			//openDB(propFile);
		} catch (RepositoryException e) {
			log.error( e );
		}
	}

	@Override
	public void closeDB() {
		// ng.stopTransaction(Conclusion.SUCCESS);
		try {
			rc.close();
			connected = false;
		} catch (Exception e) {
			log.error( e );
		}
		try {
			repo.shutDown();
		} catch (Exception e) {
			log.error( e );
		}
		// ng.shutdown();
	}

	/**
	 * Runs the passed string query against the engine and returns graph query results.  The query passed must be in the structure 
	 * of a CONSTRUCT SPARQL query.  The exact format of the results will be 
	 * dependent on the type of the engine, but regardless the results are able to be graphed.
	 * @param query the string version of the query to be run against the engine
	
	 * @return the graph query results */
	@Override
	public GraphQueryResult execGraphQuery(String query) {
		 GraphQueryResult res = null;
		 
		try {
			GraphQuery sagq =
          rc.prepareGraphQuery(QueryLanguage.SPARQL, 	query);
				res = sagq.evaluate();
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			log.error( e );
		}
		return res;	
	}

	/**
	 * Runs the passed string query against the engine as a SELECT query.  The query passed must be in the structure of a SELECT 
	 * SPARQL query and the result format will depend on the engine type.
	 * @param query the string version of the SELECT query to be run against the engine
	
	 * @return triple query results that can be displayed as a grid */
	@Override
	public TupleQueryResult execSelectQuery(String query) {
		
		TupleQueryResult sparqlResults = null;
		
		try {
			TupleQuery tq = rc.prepareTupleQuery(QueryLanguage.SPARQL, query);
			log.debug("SPARQL: " + query);
			tq.setIncludeInferred(true /* includeInferred */);
			sparqlResults = tq.evaluate();
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			log.error( e );
		}
		return sparqlResults;
	}
	
	/**
	 * Gets the type of the engine.  The engine type is often used to determine what API to use while running queries against the 
	 * engine.
	
	 * @return the type of the engine */
  @Override
	public ENGINE_TYPE getEngineType()
	{
		return IEngine.ENGINE_TYPE.SESAME;
	}
	
  @Override
	public Collection<URI> getEntityOfType(String sparqlQuery) {
    OneVarListQueryAdapter<URI> uris 
          =  OneVarListQueryAdapter.getUriList( sparqlQuery, Constants.ENTITY );
    return AbstractEngine.getSelectNoEx( uris, rc, false );
	}

	/**
	 * Returns whether or not an engine is currently connected to the data store.  The connection becomes true when {@link #openDB(String)} 
	 * is called and the connection becomes false when {@link #closeDB()} is called.
	
	 * @return true if the engine is connected to its data store and false if it is not */
	@Override
	public boolean isConnected()
	{
		return connected;
	}

  @Override
	public boolean execAskQuery(String query) {
		BooleanQuery bq;
		boolean response = false;
		try {
			bq = rc.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			log.debug("SPARQL: " + query);
			response = bq.evaluate();
		} catch (MalformedQueryException | RepositoryException | QueryEvaluationException e) {
			log.error( e );
		}
		
		return response;
	}

	/**
	 * Method addStatement. Processes a given subject, predicate, object triple and adds the statement to the SailConnection.
	 * @param subject String - RDF Subject
	 * @param predicate String - RDF Predicate
	 * @param object Object - RDF Object
	 * @param concept boolean - True if the statement is a concept
	 */
	@Override
	public void addStatement(String subject, String predicate, Object object, boolean concept){

    try {
      URI newSub;
      URI newPred;
      String subString;
      String predString;
      String sub = subject.trim();
      String pred = predicate.trim();

      ValueFactory vf = rc.getValueFactory();
      subString = Utility.getUriCompatibleString( sub, false );
      newSub = vf.createURI( subString );

      predString = Utility.getUriCompatibleString( pred, false );
      newPred = vf.createURI( predString );

      if ( !concept ) {
        if ( object.getClass() == Double.class ) {
          log.debug( "Found Double " + object );
          rc.add( new StatementImpl( newSub, newPred, vf.createLiteral( ( (Double) object ) ) ) );
        }
        else if ( object.getClass() == Date.class ) {
          log.debug( "Found Date " + object );
          rc.add( new StatementImpl( newSub, newPred, vf.createLiteral( Date.class.cast( object ) ) ) );
        }
        else {
          log.debug( "Found String " + object );
          String value = object + "";
          // try to see if it already has properties then add to it
          String cleanValue = value.replaceAll( "/", "-" ).replaceAll( "\"", "'" );
          rc.add( new StatementImpl( newSub, newPred, vf.createLiteral( cleanValue ) ) );
        }
      }
      else {
        rc.add( new StatementImpl( newSub, newPred, vf.createURI( object + "" ) ) );
      }
    }
    catch ( RepositoryException e ) {
      log.error( e );
    }
	}
	
	/**
	 * Method removeStatement. Processes a given subject, predicate, object triple and adds the statement to the SailConnection.
	 * @param subject String - RDF Subject
	 * @param predicate String - RDF Predicate
	 * @param object Object - RDF Object
	 * @param concept boolean - True if the statement is a concept
	 */
	@Override
	public void removeStatement(String subject, String predicate, Object object, boolean concept)
	{
		//logger.info("Updating Triple " + subject + "<>" + predicate + "<>" + object);
		try {
			URI newSub;
			URI newPred;
			String subString;
			String predString;
			String sub = subject.trim();
			String pred = predicate.trim();

      ValueFactory vf = rc.getValueFactory();
			subString = Utility.getUriCompatibleString(sub, false);
			newSub = vf.createURI(subString);
			
			predString = Utility.getUriCompatibleString(pred, false);
			newPred = vf.createURI(predString);
			
			if(!concept)
			{
				if(object.getClass() == Double.class ) {
					log.debug("Found Double " + object);
					rc.remove(new StatementImpl( newSub, newPred, vf.createLiteral(((Double)object))) );
				}
				else if(object.getClass() == Date.class ){
					log.debug("Found Date " + object);
					rc.remove(new StatementImpl( newSub, newPred, vf.createLiteral( Date.class.cast( object ) ) ) );
				}
        else {
					log.debug("Found String " + object);
					String value = object + "";
					// try to see if it already has properties then add to it
					String cleanValue = value.replaceAll("/", "-").replaceAll("\"", "'");			
					rc.remove(new StatementImpl( newSub, newPred, vf.createLiteral(cleanValue)) );
				} 
			}
      else {
				rc.remove(new StatementImpl( newSub, newPred, vf.createURI(object+"")) );
			}
		} catch (RepositoryException e) 
		{
			log.error( e );
		}
	}
	
	/**
	 * Runs the passed string query against the engine as an INSERT query.  The query passed must be in the structure of an INSERT 
	 * SPARQL query or an INSERT DATA SPARQL query 
	 * and there are no returned results.  The query will result in the specified triples getting added to the 
	 * data store.
	 * @param query the INSERT or INSERT DATA SPARQL query to be run against the engine
   * @throws org.openrdf.sail.SailException
   * @throws org.openrdf.query.UpdateExecutionException
   * @throws org.openrdf.repository.RepositoryException
   * @throws org.openrdf.query.MalformedQueryException
	 */
	@Override
	public void execInsertQuery(String query) 
      throws SailException, UpdateExecutionException, RepositoryException, MalformedQueryException {

		Update up = rc.prepareUpdate(QueryLanguage.SPARQL, query);
		//sc.addStatement(vf.createURI("<http://semoss.org/ontologies/Concept/Service/tom2>"),vf.createURI("<http://semoss.org/ontologies/Relation/Exposes>"),vf.createURI("<http://semoss.org/ontologies/Concept/BusinessLogicUnit/tom1>"));
		log.debug("SPARQL: " + query);
		//tq.setIncludeInferred(true /* includeInferred */);
		//tq.evaluate();
		rc.begin();
		up.execute();
		//rc.commit();
		rc.commit();
	}
	
	/**
	 * Method exportDB.  Exports the repository connection to the RDF database.
   * @throws java.lang.Exception
	 */
	public void exportDB() throws Exception
	{
		log.debug("Exporting database");
		rc.export(new RDFXMLPrettyWriter(new FileWriter(fileName)));
	}
	
	/**
	 * Method getRc.  Gets the repository connection.
	
	 * @return RepositoryConnection - The repository connection. */
	public RepositoryConnection getRc() {
		return rc;
	}
  
  @Override
  public <T> T query( QueryExecutor<T> exe )
      throws RepositoryException, MalformedQueryException, QueryEvaluationException {
    if ( isConnected() ) {
      return getSelect( exe, rc, supportsSparqlBindings() );
    }

    throw new RepositoryException( "The engine is not connected" );
  }

  @Override
  public void execute( ModificationExecutor exe ) throws RepositoryException {
    try {
      exe.exec( rc );
      AbstractEngine.updateLastModifiedDate( rc, getBaseUri() );
      rc.commit();
    }
    catch ( RepositoryException e ) {
      rc.rollback();
      throw e;
    }
  }
}
