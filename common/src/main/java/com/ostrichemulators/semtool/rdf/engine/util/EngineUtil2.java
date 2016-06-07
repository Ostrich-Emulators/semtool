/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMCORE;
import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.ImportValidationException;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.rdf.engine.impl.AbstractEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.EngineFactory;
import com.ostrichemulators.semtool.rdf.engine.impl.InsightManagerImpl;
import com.ostrichemulators.semtool.rdf.engine.impl.SesameEngine;
import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.VoidQueryAdapter;
import com.ostrichemulators.semtool.user.LocalUserImpl;
import com.ostrichemulators.semtool.user.Security;
import com.ostrichemulators.semtool.user.User;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

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
			metas.remove( SEMTOOL.Database );

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
			MetadataQuery mq = new MetadataQuery( SEMTOOL.ReificationModel );
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
	 * Creates an empty database by copying data from the db/Default directory
	 *
	 * @param ecb how the new database should be created
	 * @param conformanceErrors if not null, conformance will be checked, and
	 * errors will be placed here
	 *
	 * @return the newly-created smss file, or null if something goes wrong
	 *
	 * @throws java.io.IOException
	 * @throws
	 * com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException
	 */
	public static File createNew( EngineCreateBuilder ecb, ImportData conformanceErrors )
			throws IOException, EngineManagementException {

		File dbtopdir = ecb.getEngineDir();
		dbtopdir.mkdirs();

		User user = new LocalUserImpl();
		File smssfile = createEngine( ecb, user );

		IEngine bde = EngineFactory.getEngine( smssfile.getAbsoluteFile() );
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
		//File jnl = new File( enginedir, dbname + ".jnl" );
		File db = new File( enginedir, dbname );

		if ( db.exists() ) {
			throw new IOException( "KB journal already exists" );
		}

		smssprops = SesameEngine.generateProperties( db );
		//smssprops.setProperty( BigdataSail.Options.FILE, jnl.getAbsolutePath() );

		if ( log.isDebugEnabled() ) {
			StringBuilder sb = new StringBuilder( "creation properties:" );
			for ( String key : smssprops.stringPropertyNames() ) {
				sb.append( "\n" ).append( key ).append( "=>" ).append( smssprops.getProperty( key ) );
			}
			log.debug( sb.toString() );
		}

		Repository repo = new SailRepository( new ForwardChainingRDFSInferencer(
				new NativeStore( new File( smssprops.getProperty(
						SesameEngine.REPOSITORY_KEY ) ) ) ) );

		//BigdataSail sail = new BigdataSail( smssprops );
		//BigdataSailRepository repo = new BigdataSailRepository( sail );
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
			rc.add( new StatementImpl( baseuri, RDF.TYPE, SEMTOOL.Database ) );
			Date today = new Date();
			rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_CREATED,
					vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );
			rc.add( new StatementImpl( baseuri, MetadataConstants.DCT_MODIFIED,
					vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );

			rc.add( new StatementImpl( baseuri, SEMTOOL.ReificationModel,
					ecb.getReificationModel().uri ) );

			String tooling = Utility.getBuildProperties( EngineUtil2.class )
							.getProperty( "name", "unknown" );
			rc.add( new StatementImpl( baseuri, SEMCORE.SOFTWARE_AGENT,
					vf.createLiteral( tooling ) ) );

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

		return db;
	}

	private static List<Statement> getStatementsFromResource( URL resource,
			RDFFormat fmt ) {

		TurtleParser tp = new TurtleParser();
		StatementCollector coll = new StatementCollector();
		tp.setRDFHandler( coll );
		try ( InputStream is = resource.openStream() ) {
			tp.parse( is, SEMONTO.BASE_URI );
		}
		catch ( Exception e ) {
			log.warn( "could not open/parse model resource: " + resource, e );
		}

		return new ArrayList<>( coll.getStatements() );
	}

	public static void createInsightStatements( File modelquestions,
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
