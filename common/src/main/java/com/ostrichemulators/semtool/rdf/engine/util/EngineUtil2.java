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
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

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
			final Map<IRI, Value> metas = engine.query( new MetadataQuery() );
			metas.remove( SEMTOOL.Database );

			engine.execute( new ModificationExecutorAdapter( true ) {

				@Override
				public void exec( RepositoryConnection conn ) throws RepositoryException {
					conn.remove( (Resource) null, null, null );
					ValueFactory vf = conn.getValueFactory();

					// re-add the metadata
					for ( Map.Entry<IRI, Value> en : metas.entrySet() ) {
						conn.add( engine.getBaseIri(),
								IRI.class.cast( EngineLoader.cleanValue( en.getKey(), vf ) ),
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
		IRI reif = Constants.NONODE;
		if ( null != engine ) {
			MetadataQuery mq = new MetadataQuery( SEMTOOL.ReificationModel );
			try {
				engine.query( mq );
				Value str = mq.getOne();
				reif = ( null == str ? Constants.NONODE : IRI.class.cast( str ) );
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
			metas = new ImportMetadata( eng.getBaseIri(), eng.getSchemaBuilder(),
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

		Properties smssprops = new Properties();

		File db;

		db = new File( enginedir, dbname );
		smssprops = SesameEngine.generateProperties( db );

		if ( db.exists() ) {
			throw new IOException( "KB journal already exists" );
		}

		if ( log.isDebugEnabled() ) {
			StringBuilder sb = new StringBuilder( "creation properties:" );
			for ( String key : smssprops.stringPropertyNames() ) {
				sb.append( System.getProperty( "line.separator" ) )
						.append( key ).append( "=>" ).append( smssprops.getProperty( key ) );
			}
			log.debug( sb.toString() );
		}

		NativeStore store
				= new NativeStore( new File( smssprops.getProperty( SesameEngine.REPOSITORY_KEY ) ) );
		Sail sail = ( ecb.isCalcInfers()
				? new SchemaCachingRDFSInferencer( store )
				: store );
		Repository repo = new SailRepository( sail );
		try {
			repo.init();
			try (RepositoryConnection rc = repo.getConnection()) {
				if ( ecb.isDoMetamodel() ) {
					rc.begin();
					for ( URL url : ecb.getVocabularies() ) {
						rc.add( getStatementsFromResource( url, RDFFormat.TURTLE ) );
					}
					rc.commit();
				}

				IRI baseuri = AbstractEngine.getNewBaseUri();

				// add the metadata
				rc.begin();
				ValueFactory vf = rc.getValueFactory();
				rc.add( vf.createStatement( baseuri, RDF.TYPE, SEMTOOL.Database ) );
				Date today = new Date();
				rc.add( vf.createStatement( baseuri, MetadataConstants.DCT_CREATED,
						vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );
				rc.add( vf.createStatement( baseuri, MetadataConstants.DCT_MODIFIED,
						vf.createLiteral( QueryExecutorAdapter.getCal( today ) ) ) );

				rc.add( vf.createStatement( baseuri, SEMTOOL.ReificationModel,
						ecb.getReificationModel().uri ) );

				String tooling = Utility.getBuildProperties( EngineUtil2.class )
						.getProperty( "name", "unknown" );
				rc.add( vf.createStatement( baseuri, SEMCORE.SOFTWARE_AGENT,
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

					rc.add( vf.createStatement( baseuri, MetadataConstants.DCT_PUBLISHER,
							vf.createLiteral( poc.toString() ) ) );
				}
				if ( !org.isEmpty() ) {
					rc.add( vf.createStatement( baseuri, MetadataConstants.DCT_CREATOR,
							vf.createLiteral( org ) ) );
				}

				rc.commit();
			}
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
			tp.parse( is, SEMONTO.BASE_IRI );
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
			repo.init();
			if ( FilenameUtils.isExtension( modelquestions.toString(), extfmt.keySet() ) ) {
				try (RepositoryConnection rc = repo.getConnection()) {
					rc.add( modelquestions, "",
							extfmt.get( FilenameUtils.getExtension( modelquestions.toString() ) ) );
					Model m = QueryResults.asModel( rc.getStatements( null, null, null ) );
					loader.loadFromModel( m );
				}
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
