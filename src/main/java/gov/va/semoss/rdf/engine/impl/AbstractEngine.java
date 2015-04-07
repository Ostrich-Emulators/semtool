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
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import gov.va.semoss.util.Utility;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.UriBuilder;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;

/**
 * An Abstract Engine that sets up the base constructs needed to create an
 * engine.
 */
public abstract class AbstractEngine implements IEngine {

	private static final Logger log = Logger.getLogger( AbstractEngine.class );

	private String engineName = null;
	protected Properties prop = new Properties();
	private RepositoryConnection owlRc;
	private InsightManager insightEngine;
	protected String map = null;
	private UriBuilder schemabuilder;
	private UriBuilder databuilder;

	public static final String fromSparql = "SELECT DISTINCT ?entity WHERE { "
			+ "{?rel <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://semoss.org/ontologies/Relation>} "
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://semoss.org/ontologies/Concept>} "
			+ "{?x ?rel  ?y} "
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?x}"
			+ "{?nodeType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?y}"
			+ "}";

	public static final String toSparql = "SELECT DISTINCT ?entity WHERE { "
			+ "{?rel <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://semoss.org/ontologies/Relation>} "
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://semoss.org/ontologies/Concept>} "
			+ "{?x ?rel ?y} "
			+ "{?nodeType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?x}"
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?y}"
			+ "}";

	public static final String fromSparqlWithVerbs = "SELECT DISTINCT ?rel ?entity WHERE { "
			+ "{?rel <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://semoss.org/ontologies/Relation>} "
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://semoss.org/ontologies/Concept>} "
			+ "{?x ?rel  ?y} "
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?x}"
			+ "{?nodeType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?y}"
			+ "}";

	public static final String toSparqlWithVerbs = "SELECT DISTINCT ?rel ?entity WHERE { "
			+ "{?rel <http://www.w3.org/2000/01/rdf-schema#subPropertyOf> <http://semoss.org/ontologies/Relation>} "
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://semoss.org/ontologies/Concept>} "
			+ "{?x ?rel ?y} "
			+ "{?nodeType <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?x}"
			+ "{?entity <http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?y}"
			+ "}";

	@Override
	public void openDB( Properties initprops ) {
		try {
			File[] searchpath = makeSearchPath( initprops );

			prop = loadAllProperties( initprops, searchpath );
			startLoading( prop );

			String baseuristr = prop.getProperty( Constants.BASEURI_KEY, "" );
			if ( !baseuristr.isEmpty() ) {
				try {
					setDataBuilder( UriBuilder.getBuilder( baseuristr ) );
				}
				catch ( Exception e ) {
					log.warn( "no base uri set: " + baseuristr, e );
				}
			}

			String owlstarter = prop.getProperty( Constants.SEMOSS_URI,
			DIHelper.getInstance().getProperty( Constants.SEMOSS_URI ) );
			setSchemaBuilder( UriBuilder.getBuilder( owlstarter ) );

			String dreamerfileloc = prop.getProperty( Constants.DREAMER );
			if ( null != dreamerfileloc ) {
				Properties legacyquestions = Utility.loadProp( new File( dreamerfileloc ) );
				loadLegacyInsights( legacyquestions );
			}

			String ontofileloc = prop.getProperty( Constants.OWLFILE );
			if ( null != ontofileloc ) {
				loadLegacyOwl( ontofileloc );
			}

			finishLoading( prop );
		}
		catch ( IOException | RepositoryException e ) {
			log.error( e );
		}
	}

	/**
	 * Loads the insights (questions and perspectives) from the given properties.
	 * The default behavior is to do nothing
	 *
	 * @param props the insights
	 * @throws org.openrdf.repository.RepositoryException
	 */
	protected void loadLegacyInsights( Properties props ) throws RepositoryException {
		log.warn( "" );
	}

	/**
	 * Loads the metadata information from the given file.
	 *
	 * @param ontoloc the location of the owl file. It is guaranteed to exist
	 */
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

	protected Properties loadAllProperties( Properties props,
			File... searchpath ) throws IOException {

		Properties newprops = Utility.copyProperties( props );
		Properties ontoProp = new Properties( newprops );
		Properties dreamerProp = new Properties( ontoProp );

		String questionPropFile = props.getProperty( Constants.DREAMER,
				getDefaultName( Constants.DREAMER, engineName ) );
		File dreamer = searchFor( questionPropFile, searchpath );
		if ( null != dreamer ) {
			try {
				Utility.loadProp( dreamer, dreamerProp );
				newprops.setProperty( Constants.DREAMER, dreamer.getCanonicalPath() );
			}
			catch ( IOException ioe ) {
				log.warn( ioe, ioe );
			}
		}

		File owlf = searchFor( props.getProperty( Constants.OWLFILE,
				getDefaultName( Constants.OWLFILE, engineName ) ), searchpath );
		if ( owlf != null ) {
			newprops.setProperty( Constants.OWLFILE, owlf.getCanonicalPath() );
		}

		File ontof = searchFor( props.getProperty( Constants.ONTOLOGY,
				getDefaultName( Constants.ONTOLOGY, engineName ) ), searchpath );
		if ( ontof != null ) {
			newprops.setProperty( Constants.ONTOLOGY, ontof.getCanonicalPath() );
			Utility.loadProp( ontof, ontoProp );
		}

		return newprops;
	}

	/**
	 * Makes an array of directories to search to resolve file locations in the
	 * properties
	 *
	 * @param props the properties (should contain {@link Constants#SMSS_LOCATION}
	 * @return an array of directories to search for files
	 */
	private File[] makeSearchPath( Properties props ) {
		Set<File> searchpath = new HashSet<>();

		File propfile = new File( props.getProperty( Constants.SMSS_LOCATION, "." ) );
		File propdir = propfile.getParentFile();
		String verprop = props.getProperty( Constants.SMSS_VERSION_KEY, "0.0" );

		// support the old path resolution, but we really check all over
		// because the paths inside the properties files might also be wrong
		if ( Double.parseDouble( verprop ) < 1.0 ) {
			// these are legacy locations
			File dbdir
					= ( null == engineName ? propdir
							: new File( propdir, this.engineName ) );

			String basefolder
					= DIHelper.getInstance().getProperty( Constants.BASE_FOLDER );
			final File BASEDIR = new File( basefolder );
			searchpath.add( dbdir );
			searchpath.add( BASEDIR );
		}

		searchpath.add( propdir );

		String searchpathprop = props.getProperty( Constants.SMSS_SEARCHPATH, "" );
		if ( !searchpathprop.isEmpty() ) {
			for ( String p : searchpathprop.split( ";" ) ) {
				File f = new File( p );
				searchpath.add( f );
			}
		}

		return searchpath.toArray( new File[0] );
	}

	/**
	 * Initiates the loading process with the given properties. If overridden,
	 * subclasses should be sure to call their superclass's version of this
	 * function in addition to whatever other processing they do.
	 *
	 * @param props
	 * @throws RepositoryException
	 */
	protected void startLoading( Properties props ) throws RepositoryException {
		owlRc = createOwlRc();
		insightEngine = createInsightManager();
	}

	protected RepositoryConnection createOwlRc() throws RepositoryException {
		ForwardChainingRDFSInferencer inferencer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		SailRepository owlRepo = new SailRepository( inferencer );
		owlRepo.initialize();
		return owlRepo.getConnection();
	}

	protected InsightManager createInsightManager() throws RepositoryException {
		log.debug( "creating default (in-memory) insight repository" );
		ForwardChainingRDFSInferencer inferer
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		InsightManagerImpl imi
				= new InsightManagerImpl( new SailRepository( inferer ) );
		return imi;
	}

	protected void finishLoading( Properties props ) throws RepositoryException {

	}

	@Override
	public String getProperty( String key ) {
		return prop.getProperty( key );
	}

	@Override
	public final void setSchemaBuilder( UriBuilder ns ) {
		schemabuilder = ns;
	}

	@Override
	public UriBuilder getSchemaBuilder() {
		return schemabuilder;
	}

	@Override
	public UriBuilder getDataBuilder() {
		return databuilder;
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
		if ( null != insightEngine ) {
			insightEngine.release();
		}
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
	public void execInsertQuery( String query ) throws SailException,
			UpdateExecutionException, RepositoryException,
			MalformedQueryException {
		log.error( "execInsertQuery is not implemented" );
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
		return false;
	}

	/**
	 * Sets the name of the engine. This may be a lot of times the same as the
	 * Repository Name
	 *
	 * @param engineName - Name of the engine that this is being set to
	 */
	@Override
	public void setEngineName( String engineName ) {
		this.engineName = engineName;
	}

	/**
	 * Gets the engine name for this engine
	 *
	 * @return Name of the engine it is being set to
	 */
	@Override
	public String getEngineName() {
		return engineName;
	}

	@Override
	public void addStatement( String subject, String predicate, Object object,
			boolean concept ) {
		log.error( "addStatement not implemented" );
	}

	/**
	 * Method removeStatement. Processes a given subject, predicate, object triple
	 * and removes the statement to the SailConnection.
	 *
	 * @param subject	String - RDF Subject for the triple
	 * @param predicate	String - RDF Predicate for the triple
	 * @param object	Object - RDF Object for the triple
	 * @param concept	boolean - True if the statement is a concept
	 */
	@Override
	public void removeStatement( String subject, String predicate, Object object,
			boolean concept ) {
		log.error( "removeStatement not implemented" );
	}

	/**
	 * Commits the database.
	 */
	@Override
	public void commit() {

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

	/**
	 * Adds a new property to the properties list.
	 *
	 * @param name String - The name of the property.
	 * @param value String - The value of the property.
	 */
	@Override
	public void addConfiguration( String name, String value ) {
		prop.put( name, value );
	}

	// gets the from neighborhood for a given node
	@Override
	public Collection<String> getFromNeighbors( String nodeType, int neighborHood ) {
		// this is where this node is the from node
		OneVarListQueryAdapter<String> qea
				= OneVarListQueryAdapter.getStringList( fromSparql, "entity" );
		qea.bindURI( "nodeType", nodeType );
		return getSelectNoEx( qea, owlRc, true );
	}

	// gets the to nodes
	@Override
	public Collection<String> getToNeighbors( String nodeType, int neighborHood ) {
		// this is where this node is the to node
		OneVarListQueryAdapter<String> qea
				= OneVarListQueryAdapter.getStringList( toSparql, "entity" );
		qea.bindURI( "nodeType", nodeType );
		return getSelectNoEx( qea, owlRc, true );
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
							ret.put( verb, new ArrayList<String>() );
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

	// gets the from and to nodes
	@Override
	public Collection<String> getNeighbors( String nodeType, int neighborHood ) {
		Collection<String> from = getFromNeighbors( nodeType, 0 );
		Collection<String> to = getToNeighbors( nodeType, 0 );
		from.addAll( to );
		return from;
	}

	public static final <T> T getSelect( QueryExecutor<T> query,
			RepositoryConnection rc, boolean dobindings ) throws RepositoryException,
			MalformedQueryException, QueryEvaluationException {

		String sparql = ( dobindings ? query.getSparql() : query.bindAndGetSparql() );

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

	public static Model getConstruct( String sparql, RepositoryConnection rc )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
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
	public Collection<String> getParamValues( String name, String type,
			String insightId ) {
		String query = DIHelper.getInstance().getProperty(
				"TYPE" + "_" + Constants.QUERY );
		return getParamValues( name, type, insightId, query );
	}

	@Override
	public Collection<String> getParamValues( String name, String type,
			String insightId,
			String query ) {
		// TODO
		// try to see if this type is available with direct values
		List<String> uris = new ArrayList<>();
		String TYPEOPTION = type + "_" + Constants.OPTION;
		String options = getProperty( TYPEOPTION );
		String customQuery = query;
		if ( options != null ) {
			uris.addAll( Arrays.asList( options.split( "; " ) ) );
		}
		else {
			// this needs to be retrieved through SPARQL
			// need to use custom query if it has been specified on the dreamer
			// otherwise use generic fill query
			String sparqlQuery;
			if ( customQuery != null ) {
				sparqlQuery = customQuery;
			}
			else {
				sparqlQuery
						= DIHelper.getInstance().
						getProperty( "TYPE" + "_" + Constants.QUERY );
			}

			Map<String, String> paramTable = new HashMap<>();
			paramTable.put( Constants.ENTITY, type );
			sparqlQuery = Utility.fillParam( sparqlQuery, paramTable );

			for ( URI entity : getEntityOfType( sparqlQuery ) ) {
				uris.add( entity.stringValue() );
			}
		}
		return uris;
	}

	@Override
	public InsightManager getInsightManager() {
		return insightEngine;
	}

	@Override
	public WriteableInsightManager getWriteableInsightManager() {
		log.warn( "getting a non-committing WriteableInsightManager (this isn't what you want)" );
		return new WriteableInsightManagerImpl( insightEngine ) {

			@Override
			public void commit() {
				log.warn( "this WriteableInsightManager doesn't write" );
			}
		};
	}

	@Override
	public void setInsightManager( InsightManager ie ) {
		this.insightEngine = ie;
	}

	@Override
	public void setMap( String map ) {
		this.map = map;
	}

	@Override
	public <T> T query( QueryExecutor<T> exe )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public Model construct( String q )
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public void execute( ModificationExecutor exe ) throws RepositoryException {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	protected static File searchFor( String filename, File... dirs ) {
		if ( null != filename ) {
			File orig = new File( filename );
			if ( orig.isAbsolute() && orig.exists() ) {
				return orig;
			}

			final String basefilename = orig.getName();
			final List<String> dirparts
					= new ArrayList<>( Arrays.asList( filename.split( Pattern.quote(
													File.separator ) ) ) );
			// we're going to use the filename anyway, so we can remove it from the list
			if ( !dirparts.isEmpty() ) {
				dirparts.remove( dirparts.size() - 1 );
			}

			// special case: filename is db/XXX, we can skip the db part
			if ( !dirparts.isEmpty() && "db".equals( dirparts.get( 0 ) ) ) {
				dirparts.remove( 0 );
			}

			List<File> searchpath = new ArrayList<>();
			for ( File dir : dirs ) {
				searchpath.add( new File( dir, basefilename ) );
				StringBuilder sb = new StringBuilder();
				for ( String dirpart : dirparts ) {
					sb.append( File.separator ).append( dirpart );

					File check = new File( dir + sb.toString(), basefilename );
					searchpath.add( check );
				}
			}
			searchpath.add( orig );

			Set<String> checked = new HashSet<>(); // don't check the same place twice
			for ( File loc : searchpath ) {
				if ( !checked.contains( loc.getAbsolutePath() ) ) {
					try {
						checked.add( loc.getAbsolutePath() );

						log.debug( "looking for " + filename + " as " + loc.
								getAbsolutePath() );
						if ( loc.exists() ) {
							log.debug( "using " + loc.getCanonicalPath() );
							return loc.getCanonicalFile();
						}
					}
					catch ( IOException ioe ) {
						log.error( "could not access file: " + loc.getAbsolutePath(), ioe );
					}
				}
			}
		}

		return null;
	}

	@Override
	public void setProperty( String key, String val ) {
		if ( null == val || val.isEmpty() ) {
			prop.remove( key );
		}
		else {
			prop.setProperty( key, val );
		}
	}

	@Override
	public Properties getProperties() {
		Properties p = new Properties();
		p.putAll( prop );
		return p;
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
	public String toString() {
		return engineName;
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
						MetadataConstants.VOID_DS, false );
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
	public org.openrdf.model.URI getBaseUri() {
		return databuilder.toUri();
	}

	protected void setDataBuilder( UriBuilder b ) {
		databuilder = b;
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
		log.warn( "Namespace handling not yet implemented" );
		return new HashMap<>();
	}
}
