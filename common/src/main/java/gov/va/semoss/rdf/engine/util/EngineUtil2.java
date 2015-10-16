/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import gov.va.semoss.model.vocabulary.VAC;
import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.ImportValidationException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.AbstractEngine;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.impl.InsightManagerImpl;
import gov.va.semoss.rdf.query.util.MetadataQuery;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.rdf.query.util.QueryExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.VoidQueryAdapter;
import gov.va.semoss.user.LocalUserImpl;
import gov.va.semoss.user.Security;
import gov.va.semoss.user.User;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.Utility;
import info.aduna.iteration.Iterations;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A class to centralize Engine operations. This class is thread-safe, and if
 * started as a thread and used to mount repositories, those repositories can be
 * successfully unmounted later.
 *
 * @author ryan
 */
public class EngineUtil2 {

	private static final Logger log = Logger.getLogger( EngineUtil2.class );

	private EngineUtil2() {
	}

	public static void clear( IEngine engine ) throws RepositoryException {
		try {
			final Map<URI, Value> metas = engine.query( new MetadataQuery() );
			metas.remove( VAS.Database );

			engine.execute( new ModificationExecutorAdapter( true ) {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					conn.remove( (Resource) null, null, null );
					ValueFactory vf = conn.getValueFactory();

					// re-add the metadata
					for ( Map.Entry<URI, Value> en : metas.entrySet() ) {
						conn.add( engine.getBaseUri(),
								URI.class.cast( EngineLoader.cleanValue( en.getKey(), vf ) ),
								EngineLoader.cleanValue( en.getValue(), vf ) );
					}
				}
			} );
		}
		catch ( MalformedQueryException | QueryEvaluationException e ) {
			log.error( e, e );
		}
	}

	public static String getEngineLabel( IEngine engine ) {
		String label = engine.getEngineName();
		MetadataQuery mq = new MetadataQuery( RDFS.LABEL );
		engine.queryNoEx( mq );
		String str = mq.getString();
		if ( null != str ) {
			label = str;
		}
		return label;
	}

	/**
	 * Gets the reification model URI from the given engine
	 *
	 * @param engine
	 * @return return the reification model, or {@link Constants#NONODE} if none
	 * is defined
	 */
	public static ReificationStyle getReificationStyle( IEngine engine ) {
		URI reif = Constants.NONODE;
		if ( null != engine ) {
			MetadataQuery mq = new MetadataQuery( VAS.ReificationModel );
			try {
				engine.query( mq );
				Value str = mq.getOne();
				reif = ( null == str ? Constants.NONODE : URI.class.cast( str ) );
			}
			catch ( RepositoryException | MalformedQueryException | QueryEvaluationException e ) {
				// don't care
			}
		}

		return ReificationStyle.fromUri( reif );
	}

	public static ImportData createImportData( IEngine eng ) {
		ImportMetadata metas = null;
		if ( null == eng ) {
			metas = new ImportMetadata();
		}
		else {
			metas = new ImportMetadata( eng.getBaseUri(), eng.getSchemaBuilder(),
					eng.getDataBuilder() );
			metas.setNamespaces( eng.getNamespaces() );
		}

		return new ImportData( metas );
	}

	/**
	 * Prints all statements in the main database to the log's DEBUG output. This
	 * only works in if the logger prints debug output.
	 *
	 * @param eng
	 */
	public static void logAllDataStatements( IEngine eng ) {
		if ( log.isDebugEnabled() ) {
			eng.queryNoEx( new VoidQueryAdapter( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" ) {
				@Override
				public void handleTuple( BindingSet set, ValueFactory fac ) {
					log.debug( set.getValue( "s" ) + " " + set.getValue( "p" ) + " "
							+ set.getValue( "o" ) );
				}
			} );
		}
	}

	/**
	 * Factory method for loading an engine.
	 *
	 * @param smssfile
	 *
	 * @return Loaded engine.
	 *
	 * @throws java.io.IOException
	 */
	public static IEngine loadEngine( File smssfile ) throws IOException {
		log.debug( "In Utility file name is " + smssfile );
		String smssloc = smssfile.getCanonicalPath();
		IEngine engine = null;
		String engineName = FilenameUtils.getBaseName( smssloc );

		if ( "jnl".equalsIgnoreCase(
				FilenameUtils.getExtension( smssfile.getName() ).toLowerCase() ) ) {
			// we're loading a BigData journal file, so jump straight to its ctor
			engine = new BigDataEngine( smssfile );
		}
		else {
			Properties props = Utility.loadProp( smssfile );
			engineName = props.getProperty( Constants.ENGINE_NAME, engineName );

			String engineClass = props.getProperty( Constants.ENGINE_IMPL );
			engineClass = engineClass.replaceAll( "prerna", "gov.va.semoss" );

			try {
				Class<IEngine> theClass = (Class<IEngine>) Class.forName( engineClass );
				engine = (IEngine) theClass.getConstructor( Properties.class ).newInstance( props );

			}
			catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				log.error( e );
			}
		}

		if ( null == engine ) {
			throw new IOException( "Could not create engine" );
		}

		if ( null == engine.getEngineName() && null != engineName ) {
			engine.setEngineName( engineName );
		}

		engine.setProperty( Constants.SMSS_LOCATION, smssloc );

		return engine;
	}

	/**
	 * Pair for {@link #loadEngine(java.io.File) }. Implementation simply calls
	 * {@link IEngine#closeDB() }
	 *
	 * @param eng
	 */
	public static void closeEngine( IEngine eng ) {
		eng.closeDB();
	}

	/**
	 * Creates an empty database by copying data from the db/Default directory
	 *
	 * @param ecb how the new database should be created
	 * @param conformanceErrors if not null, conformance will be checked, and
	 * errors will be placed here
	 *
	 * @return the newly-created smss file, or null if something goes wrong
	 *
	 * @throws java.io.IOException
	 * @throws gov.va.semoss.rdf.engine.util.EngineManagementException
	 */
	public static File createNew( EngineCreateBuilder ecb, ImportData conformanceErrors )
			throws IOException, EngineManagementException {

		File dbtopdir = ecb.getEngineDir();
		dbtopdir.mkdirs();

		User user = new LocalUserImpl();
		File smssfile = createEngine( ecb, user );

		IEngine bde = EngineUtil2.loadEngine( smssfile.getAbsoluteFile() );
		Security.getSecurity().associateUser( bde, user );

		if ( null != ecb.getQuestions() ) {
			InsightManagerImpl imi = new InsightManagerImpl();
			createInsightStatements( ecb.getQuestions(), imi );
			bde.updateInsights( imi );
		}

		EngineLoader el = new EngineLoader( ecb.isStageInMemory() );
		el.setDefaultBaseUri( ecb.getDefaultBaseUri(), ecb.isDefaultBaseOverridesFiles() );

		try {
			el.loadToEngine( ecb.getFiles(), bde, ecb.isDoMetamodel(), conformanceErrors );
			if ( ecb.isCalcInfers() ) {
				bde.calculateInferences();
			}
		}
		catch ( ImportValidationException | RepositoryException e ) {
			throw new EngineManagementException( e.getMessage(), e );
		}
		finally {
			el.release();
			bde.closeDB();
		}

		return smssfile;
	}

	private static File createEngine( EngineCreateBuilder ecb, User user )
			throws IOException, EngineManagementException {

		String dbname = ecb.getEngineName();
		File enginedir = ecb.getEngineDir();
		File modelmap = ecb.getMap();

		if ( null != modelmap && modelmap.exists() ) {
			try {
				FileUtils.copyFile( modelmap, new File( enginedir,
						AbstractEngine.getDefaultName( Constants.ONTOLOGY, dbname ) ) );
			}
			catch ( IOException e ) {
				log.error( e, e );
				return null;
			}
		}

		Properties smssprops = new Properties();
		File modelsmss = ecb.getSmss();
		if ( null == modelsmss || !modelsmss.exists() ) {
			String dprop = "/defaultdb/Default.properties";
			smssprops.load( EngineUtil2.class.getResourceAsStream( dprop ) );
		}
		else {
			try ( FileReader rdr = new FileReader( modelsmss ) ) {
				smssprops.load( rdr );
			}
			catch ( IOException e ) {
				log.error( e, e );
				return null;
			}
		}

		// make the big data journal, and then write out the (empty) OWL file
		File jnl = new File( enginedir, dbname + ".jnl" );

		if ( jnl.exists() ) {
			throw new IOException( "KB journal already exists" );
		}

		smssprops.setProperty( BigdataSail.Options.FILE, jnl.getAbsolutePath() );

		BigdataSail sail = new BigdataSail( smssprops );
		BigdataSailRepository repo = new BigdataSailRepository( sail );
		try {
			repo.initialize();
			RepositoryConnection rc = repo.getConnection();

			if ( ecb.isDoMetamodel() ) {
				rc.begin();
				for ( URL url : ecb.getVocabularies() ) {
					rc.add( getStatementsFromResource( url, RDFFormat.TURTLE ) );
				}
				rc.commit();
			}

			URI baseuri = AbstractEngine.getNewBaseUri();

			// add the metadata
			rc.begin();
			ValueFactory vf = rc.getValueFactory();
			rc.add( new StatementImpl( baseuri, RDF.TYPE, VAS.Database ) );
			Date today = new Date();
			rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_CREATED,
					vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );
			rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_MODIFIED,
					vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );

			rc.add( new StatementImpl( baseuri, VAS.ReificationModel,
					ecb.getReificationModel().uri ) );

			rc.add( new StatementImpl( baseuri, VAC.SOFTWARE_AGENT,
					vf.createLiteral( System.getProperty( "build.name", "unknown" ) ) ) );

			String username = user.getProperty( User.UserProperty.USER_FULLNAME );
			String email = user.getProperty( User.UserProperty.USER_EMAIL );
			String org = user.getProperty( User.UserProperty.USER_ORG );

			if ( !( username.isEmpty() && email.isEmpty() ) ) {
				StringBuilder poc = new StringBuilder();
				if ( username.isEmpty() ) {
					poc.append( email );
				}
				else {
					poc.append( username );
				}
				if ( !email.isEmpty() ) {
					poc.append( " <" ).append( email ).append( ">" );
				}

				rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_PUBLISHER,
						vf.createLiteral( poc.toString() ) ) );
			}
			if ( !org.isEmpty() ) {
				rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_CREATOR,
						vf.createLiteral( org ) ) );
			}

			rc.commit();

			rc.close();
			repo.shutDown();
		}
		catch ( RepositoryException e ) {
			log.error( e, e );
			return null;
		}

		return jnl;
	}

	private static List<Statement> getStatementsFromResource( URL resource,
			RDFFormat fmt ) {
		List<Statement> stmts = new ArrayList<>();

		Repository repo = null;
		RepositoryConnection rc = null;

		try ( InputStream is = resource.openStream() ) {
			repo = new SailRepository( new MemoryStore() );
			repo.initialize();
			rc = repo.getConnection();

			rc.add( is, Constants.DEFAULT_SEMOSS_URI, fmt );
			rc.commit();
			stmts.addAll( Iterations.asList( rc.getStatements( null, null, null, false ) ) );
		}
		catch ( Exception e ) {
			log.warn( "could not open/parse model resource: " + resource, e );
		}
		finally {
			if ( null != rc ) {
				try {
					rc.close();
				}
				catch ( Exception e ) {
					log.warn( "could not remove temp rc", e );
				}
			}
			if ( null != repo ) {
				try {
					repo.shutDown();
				}
				catch ( Exception e ) {
					log.warn( "could not remove temp rc", e );
				}
			}
		}

//		log.debug( "subjects from resource: " + resource );
//		Set<Resource> uris = new HashSet<>();
//		for ( Statement s : stmts ) {
//			uris.add( s.getSubject() );
//		}
//		for ( Resource u : uris ) {
//			log.debug( u );
//		}
		return stmts;
	}

	static void createInsightStatements( File modelquestions,
			InsightManagerImpl imi ) throws IOException, EngineManagementException {

		if ( null == modelquestions || !modelquestions.exists() ) {
			return;
		}

		Map<String, RDFFormat> extfmt = new HashMap<>();

		extfmt.put( "ttl", RDFFormat.TURTLE );
		extfmt.put( "rdf", RDFFormat.RDFXML );
		extfmt.put( "n3", RDFFormat.N3 );
		extfmt.put( "nt", RDFFormat.NTRIPLES );

		// we need to check that we actually loaded SOME perspectives, so we'll load
		// a temporary InsightManager first
		InsightManagerImpl loader = new InsightManagerImpl();

		try {
			Repository repo = new SailRepository( new MemoryStore() );
			repo.initialize();
			if ( FilenameUtils.isExtension( modelquestions.toString(), extfmt.keySet() ) ) {
				RepositoryConnection rc = repo.getConnection();
				rc.add( modelquestions, "",
						extfmt.get( FilenameUtils.getExtension( modelquestions.toString() ) ) );
				loader.loadFromRepository( rc );
				rc.close();
				repo.shutDown();
			}
			else {
				Properties p = Utility.loadProp( modelquestions );
				loader.loadLegacyData( p );
			}
		}
		catch ( RepositoryException | RDFParseException e ) {
			throw new EngineManagementException( EngineManagementException.ErrorCode.FILE_ERROR,
					e );
		}

		boolean ok = !loader.getPerspectives().isEmpty();

		if ( !ok ) {
			throw new EngineManagementException( EngineManagementException.ErrorCode.MISSING_REQUIRED_TUPLE,
					modelquestions + " does not contain any Perspectives" );
		}

		imi.addAll( loader.getPerspectives(), false );
	}
}
