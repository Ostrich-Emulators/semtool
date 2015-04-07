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

import com.bigdata.journal.IIndexManager;
import com.bigdata.journal.ITx;
import com.bigdata.journal.Journal;
import java.util.Date;

import org.openrdf.model.URI;
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
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;

import com.bigdata.rdf.rules.InferenceEngine;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.sail.BigdataSailRepositoryConnection;
import com.bigdata.rdf.sail.CreateKBTask;
import com.bigdata.rdf.sail.webapp.NanoSparqlServer;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.task.AbstractApiTask;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.rdf.engine.api.InsightManager;
import info.aduna.iteration.Iterations;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryResult;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import static gov.va.semoss.rdf.engine.impl.AbstractEngine.searchFor;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.UriBuilder;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Big data engine serves to connect the .jnl files, which contain the RDF
 * database, to the java engine.
 */
public class BigDataEngine extends AbstractEngine implements IEngine {

	private static final Logger log = Logger.getLogger( BigDataEngine.class );

	private Journal journal = null;
	private BigdataSailRepository repo = null;
	private BigdataSailRepositoryConnection rc = null;
	private BigdataSail sail = null;
	private BigdataSailRepository insightrepo = null;
	private boolean connected = false;
	private Server server = null;
	private java.net.URI serverurl = null;
	private InsightManagerImpl insightEngine = null;

	@Override
	protected void startLoading( Properties props ) throws RepositoryException {
		Properties rws = getRWSProperties( props );

		// the journal is the file itself
		journal = new Journal( rws );

		// the main KB
		rws.setProperty( BigdataSail.Options.NAMESPACE, "kb" );
		CreateKBTask ctor = new CreateKBTask( "kb", rws );
		try {
			AbstractApiTask.submitApiTask( journal, ctor ).get();
			AbstractTripleStore triples
					= AbstractTripleStore.class.cast( journal.getResourceLocator().
							locate( "kb", ITx.UNISOLATED ) );

			sail = new BigdataSail( triples );
			repo = new BigdataSailRepository( sail );
			repo.initialize();

			// the insights KB
			rws.setProperty( BigdataSail.Options.NAMESPACE, Constants.INSIGHTKB );
			CreateKBTask ctor2 = new CreateKBTask( Constants.INSIGHTKB, rws );
			AbstractApiTask.submitApiTask( journal, ctor2 ).get();
			AbstractTripleStore insights
					= AbstractTripleStore.class.cast( journal.getResourceLocator().
							locate( Constants.INSIGHTKB, ITx.UNISOLATED ) );
			BigdataSail insightSail = new BigdataSail( insights );
			insightrepo = new BigdataSailRepository( insightSail );
			insightrepo.initialize();
		}
		catch ( InterruptedException | ExecutionException e ) {
			log.fatal( e, e );
		}

		super.startLoading( props );

		rc = repo.getConnection();

		// if the baseuri isn't already set, then query the kb for void:Dataset
		RepositoryResult<Statement> rr = rc.getStatements( null, RDF.TYPE,
				MetadataConstants.VOID_DS, false );
		List<Statement> stmts = Iterations.asList( rr );
		for ( Statement s : stmts ) {
			setDataBuilder( UriBuilder.getBuilder( s.getSubject().stringValue() ) );
		}
	}

	@Override
	protected Properties loadAllProperties( Properties props, File... searchpath )
			throws IOException {
		Properties ret = super.loadAllProperties( props, searchpath );

		String rwspropfile
				= ret.getProperty( Constants.SMSS_RWSTORE_KEY, "RWStore.properties" );
		File rwsfile = searchFor( rwspropfile, searchpath );

		Properties rws = ( null == rwsfile ? props : Utility.loadProp( rwsfile ) );

		String jnlName
				= rws.getProperty( BigdataSail.Options.FILE, getEngineName() + ".jnl" );
		// fix the path for the jnl file
		File jnl = searchFor( jnlName, searchpath );

		ret.put( BigdataSail.Options.FILE, jnl.toString() );
		return ret;
	}

	@Override
	protected void finishLoading( Properties props ) throws RepositoryException {
		connected = true;
		refreshSchemaData();

		Map<String, String> namespaces = new LinkedHashMap<>();
		namespaces.put( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		namespaces.put( OWL.PREFIX, OWL.NAMESPACE );
		namespaces.put( RDF.PREFIX, RDF.NAMESPACE );
		namespaces.put( RDFS.PREFIX, RDFS.NAMESPACE );
		namespaces.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		namespaces.put( MetadataConstants.VOID_PREFIX, MetadataConstants.VOID_NS );
		namespaces.put( DC.PREFIX, DC.NAMESPACE );

		//namespaces.put( "rel", getSchemaBuilder().getRelationUri().toString() );
		//namespaces.put( "concept", getSchemaBuilder().getConceptNamespace().toString() );
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );

		for ( Map.Entry<String, String> en : namespaces.entrySet() ) {
			rc.setNamespace( en.getKey(), en.getValue() );
		}
	}

	protected void refreshSchemaData() {
		// load everything from the SEMOSS namespace as our OWL dataset
		UriBuilder owlb = getSchemaBuilder();
		if ( null == owlb ) {
			throw new UnsupportedOperationException(
					"Cannot determine base relationships before calling setOwlStarter" );
		}

		String q = "SELECT ?uri { ?uri rdfs:subClassOf+ ?root }";
		OneVarListQueryAdapter<URI> uris
				= OneVarListQueryAdapter.getUriList( q, "uri" );
		uris.bind( "root", owlb.getConceptUri().build() );
		try {
			List<Statement> stmts = new ArrayList<>();
			for ( URI uri : query( uris ) ) {
				stmts.addAll( Iterations.asList( rc.getStatements( uri, null, null,
						false ) ) );
			}
			setOwlData( stmts );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.warn( "could not retrieve OWL data", e );
		}
	}

	@Override
	protected InsightManager createInsightManager() throws RepositoryException {
		// create an in-memory KB, but copy everything from our jnl-based
		// KB to it

		BigdataSailRepositoryConnection insightrc = insightrepo.getReadOnlyConnection();

		ForwardChainingRDFSInferencer inferer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		SailRepository sailor = new SailRepository( inferer );
		insightEngine = new InsightManagerImpl( sailor );
		RepositoryConnection src = insightEngine.getRawConnection();
		// copy statements from disk to memory
		List<Statement> stmts
				= Iterations.asList( insightrc.getStatements( null, null, null, false ) );
		log.debug( "loading on-disk insights stmts: " + stmts.size() );
		src.begin();
		src.add( stmts );
		src.commit();
		//src.close();
		insightrc.close();

//    log.debug( "insight statements: " + stmts.size() );
//    try ( Writer w = new BufferedWriter( new FileWriter( new File( "/tmp/ikb-" + getEngineName() + ".nt" ) ) ) ) {
//      src.export( new NTriplesWriter( w ) );
//    }
//    catch ( Exception e ) {
//      log.error( e, e );
//    }
		return insightEngine;
	}

	private void copyInsightsToDisk( Collection<Statement> newstmts ) throws RepositoryException {
		// this function is a bit tricky...we want to:
		// 1) commit then close this engine's write-handle on the main KB
		// 2) open it on the Insights KB
		// 3) rewrite everything to the Insights KB
		// 4) close the Insights write handle
		// 5) re-open the write handle to the main KB
		try {
			// 1
			rc.commit();
			rc.close();
		}
		catch ( Exception e ) {
			log.error( "unable to prepare repository for insights management", e );
			throw e;
		}

		try {
			// 2
			BigdataSailRepositoryConnection repoc = insightrepo.getConnection();
			// 3
			log.debug( "writing " + newstmts.size() + " statements to on-disk insight kb" );
			repoc.begin();
			repoc.clear();
			repoc.add( newstmts );
			repoc.commit();
			// 4
			repoc.close();
		}
		finally {
			try {
				// 5
				rc = BigDataEngine.this.repo.getConnection();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}
	}

	@Override
	protected void loadLegacyInsights( Properties props ) throws RepositoryException {
		// this gets called from the startup logic, so we have a RW connection
		if ( !props.isEmpty() ) {
			insightEngine.loadAllPerspectives( props );
			copyInsightsToDisk( insightEngine.getStatements() );
		}
	}

	@Override
	public WriteableInsightManager getWriteableInsightManager() {
		return new WriteableInsightManagerImpl( insightEngine ) {

			@Override
			public void commit() {
				if ( hasCommittableChanges() ) {
					try {
						Collection<Statement> stmts = getStatements();
						copyInsightsToDisk( getStatements() ); // from the WriteableInsightManager

						// refresh the insight engine's KB
						RepositoryConnection src = insightEngine.getRawConnection();
						src.begin();
						src.clear();
						src.add( stmts );
						src.commit();
					}
					catch ( RepositoryException re ) {
						log.error( re, re );
					}
				}

			}
		};
	}

	/**
	 * Closes the data base associated with the engine. This will prevent further
	 * changes from being made in the data store and safely ends the active
	 * transactions and closes the engine.
	 */
	@Override
	public void closeDB() {
		super.closeDB();

		try {
			rc.commit();
		}
		catch ( Exception e1 ) {
			log.warn( "could not commit last transaction", e1 );
		}

		try {
			rc.close();
		}
		catch ( Exception e1 ) {
			log.warn( "could not close kb connection", e1 );
		}

		try {
			insightrepo.shutDown();
		}
		catch ( Exception e1 ) {
			log.warn( "could not close insight kb repository", e1 );
		}

		try {
			repo.shutDown();
		}
		catch ( Exception e1 ) {
			log.warn( "could not shut down kb repository", e1 );
		}

		try {
			journal.close();
		}
		catch ( Exception e1 ) {
			log.warn( "could not close journal file", e1 );
		}

		connected = false;
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
			GraphQuery sagq = rc.prepareGraphQuery( QueryLanguage.SPARQL, query );
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
	 *
	 * @throws org.openrdf.sail.SailException
	 * @throws org.openrdf.query.UpdateExecutionException
	 * @throws org.openrdf.repository.RepositoryException
	 * @throws org.openrdf.query.MalformedQueryException
	 */
	@Override
	public void execInsertQuery( String query )
			throws SailException, UpdateExecutionException, RepositoryException, MalformedQueryException {

		Update up = rc.prepareUpdate( QueryLanguage.SPARQL, query );
		//sc.addStatement(vf.createURI("<http://semoss.org/ontologies/Concept/Service/tom2>"),vf.createURI("<http://semoss.org/ontologies/Relation/Exposes>"),vf.createURI("<http://semoss.org/ontologies/Concept/BusinessLogicUnit/tom1>"));
		log.debug( "SPARQL: " + query );
		//tq.setIncludeInferred(true /* includeInferred */);
		//tq.evaluate();
		rc.begin();
		up.execute();
		//rc.commit();
		InferenceEngine ie = sail.getInferenceEngine();
		ie.computeClosure( null );
		AbstractEngine.updateLastModifiedDate( rc, getBaseUri() );
		rc.commit();
	}

	@Override
	public boolean execAskQuery( String query ) {
		boolean response = false;
		try {
			BooleanQuery bq = rc.prepareBooleanQuery( QueryLanguage.SPARQL, query );
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

	/**
	 * Processes a SELECT query just like {@link #execSelectQuery(String)} but
	 * then parses the results to get only their instance names. These instance
	 * names are then returned as the Vector of Strings.
	 *
	 * @param sparqlQuery the SELECT SPARQL query to be run against the engine
	 *
	 * @return the Vector of Strings representing the instance names of all of the
	 * query results
	 */
	@Override
	public Collection<URI> getEntityOfType( String sparqlQuery ) {
		try {
			if ( sparqlQuery != null ) {
				OneVarListQueryAdapter<URI> qea
						= OneVarListQueryAdapter.getUriList( sparqlQuery, Constants.ENTITY );
				qea.useInferred( true );
				return getSelect( qea, rc, supportsSparqlBindings() );
			}
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e );
		}
		return new ArrayList<>();
	}

	/**
	 * Returns whether or not an engine is currently connected to the data store.
	 * The connection becomes true between calls to {@link #openDB(java.util.Properties)
	 * }
	 * and {@link #closeDB()}.
	 *
	 * @return true if the engine is connected to its data store and false if it
	 * is not
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Method addStatement. Processes a given subject, predicate, object triple
	 * and adds the statement to the SailConnection.
	 *
	 * @param subject String - RDF Subject for the triple
	 * @param predicate String - RDF Predicate for the triple
	 * @param object Object - RDF Object for the triple
	 * @param concept boolean - True if the statement is a concept
	 */
	@Override
	public void addStatement( String subject, String predicate, Object object,
			boolean concept ) {
		//logger.debug("Updating Triple " + subject + "<>" + predicate + "<>" + object);
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
					rc.add(
							new StatementImpl( newSub, newPred, vf.createLiteral(
											( (Double) object ) ) ) );
				}

				else if ( object.getClass() == Date.class ) {
					log.debug( "Found Date " + object );
					rc.add(
							new StatementImpl( newSub, newPred, vf.createLiteral( Date.class
											.cast( object ) ) ) );
				}
				else {
					log.debug( "Found String " + object );
					String value = object + "";
					// try to see if it already has properties then add to it
					String cleanValue
							= value.replaceAll( "/", "-" ).replaceAll( "\"", "'" );
					rc.add( new StatementImpl( newSub, newPred, vf.createLiteral(
							cleanValue ) ) );
				}
			}
			else {
				rc.add(
						new StatementImpl( newSub, newPred, vf.createURI( object + "" ) ) );
			}

		}
		catch ( RepositoryException e ) {
			log.error( e );
		}
	}

	@Override
	public void calculateInferences() throws RepositoryException {
		try {
			log.debug( "start calculating inferences" );
			InferenceEngine ie = sail.getInferenceEngine();
			ie.computeClosure( null );
			AbstractEngine.updateLastModifiedDate( rc, getBaseUri() );
			rc.commit();
			log.debug( "done calculating inferences" );
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
		}
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

	public static Properties generateProperties( File jnl ) {
		Properties props = new Properties();
		props.setProperty( BigdataSail.Options.FILE, jnl.toString() );
		props.setProperty( Constants.ENGINE_IMPL,
				BigDataEngine.class.getCanonicalName() );
		props.setProperty( Constants.SMSS_VERSION_KEY, "1.0" );

		return props;
	}

	@Override
	public void commit() {
		try {
			rc.commit();
		}
		catch ( RepositoryException e ) {
			log.error( e );
		}
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
	public Model construct( String q )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		return getConstruct( q, rc );
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

	/**
	 * Gets the bigdata-specific properties
	 *
	 * @param prop all the properties to look through
	 *
	 * @return bigdata-specific properties
	 */
	private Properties getRWSProperties( Properties prop ) {
		Properties rws = new Properties();
		for ( String key : prop.stringPropertyNames() ) {
			if ( key.startsWith( "com.bigdata" ) ) {
				String val = prop.getProperty( key );
				rws.setProperty( key, val );
			}
		}

		return rws;
	}

	@Override
	public boolean serverIsRunning() {
		return ( null != server );
	}

	@Override
	public java.net.URI getServerUri() {
		return serverurl;
	}

	@Override
	public boolean isServerSupported() {
		return true;
	}

	@Override
	public void stopServer() {
		serverurl = null;
		if ( null == server ) {
			return;
		}
		try {
			server.stop();
		}
		catch ( Exception e ) {
			log.warn( "could not stop server", e );
		}
	}

	@Override
	public void startServer( int port ) {
		try {
			IIndexManager indexmgr = sail.getDatabase().getIndexManager();

			Properties rws = getRWSProperties( prop );
			Map<String, String> opts = new HashMap<>();
			for ( String key : rws.stringPropertyNames() ) {
				opts.put( key, rws.getProperty( key ) );
			}
			opts.put( BigdataSail.Options.READ_ONLY, Boolean.toString( true ) );

			EmbeddedServerRunnable run
					= new EmbeddedServerRunnable( port, indexmgr, opts );
			serverurl = new java.net.URI( "http://127.0.0.1:" + port + "/bigdata" );
			new Thread( run ).start();
		}
		catch ( Exception ioe ) {
			log.error( ioe );
		}
	}

	@Override
	public boolean supportsSparqlBindings() {
		// if we ever get remote BD working, we need to return false 
		return true;
	}

	/**
	 * A server thread. Taken almost exclusively from
	 * http://sourceforge.net/p/bigdata/code/HEAD/tree/branches/BIGDATA_RELEASE_1_3_0/bigdata-sails/src/samples/com/bigdata/samples/NSSEmbeddedExample.java?view=markup#l31
	 */
	private class EmbeddedServerRunnable implements Runnable {

		private final int port;
		private final IIndexManager mgr;
		private final Map<String, String> opts;

		public EmbeddedServerRunnable( int port, IIndexManager mgr,
				Map<String, String> opts ) {
			this.port = port;
			this.mgr = mgr;
			this.opts = opts;
		}

		@Override
		public void run() {
			try {
				log.debug( "starting jetty server on port " + port + "..." );
				BigDataEngine.this.server = NanoSparqlServer.newInstance( port,
						mgr, opts );
				server.setStopAtShutdown( true );

				NanoSparqlServer.awaitServerStart( BigDataEngine.this.server );
				// Block and wait. The NSS is running.
				log.debug( "jetty server started" );
				BigDataEngine.this.server.join();
			}
			catch ( Throwable t ) {
				log.error( t );
			}
			finally {
				if ( BigDataEngine.this.server != null ) {
					try {
						BigDataEngine.this.server.stop();
					}
					catch ( Exception e ) {
						log.error( e, e );
					}
					server = null;
					System.gc();
					log.debug( "jetty stopped" );
				}
			}
		}
	}
}
