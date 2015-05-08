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

import gov.va.semoss.model.vocabulary.VAS;
import info.aduna.iteration.Iterations;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.ui.main.SemossPreferences;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.sail.SailException;

/**
 * An Abstract Engine that sets up the base constructs needed to create an
 * engine.
 */
public abstract class AbstractSesameEngine extends AbstractEngine {

	private static final Logger log = Logger.getLogger( AbstractSesameEngine.class );
	private RepositoryConnection owlRc;

	/**
	 * Loads the metadata information from the given file.
	 *
	 * @param ontoloc the location of the owl file. It is guaranteed to exist
	 */
	@Override
	protected void loadLegacyOwl( String ontoloc ) {
		try {
			owlRc.begin();
			owlRc.add( new File( ontoloc ), ontoloc, RDFFormat.RDFXML );
			owlRc.commit();
		}
		catch ( IOException | RDFParseException | RepositoryException e ) {
			log.error( e, e );
		}
	}

	protected RepositoryConnection createOwlRc() throws RepositoryException {
		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		SailRepository owlRepo = new SailRepository( inferencer );
		owlRepo.initialize();
		return owlRepo.getConnection();
	}

	/**
	 * Initiates the loading process with the given properties. Subclasses will
	 * usually use this function to open their repositories before the rest of the
	 * loading process occurs. If overridden, subclasses should be sure to call
	 * their superclass's version of this function in addition to whatever other
	 * processing they do.
	 *
	 * @param props
	 * @throws RepositoryException
	 */
	@Override
	protected void startLoading( Properties props ) throws RepositoryException {
		super.startLoading( props );
		owlRc = createOwlRc();
	}

	@Override
	protected URI setUris( String data, String schema ) {
		URI baseuri = null;
		if ( data.isEmpty() ) {
			try {
				// if the baseuri isn't already set, then query the kb for void:Dataset
				RepositoryResult<Statement> rr
						= getRawConnection().getStatements( null, RDF.TYPE, VAS.Database, false );
				List<Statement> stmts = Iterations.asList( rr );
				for ( Statement s : stmts ) {
					baseuri = URI.class.cast( s.getSubject() );
					break;
				}

				if ( null == baseuri ) {
					// not set yet, so make one (this is a silent upgrade)
					RepositoryConnection rc = getRawConnection();
					rc.begin();
					try {
						baseuri = silentlyUpgrade( rc );
						rc.commit();
					}
					catch ( RepositoryException e ) {
						log.error( e, e );
						rc.rollback();
					}
				}
			}
			catch ( RepositoryException e ) {
				log.error( e, e );
			}
		}
		else {
			baseuri = new URIImpl( data );
		}

		if ( null == baseuri ) {
			log.fatal( "no base uri set" );
		}

		setSchemaBuilder( UriBuilder.getBuilder( schema ) );
		setDataBuilder( UriBuilder.getBuilder( baseuri ) );
		return baseuri;
	}

	protected URI silentlyUpgrade( RepositoryConnection rc ) throws RepositoryException {
		URI baseuri = getNewBaseUri();
		rc.add( baseuri, RDF.TYPE, VAS.Database );

		// see if we have some old metadata we can move over, too
		VoidQueryAdapter q = new VoidQueryAdapter( "SELECT ?pred ?val { ?uri a ?voidds . ?uri ?pred ?val}" ) {

			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				URI pred = URI.class.cast( set.getValue( "pred" ) );
				if ( !( MetadataConstants.OWLIRI.equals( pred ) || RDF.TYPE.equals( pred )
						|| OWL.VERSIONINFO.equals( pred ) ) ) {

					try {
						rc.add( baseuri, pred, set.getValue( "val" ) );
					}
					catch ( RepositoryException re ) {
						log.warn( "Could not move metadata to new URI", re );
					}
				}
			}
		};
		q.bind( "voidds", MetadataConstants.VOID_DS );
		try {
			query( q );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
		return baseuri;

	}

	@Override
	protected void finishLoading( Properties props ) throws RepositoryException {
		refreshSchemaData();

		setEngineName( MetadataQuery.getEngineLabel( this ) );

		RepositoryConnection rc = getRawConnection();
		rc.begin();
		for ( Map.Entry<String, String> en : Utility.DEFAULTNAMESPACES.entrySet() ) {
			rc.setNamespace( en.getKey(), en.getValue() );
		}
		rc.commit();
	}

	protected void refreshSchemaData() {
		// load everything from the SEMOSS namespace as our OWL dataset
		UriBuilder owlb = getSchemaBuilder();
		if ( null == owlb ) {
			throw new UnsupportedOperationException(
					"Cannot determine base relationships before setting the schema URI" );
		}

		String q = "SELECT ?uri { ?uri rdfs:subClassOf+ ?root }";
		OneVarListQueryAdapter<URI> uris
				= OneVarListQueryAdapter.getUriList( q, "uri" );
		uris.bind( "root", owlb.getConceptUri().build() );
		try {
			RepositoryConnection rc = getRawConnection();

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
		log.debug( "creating default (in-memory) insight repository" );
		ForwardChainingRDFSInferencer inferer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		InsightManagerImpl imi
				= new InsightManagerImpl( new SailRepository( inferer ) );
		return imi;
	}

	@Override
	public void closeDB() {
		log.debug( "closing db: " + getEngineName() );
		if ( null != owlRc ) {
			try {
				owlRc.close();
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}

			try {
				owlRc.getRepository().shutDown();
			}
			catch ( Exception e ) {
				log.warn( e, e );
			}
		}

		super.closeDB();
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
		try {
			return getRawConnection().isOpen();
		}
		catch ( RepositoryException e ) {
			return false;
		}
	}

	/**
	 * Writes the database back with updated properties if necessary
	 */
	@Override
	public void saveConfiguration() {
		String propFile = prop.getProperty( Constants.SMSS_LOCATION );
		try ( FileWriter fw = new FileWriter( propFile ) ) {
			log.debug( "Writing to file " + propFile );
			prop.store( fw, null );
		}
		catch ( IOException e ) {
			log.debug( e );
		}
	}

	// gets the from neighborhood for a given node
	public Map<String, List<String>> getFromNeighborsWithVerbs( String nodeType,
			int neighborHood ) {
		// this is where this node is the from node
		final Map<String, List<String>> ret = new HashMap<>();
		QueryExecutor<Void> vqa = new QueryExecutorAdapter<Void>(
				fromSparqlWithVerbs ) {
					@Override
					public void handleTuple( BindingSet set, ValueFactory fac ) {
						String verb = set.getValue( "ret" ).stringValue();
						String node = set.getValue( "entity" ).stringValue();
						if ( !ret.containsKey( verb ) ) {
							ret.put( verb, new ArrayList<>() );
						}
						ret.get( verb ).add( node );
					}
				};
		vqa.bindURI( "noteType", nodeType );
		getSelectNoEx( vqa, owlRc, true );
		return ret;
	}

	// gets the to nodes
	public Map<String, List<String>> getToNeighborsWithVerbs( String nodeType,
			int neighborHood ) {
		// this is where this node is the to node
		final Map<String, List<String>> ret = new HashMap<>();
		QueryExecutor<Void> vqa = new QueryExecutorAdapter<Void>( toSparqlWithVerbs ) {
			@Override
			public void handleTuple( BindingSet set, ValueFactory fac ) {
				String verb = set.getValue( "ret" ).stringValue();
				String node = set.getValue( "entity" ).stringValue();
				if ( !ret.containsKey( verb ) ) {
					ret.put( verb, new ArrayList<>() );
				}
				ret.get( verb ).add( node );
			}
		};
		vqa.bindURI( "noteType", nodeType );
		getSelectNoEx( vqa, owlRc, true );
		return ret;
	}

	private static String processNamespaces( String rawsparql ) {
		Map<String, String> namespaces = SemossPreferences.getInstance().getNamespaces();
		namespaces.putAll( Utility.DEFAULTNAMESPACES );

		Set<String> existingNamespaces = new HashSet<>();
		if ( rawsparql.toUpperCase().contains( "PREFIX" ) ) {
			Pattern pat = Pattern.compile( "prefix[\\s]+([A-Za-z0-9_-]+)[\\s]*:",
					Pattern.CASE_INSENSITIVE );
			Matcher m = pat.matcher( rawsparql );
			while ( m.find() ) {
				existingNamespaces.add( m.group( 1 ) );
			}
		}

		StringBuilder sparql = new StringBuilder();
		for ( Map.Entry<String, String> en : namespaces.entrySet() ) {
			if ( !existingNamespaces.contains( en.getKey() ) ) {
				sparql.append( "PREFIX " ).append( en.getKey() );
				sparql.append( ": <" ).append( en.getValue() ).append( "> " );
			}
		}

		sparql.append( rawsparql );
		return sparql.toString();
	}

	public static final <T> T getSelect( QueryExecutor<T> query,
			RepositoryConnection rc, boolean dobindings ) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {

		String sparql = processNamespaces( dobindings ? query.getSparql()
				: query.bindAndGetSparql() );

		ValueFactory vfac = new ValueFactoryImpl();
		TupleQuery tq = rc.prepareTupleQuery( QueryLanguage.SPARQL, sparql );
		if ( dobindings ) {
			tq.setIncludeInferred( query.usesInferred() );
			query.setBindings( tq, vfac );
		}

		TupleQueryResult rslt = tq.evaluate();
		query.start( rslt.getBindingNames() );
		while ( rslt.hasNext() ) {
			query.handleTuple( rslt.next(), vfac );
		}
		query.done();
		rslt.close();
		return query.getResults();
	}

	protected abstract RepositoryConnection getRawConnection();

	public static final <T> T getSelectNoEx( QueryExecutor<T> query,
			RepositoryConnection rc,
			boolean dobindings ) {
		try {
			return getSelect( query, rc, dobindings );
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( "could not execute select: " + query.getSparql(), e );
			return null;
		}
	}

	public static Model getConstruct( String sparqlstr, RepositoryConnection rc )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		String sparql = processNamespaces( sparqlstr );
		GraphQuery tq = rc.prepareGraphQuery( QueryLanguage.SPARQL, sparql );
		Model model = new LinkedHashModel();
		GraphQueryResult gqr = tq.evaluate();
		while ( gqr.hasNext() ) {
			model.add( gqr.next() );
		}
		gqr.close();
		return model;
	}

	@Override
	public <T> T query( QueryExecutor<T> exe )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		if ( isConnected() ) {
			RepositoryConnection rc = getRawConnection();
			return getSelect( exe, rc, supportsSparqlBindings() );
		}

		throw new RepositoryException( "The engine is not connected" );
	}

	@Override
	public Model construct( String q )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		RepositoryConnection rc = getRawConnection();
		return getConstruct( q, rc );
	}

	@Override
	public void execute( ModificationExecutor exe ) throws RepositoryException {
		RepositoryConnection rc = getRawConnection();

		try {
			if ( exe.execInTransaction() ) {
				rc.begin();
			}

			exe.exec( rc );

			if ( exe.execInTransaction() ) {
				rc.commit();
			}
		}
		catch ( RepositoryException e ) {
			if ( exe.execInTransaction() ) {
				rc.rollback();
			}

			throw e;
		}
	}

	/**
	 * Retrieves the "by convention" name for the given file type
	 *
	 * @param filetype one of:
	 * {@link Constants#ONTOLOGY}, {@link Constants#DREAMER}, or
	 * {@link Constants#OWLFILE}
	 * @param engineName the name of the engine
	 *
	 * @return the default name for the given file
	 *
	 * @throws IllegalArgumentException if an unknown file type arg is given
	 */
	public static String getDefaultName( String filetype, String engineName ) {
		switch ( filetype ) {
			case Constants.DREAMER:
				return engineName + "_Questions.properties";
			case Constants.ONTOLOGY:
				return engineName + "_Custom_Map.prop";
			case Constants.OWLFILE:
				return engineName + "_OWL.OWL";
			default:
				throw new IllegalArgumentException( "unhandled file type: " + filetype );
		}
	}

	@Override
	public boolean serverIsRunning() {
		return false;
	}

	@Override
	public boolean isServerSupported() {
		return false;
	}

	@Override
	public void startServer( int port ) {
		log.error(
				"Server mode is not supported. Please check isServerSupported() before calling startServer(int)" );
	}

	@Override
	public void stopServer() {
	}

	@Override
	public java.net.URI getServerUri() {
		return null;
	}

	/**
	 * Does this engine support binding variables within the Sparql execution?
	 *
	 * @return true, if the engine supports sparql variable binding
	 */
	@Override
	public boolean supportsSparqlBindings() {
		return true;
	}

	public static void updateLastModifiedDate( RepositoryConnection rc,
			Resource baseuri ) {
		// updates the base uri's last modified key
		// 1) if we don't know it already, figure out what our base uri is
		// 2) remove any last modified value
		// 3) add the new last modified value

		ValueFactory vf = rc.getValueFactory();
		try {
			if ( null == baseuri ) {
				RepositoryResult<Statement> rr = rc.getStatements( null, RDF.TYPE,
						VAS.Database, false );
				List<Statement> stmts = Iterations.asList( rr );
				for ( Statement s : stmts ) {
					baseuri = s.getSubject();
				}
			}

			if ( null == baseuri ) {
				log.warn( "cannot update last modified date when no base uri is set" );
			}
			else {
				rc.remove( baseuri, MetadataConstants.DCT_MODIFIED, null );

				rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_MODIFIED,
						vf.createLiteral( QueryExecutorAdapter.getCal( new Date() ) ) ) );
			}
		}
		catch ( RepositoryException e ) {
			log.warn( "could not update last modified date", e );
		}
	}

	@Override
	public void calculateInferences() throws RepositoryException {
		// nothing to do
	}

	@Override
	public Collection<Statement> getOwlData() {
		final List<Statement> stmts = new ArrayList<>();

		try {
			for ( Statement st : Iterations.asList( owlRc.getStatements( null, null,
					null, false ) ) ) {
				// re-box, because BigData doesn't play nicely
				Resource s = new URIImpl( st.getSubject().stringValue() );
				URI p = new URIImpl( st.getPredicate().stringValue() );
				Value o = st.getObject();
				stmts.add( new StatementImpl( s, p, o ) );
			}
		}
		catch ( RepositoryException re ) {
			log.warn( re, re );
		}

		return stmts;
	}

	@Override
	public void commit() {
		try {
			RepositoryConnection rc = getRawConnection();
			rc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public void setOwlData( Collection<Statement> stmts ) {
		try {
			owlRc.clear();
			owlRc.commit();
			addOwlData( stmts );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public void addOwlData( Collection<Statement> stmts ) {
		try {
			owlRc.add( stmts );
			owlRc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public void addOwlData( Statement stmt ) {
		try {
			owlRc.add( stmt );
			owlRc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public void removeOwlData( Statement stmt ) {
		try {
			owlRc.remove( stmt );
			owlRc.commit();
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Override
	public Map<String, String> getNamespaces() {
		Map<String, String> ret = new HashMap<>();
		try {
			RepositoryConnection rc = getRawConnection();

			for ( Namespace ns : Iterations.asList( rc.getNamespaces() ) ) {
				ret.put( ns.getPrefix(), ns.getName() );
			}
		}
		catch ( RepositoryException re ) {
			log.warn( "could not retrieve namespaces", re );
		}
		return ret;
	}

	@Override
	protected void updateLastModifiedDate() {
		RepositoryConnection rc = getRawConnection();
		updateLastModifiedDate( rc, getBaseUri() );
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
		RepositoryConnection rc = getRawConnection();
		Update up = rc.prepareUpdate( QueryLanguage.SPARQL, query );
		log.debug( "SPARQL: " + query );
		rc.begin();
		up.execute();
		rc.commit();
		calculateInferences();
	}

	@Override
	public boolean execAskQuery( String query ) {
		boolean response = false;
		try {
			RepositoryConnection rc = getRawConnection();
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
				RepositoryConnection rc = getRawConnection();

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
			RepositoryConnection rc = getRawConnection();

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
			RepositoryConnection rc = getRawConnection();
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
			RepositoryConnection rc = getRawConnection();
			TupleQuery tq = rc.prepareTupleQuery( QueryLanguage.SPARQL, query );
			log.debug( "SPARQL: " + query );
			tq.setIncludeInferred( true );
			sparqlResults = tq.evaluate();
		}
		catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
			log.error( e );
		}
		return sparqlResults;
	}

	@Override
	public ENGINE_TYPE getEngineType() {
		return ENGINE_TYPE.SESAME;
	}

	@Override
	public void removeStatement( String subject, String predicate, Object object, boolean concept ) {
		throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
	}
}
