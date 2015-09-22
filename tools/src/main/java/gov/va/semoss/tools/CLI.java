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
package gov.va.semoss.tools;

import com.bigdata.journal.IIndexManager;
import com.bigdata.journal.ITx;
import com.bigdata.journal.Journal;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.CreateKBTask;
import com.bigdata.rdf.sail.webapp.NanoSparqlServer;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.task.AbstractApiTask;
import gov.va.semoss.poi.main.ImportValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.util.EngineCreateBuilder;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.ui.components.ImportDataProcessor;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;

import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.rdf.query.util.UpdateExecutorAdapter;
import static gov.va.semoss.util.RDFDatatypeTools.getRDFStringValue;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jetty.server.Server;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class CLI {

	private static final Logger log = Logger.getLogger( CLI.class );
	private static final String TRIALS[] = { "create", "update", "replace",
		"export", "server" };

	private IEngine engine;
	private File dbfile;
	private boolean stageInMemory;
	private boolean closure;
	private boolean conformance;
	private boolean createMetamodel;
	private ImportData errors;
	private final CommandLine cmd;

	public CLI( String[] args ) throws ParseException, ShowHelpException {
		Options options = createOptions();
		cmd = getCommandLine( options, args );
		if ( cmd.hasOption( "help" ) || 0 == cmd.getOptions().length ) {
			throw new ShowHelpException();
		}

	}

	public void execute()
			throws IOException, EngineManagementException, RepositoryException,
			ImportValidationException, MalformedQueryException, UpdateExecutionException {
		int mode = setDatabaseFile( cmd );

		switch ( mode ) {
			case 0:
			case 2:
				create( cmd, ( 2 == mode ) );
				break;
			case 1:
				update( cmd );
				break;
			case 3:
				export( cmd );
				break;
			case 4:
				serve( cmd );
				break;
		}
	}

//	public void execute() throws IOException, EngineManagementException {
//		// get a DB handle:
//		// Engine engine = null;
//		CommandLine cmd = null;
//		Collection<File> loads = new ArrayList<>();
//		if ( cmd.hasOption( "load" ) ) {
//			String[] loadArgs = cmd.getOptionValues( "load" );
//			// split load by space
//
//			for ( String load : loadArgs ) {
//				File loadFile = new File( load );
//				if ( !loadFile.exists() ) {
//					throw new FileNotFoundException( "Could not find: " + load );
//				}
//				loads.add( loadFile );
//			}
//		}
//
//		Collection<URL> vocabs = new ArrayList<>();
//		if ( cmd.hasOption( "vocab" ) ) {
//			String[] vos = cmd.getOptionValues( "vocab" );
//			// split load by space
//
//			for ( String load : vos ) {
//				File loadFile = new File( load );
//				if ( !loadFile.exists() ) {
//					throw new FileNotFoundException( "Could not find: " + load );
//				}
//				vocabs.add( loadFile.toURI().toURL() );
//			}
//		}
//
//		// we need parameters for the following:
//		stageInMemory = !cmd.hasOption( "stage-on-disk" );
//		closure = cmd.hasOption( "closure" ); // calculate inferences
//		conformance = cmd.hasOption( "conformance" ); // perform conformance tests
//		createMetamodel = !cmd.hasOption( "no-metamodel" ); // create metamodel
//		errors = ( conformance ? new ImportData() : null );
//
//		File smss = null;
//
//		String baseURI = ( cmd.hasOption( "baseuri" )
//				? cmd.getOptionValue( "baseuri" ) : "http://semoss.test/database" );
//
//		if ( cmd.hasOption( "out" ) ) {
//			File db = new File( cmd.getOptionValue( "out" ) ).getAbsoluteFile();
//			File dbdir = db.getParentFile();
//			if ( !dbdir.exists() ) {
//				if ( !dbdir.mkdirs() ) {
//					throw new FileNotFoundException( "Could not create output directory/file" );
//				}
//			}
//
//			EngineCreateBuilder ecb = new EngineCreateBuilder( dbdir,
//					FilenameUtils.getBaseName( db.getName() ) )
//					.setDefaultBaseUri( new URIImpl( baseURI ), false )
//					.setReificationModel( ReificationStyle.SEMOSS )
//					.setFiles( loads )
//					.setVocabularies( vocabs )
//					.setBooleans( stageInMemory, closure, createMetamodel );
//
//			if ( cmd.hasOption( "insights" ) ) {
//				ecb.setDefaultsFiles( null, null, cmd.getOptionValue( "insights" ) );
//			}
//
//			smss = EngineUtil.createNew( ecb, errors );
//
//			// set the metadata on the just-created database
//			final Map<URI, String> metadatas = getMetadata( cmd );
//
//			if ( !metadatas.isEmpty() ) {
//				// set the metadata on the just-created database
//				IEngine engine = GuiUtility.loadEngine( smss );
//				try {
//					engine.execute( new ModificationExecutorAdapter() {
//
//						@Override
//						public void exec( RepositoryConnection conn ) throws RepositoryException {
//							ValueFactory vf = conn.getValueFactory();
//							for ( Map.Entry<URI, String> en : metadatas.entrySet() ) {
//								Value val = getRDFStringValue( en.getValue(),
//										engine.getNamespaces(), vf );
//								conn.add( engine.getBaseUri(), en.getKey(), val );
//							}
//						}
//					} );
//
//					engine.commit();
//					GuiUtility.closeEngine( engine );
//				}
//				catch ( Exception e ) {
//					logger.error( e, e );
//				}
//			}
//		}
//		else if ( cmd.hasOption( "update" ) ) {
//			String update = cmd.getOptionValue( "update" );
//			boolean replace = cmd.hasOption( "replace" );
//
//			smss = new File( update );
//			if ( null == update || !smss.exists() ) {
//				throw new FileNotFoundException( "Journal not found:  " + update );
//			}
//
//			IEngine engine = GuiUtility.loadEngine( smss );
//			if ( replace ) {
//				ImportDataProcessor.clearEngine( engine, loads );
//			}
//
//			try {
//				EngineLoader el = new EngineLoader( stageInMemory );
//				el.loadToEngine( loads, engine, createMetamodel, errors );
//				el.release();
//				// if we get here, no exceptions have been thrown, so we're good
//			}
//			catch ( ImportValidationException | RepositoryException | IOException ioe ) {
//				logger.error( ioe, ioe );
//			}
//		}
//
//		if ( cmd.hasOption( "sparql" ) ) {
//			String sparql = cmd.getOptionValue( "sparql" );
//			// run an update , save updated db
//		}
//	}
	private Map<URI, String> getMetadata( CommandLine cmd ) {
		Map<URI, String> map = new HashMap<>();

		Map<String, URI> mets = new HashMap<>();
		mets.put( "organization", DCTERMS.CREATOR );
		mets.put( "poc", DCTERMS.PUBLISHER );
		mets.put( "summary", DCTERMS.DESCRIPTION );
		mets.put( "title", RDFS.LABEL );

		for ( Map.Entry<String, URI> en : mets.entrySet() ) {
			if ( cmd.hasOption( en.getKey() ) ) {
				String rawval = cmd.getOptionValue( en.getKey() );
				map.put( en.getValue(), rawval );
			}
		}

		return map;
	}

	/**
	 * Command line interpreter to create and modify semoss databases.
	 *
	 * @param args String[] - the Main method.
	 *
	 * @throws java.lang.Exception
	 */
	public static void main( String[] args ) throws Exception {
		CLI mossy = null;
		try {
			mossy = new CLI( args );
			mossy.execute();
		}
		catch ( ParseException exp ) {
			System.err.println( exp );
			System.exit( -1 );
		}
		catch ( ShowHelpException exp ) {
			// automatically generate the help statement
			String header = "Do something useful with an input file\n\n";
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth( 140 );
			formatter.printHelp( "mossy", header, createOptions(), "", true );
		}
		finally {
			if ( null != mossy ) {
				mossy.release();
			}
		}
	}

	public static Options createOptions() {
		Options options = new Options();
		Option help = new Option( "help", "Print this message" );

		OptionBuilder.withArgName( "file.[xlsx|csv|ttl|rdf|rdfs|owl|n3|jnl]+" );
		OptionBuilder.hasArgs( 10 );
		OptionBuilder.withDescription( "Data to import." );
		Option load = OptionBuilder.create( "data" );

		OptionBuilder.withArgName( "file.[ttl|rdf|rdfs|owl|n3]+" );
		OptionBuilder.hasArgs( 10 );
		OptionBuilder.withDescription( "Vocabularies to import." );
		Option vocab = OptionBuilder.create( "vocab" );

		OptionBuilder.withArgName( "uri" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Set the Base URI for loads" );
		Option baseuri = OptionBuilder.create( "baseuri" );

		OptionBuilder.withArgName( "insights.ttl+" );
		OptionBuilder.hasArgs( 10 );
		OptionBuilder.withDescription( "Insights to import." );
		Option insights = OptionBuilder.create( "insights" );

		OptionBuilder.withArgName( "old.jnl" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Replace existing data" );
		Option replace = OptionBuilder.create( "replace" );

		Option insightsdb
				= new Option( "insightsdb", "Only operate on the Insights DB?" );

		OptionBuilder.withArgName( "file.[sparql|spq]+" );
		OptionBuilder.hasArgs( 10 );
		OptionBuilder.withDescription( "A SPARQL update expression(s) to evaluate." );
		Option sparql = OptionBuilder.create( "sparql" );

		OptionBuilder.withArgName( "old.jnl" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Update an existing database." );
		Option update = OptionBuilder.create( "update" );

		OptionBuilder.withArgName( "new.jnl" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Create a new database." );
		Option create = OptionBuilder.create( "create" );

		OptionBuilder.withArgName( "string" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Add creator metadata to database." );
		Option creator = OptionBuilder.create( "organization" );

		OptionBuilder.withArgName( "string" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Add description metadata to database." );
		Option desc = OptionBuilder.create( "summary" );

		OptionBuilder.withArgName( "string" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Add label metadata to database." );
		Option label = OptionBuilder.create( "title" );

		OptionBuilder.withArgName( "string" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Add POC metadata to database." );
		Option publisher = OptionBuilder.create( "poc" );

		OptionBuilder.withArgName( "file.[ttl|rdf|nt]" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Export data to a file." );
		Option export = OptionBuilder.create( "export" );

		OptionBuilder.withArgName( "old.jnl" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Start a Sparql Endpoint." );
		Option server = OptionBuilder.create( "server" );

		OptionGroup modes = new OptionGroup();
		modes.addOption( create );
		modes.addOption( update );
		modes.addOption( replace );
		modes.addOption( server );
		modes.addOption( export );

		options.addOption( help );
		options.addOption( load );
		options.addOption( insights );
		options.addOption( insightsdb );
		options.addOption( baseuri );
		options.addOption( creator );
		options.addOption( desc );
		options.addOption( label );
		options.addOption( publisher );
		options.addOption( vocab );
		options.addOptionGroup( modes );

		return options;
	}

	public static CommandLine getCommandLine( Options options, String[] args )
			throws ParseException {

		PosixParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args );
		return cmd;
	}

	public int setDatabaseFile( CommandLine cmd ) {
		for ( int i = 0; i < TRIALS.length; i++ ) {
			if ( cmd.hasOption( TRIALS[i] ) ) {
				String val = cmd.getOptionValue( TRIALS[i] );
				dbfile = new File( val );

				return i;
			}
		}

		return -1;
	}

	public void release() {
		if ( null != engine ) {
			engine.closeDB();
		}
	}

	public void create( CommandLine cmd, boolean replace )
			throws IOException, EngineManagementException, RepositoryException {
		setLoadingOptions( cmd );

		List<File> data = getFileList( cmd, "data" );
		List<File> insights = getFileList( cmd, "insights" );

		List<File> vocabfiles = getFileList( cmd, "vocab" );
		Collection<URL> vocab = new ArrayList<>();
		for ( File f : vocabfiles ) {
			vocab.add( f.toURI().toURL() );
		}

		String baseURI = ( cmd.hasOption( "baseuri" )
				? cmd.getOptionValue( "baseuri" ) : "http://semoss.test/database" );

		File dbdir = dbfile.getParentFile();
		if ( !dbdir.exists() ) {
			if ( !dbdir.mkdirs() ) {
				throw new FileNotFoundException( "Could not create output directory/file" );
			}
		}

		if ( replace && dbfile.exists() ) {
			engine = new BigDataEngine( BigDataEngine.generateProperties( dbfile ) );

			if ( cmd.hasOption( "insightsdb" ) ) {
				EngineUtil.getInstance().importInsights( engine, insights.get( 0 ),
						true, vocab );
				engine.commit();
				return;
			}
			else {
				ImportDataProcessor.clearEngine( engine, data );
				load( engine, data );
			}
		}
		else {
			if ( replace ) {
				log.warn( "creating new database instead of replacing: " + dbfile );
			}

			EngineCreateBuilder ecb = new EngineCreateBuilder( dbdir,
					FilenameUtils.getBaseName( dbfile.getName() ) )
					.setDefaultBaseUri( new URIImpl( baseURI ), false )
					.setReificationModel( ReificationStyle.SEMOSS )
					.setFiles( data )
					.setVocabularies( vocab )
					.setBooleans( stageInMemory, closure, createMetamodel );

			if ( !insights.isEmpty() ) {
				ecb.setDefaultsFiles( null, null, insights.get( 0 ) );
			}

			File smss = EngineUtil.createNew( ecb, errors );
			engine = new BigDataEngine( BigDataEngine.generateProperties( smss ) );
		}

		// set the metadata on the just-created database
		final Map<URI, String> metadatas = getMetadata( cmd );

		if ( !metadatas.isEmpty() ) {
			// set the metadata on the just-created database
			try {
				engine.execute( new ModificationExecutorAdapter() {

					@Override
					public void exec( RepositoryConnection conn ) throws RepositoryException {
						ValueFactory vf = conn.getValueFactory();
						for ( Map.Entry<URI, String> en : metadatas.entrySet() ) {
							Value val = getRDFStringValue( en.getValue(),
									engine.getNamespaces(), vf );
							conn.add( engine.getBaseUri(), en.getKey(), val );
						}
					}
				} );

				engine.commit();
			}
			catch ( Exception e ) {
				log.error( e, e );
			}
		}
	}

	public void update( CommandLine cmd ) throws IOException, RepositoryException,
			ImportValidationException, EngineManagementException, MalformedQueryException, UpdateExecutionException {

		setLoadingOptions( cmd );
		if ( !dbfile.exists() ) {
			throw new FileNotFoundException( dbfile.getAbsolutePath() );
		}

		engine = new BigDataEngine( BigDataEngine.generateProperties( dbfile ) );

		if ( cmd.hasOption( "insightsdb" ) ) {
			List<File> vocabfiles = getFileList( cmd, "vocab" );
			Collection<URL> vocab = new ArrayList<>();
			for ( File f : vocabfiles ) {
				vocab.add( f.toURI().toURL() );
			}

			List<File> data = getFileList( cmd, "insights" );
			data.addAll( getFileList( cmd, "sparql" ) );

			EngineUtil.getInstance().importInsights( engine, dbfile, false, vocab );
		}
		else {
			List<File> data = getFileList( cmd, "data" );
			load( engine, data );

			for ( File f : getFileList( cmd, "sparql" ) ) {
				String sparql = FileUtils.readFileToString( f );
				engine.update( new UpdateExecutorAdapter( sparql ) {
				} );
			}
		}
	}

	public void export( CommandLine cmd ) throws IOException, RepositoryException {
		if ( !dbfile.exists() ) {
			throw new FileNotFoundException( dbfile.getAbsolutePath() );
		}

		engine = new BigDataEngine( BigDataEngine.generateProperties( dbfile ) );
	}

	public void serve( CommandLine cmd ) throws IOException, RepositoryException {
		if ( !dbfile.exists() ) {
			throw new FileNotFoundException( dbfile.getAbsolutePath() );
		}

		try {
			// the journal is the file itself
			Properties dbprops = BigDataEngine.generateProperties( dbfile );
			Journal journal = new Journal( dbprops );

			// the main KB
			dbprops.setProperty( BigdataSail.Options.NAMESPACE, "kb" );
			CreateKBTask ctor = new CreateKBTask( "kb", dbprops );
			try {
				AbstractApiTask.submitApiTask( journal, ctor ).get();
				AbstractTripleStore triples
						= AbstractTripleStore.class.cast( journal.getResourceLocator().
								locate( "kb", ITx.UNISOLATED ) );

				IIndexManager indexmgr = triples.getIndexManager();

				Map<String, String> opts = new HashMap<>();
				for ( String key : dbprops.stringPropertyNames() ) {
					opts.put( key, dbprops.getProperty( key ) );
				}
				opts.put( BigdataSail.Options.READ_ONLY, Boolean.toString( true ) );

				// find an open port
				int port = 0;
				for ( int i = 1024; i < 65536; i++ ) {
					ServerSocket ss = null;
					try {
						ss = new ServerSocket( i );
						port = i;
						break;
					}
					catch ( Exception e ) {
						// don't care; just go to the next port
					}
					finally {
						if ( null != ss ) {
							ss.close();
						}
					}
				}

				EmbeddedServerRunnable run
						= new EmbeddedServerRunnable( port, indexmgr, opts );
				new Thread( run ).start();
			}
			catch ( InterruptedException | ExecutionException | IOException ioe ) {
				log.error( ioe );
			}
		}
		catch ( Exception x ) {

		}
	}

	private static List<File> getFileList( CommandLine cmd, String option )
			throws FileNotFoundException {
		List<File> loads = new ArrayList<>();
		if ( !cmd.hasOption( option ) ) {
			return loads;
		}

		String[] loadArgs = cmd.getOptionValues( option );

		for ( String load : loadArgs ) {
			File loadFile = new File( load );
			if ( !loadFile.exists() ) {
				throw new FileNotFoundException( "Could not find: " + load );
			}
			loads.add( loadFile );
		}

		return loads;
	}

	private void load( IEngine engine, Collection<File> files )
			throws IOException, ImportValidationException, RepositoryException {

		EngineLoader el = new EngineLoader( stageInMemory );
		try {
			el.loadToEngine( files, engine, createMetamodel, errors );
		}
		finally {
			el.release();
		}
	}

	private void setLoadingOptions( CommandLine cmd ) {
		// we need parameters for the following:
		stageInMemory = !cmd.hasOption( "stage-on-disk" );
		closure = cmd.hasOption( "closure" ); // calculate inferences
		conformance = cmd.hasOption( "conformance" ); // perform conformance tests
		createMetamodel = !cmd.hasOption( "no-metamodel" ); // create metamodel
		errors = ( conformance ? new ImportData() : null );
	}

	public static class ShowHelpException extends Exception {
	}

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
			Server server = null;

			try {
				log.info( "starting jetty server on port: " + port + "..." );
				server = NanoSparqlServer.newInstance( port,
						mgr, opts );
				server.setStopAtShutdown( true );

				NanoSparqlServer.awaitServerStart( server );
				// Block and wait. The NSS is running.
				log.debug( "jetty server started at http://localhost:" + port + "/bigdata" );
				server.join();
			}
			catch ( Throwable t ) {
				log.error( t );
			}
			finally {
				if ( server != null ) {
					try {
						server.stop();
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
