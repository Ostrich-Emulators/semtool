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

import gov.va.semoss.poi.main.ImportValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.impl.InsightManagerImpl;
import gov.va.semoss.rdf.engine.util.EngineCreateBuilder;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.ui.components.ImportDataProcessor;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;

import gov.va.semoss.rdf.engine.util.EngineUtil2;
import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.rdf.query.util.UpdateExecutorAdapter;
import gov.va.semoss.user.LocalUserImpl;
import static gov.va.semoss.util.RDFDatatypeTools.getRDFStringValue;
import gov.va.semoss.util.Utility;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class CLI {

	private static final Logger log = Logger.getLogger( CLI.class );
	private static final String TRIALS[] = { "create", "update", "replace",
		"export", "server", "copy", "upgrade" };

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
			case 5:
				copy( cmd );
				break;
			case 6:
				upgrade( cmd );
				break;
		}
	}

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

		OptionBuilder.withArgName( "insights.ttl" );
		OptionBuilder.hasArgs();
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

		OptionBuilder.withArgName( "old.jnl> <file.[ttl|rdf|nt]" );
		OptionBuilder.hasArgs( 2 );
		OptionBuilder.withDescription( "Export data to a file." );
		Option export = OptionBuilder.create( "export" );

		OptionBuilder.withArgName( "old.jnl> <Sesame URL" );
		OptionBuilder.hasArgs( 2 );
		OptionBuilder.withDescription( "Copy the BigData DB to a Sesame endpoint." );
		Option copy = OptionBuilder.create( "copy" );

		OptionBuilder.withArgName( "old.jnl" );
		OptionBuilder.hasArg();
		OptionBuilder.withDescription( "Start a Sparql Endpoint." );
		Option server = OptionBuilder.create( "server" );

		OptionBuilder.withArgName( "directory> <new.jnl" );
		OptionBuilder.hasArgs( 2 );
		OptionBuilder.withDescription( "Upgrade a legacy database" );
		Option upgrade = OptionBuilder.create( "upgrade" );

		OptionGroup modes = new OptionGroup();
		modes.addOption( create );
		modes.addOption( update );
		modes.addOption( replace );
		modes.addOption( server );
		modes.addOption( export );
		modes.addOption( copy );
		modes.addOption( upgrade );

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
				// the export and upgrade commands need two options
				// values, so only look at the first one
				String vals[] = cmd.getOptionValues( TRIALS[i] );
				String val = vals[0];
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
			engine = new BigDataEngine( dbfile );

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

			File smss = EngineUtil2.createNew( ecb, errors );
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

		engine = new BigDataEngine( dbfile );

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

		String exportnames[] = cmd.getOptionValues( "export" );
		File exportfile = null;
		if ( 2 == exportnames.length ) {
			exportfile = new File( exportnames[1] );
			File parentdir = exportfile.getAbsoluteFile().getParentFile();
			if ( !( parentdir.exists() || parentdir.mkdirs() ) ) {
				throw new FileNotFoundException( "Could not create export file directory" );
			}
		}

		engine = new BigDataEngine( dbfile );
		try ( BufferedWriter w = new BufferedWriter( null == exportfile
				? new OutputStreamWriter( System.out ) : new FileWriter( exportfile ) ) ) {
			RDFHandler handler = Utility.getExporterFor( null == exportfile
					? "" : exportfile.getName(), w );

			if ( cmd.hasOption( "insightsdb" ) ) {
				try {
					handler.startRDF();
					InsightManager im = engine.getInsightManager();
					for ( Statement s : InsightManagerImpl.getStatements( im, new LocalUserImpl() ) ) {
						handler.handleStatement( s );
					}
					handler.endRDF();
				}
				catch ( RDFHandlerException re ) {
					log.error( re, re );
				}
			}
			else {
				engine.execute( new ModificationExecutorAdapter() {

					@Override
					public void exec( RepositoryConnection conn ) throws RepositoryException {
						try {
							conn.export( handler );
						}
						catch ( RDFHandlerException re ) {
							log.error( re, re );
						}
					}
				} );
			}
		}
	}

	public void serve( CommandLine cmd ) throws IOException, RepositoryException {
		if ( !dbfile.exists() ) {
			throw new FileNotFoundException( dbfile.getAbsolutePath() );
		}

		try {
			new Thread( new EmbeddedServerRunnable( dbfile ) ).start();
		}
		catch ( IOException ioe ) {
			log.error( ioe );
		}
	}

	public void copy( CommandLine cmd ) throws IOException, RepositoryException {
		if ( !dbfile.exists() ) {
			throw new FileNotFoundException( dbfile.getAbsolutePath() );
		}

		String args[] = cmd.getOptionValues( "copy" );
		if ( args.length < 2 ) {
			throw new IOException( "No Sesame endpoint given" );
		}
		String url = args[1];

		Repository sesame = null;
		RepositoryConnection tmpconn = null;

		try {
			sesame = new HTTPRepository( url );
			sesame.initialize();
			tmpconn = sesame.getConnection();
			final RepositoryConnection sesameconn = tmpconn;

			engine = new BigDataEngine( dbfile );

			if ( cmd.hasOption( "insightsdb" ) ) {
				try {
					sesameconn.begin();
					InsightManager im = engine.getInsightManager();
					sesameconn.add( InsightManagerImpl.getStatements( im, new LocalUserImpl() ) );
					sesameconn.commit();
				}
				catch ( RepositoryException re ) {
					log.error( re, re );

					try {
						sesameconn.rollback();
					}
					catch ( RepositoryException re2 ) {
						log.error( re2, re2 );
					}
				}
			}
			else {
				engine.execute( new ModificationExecutorAdapter() {

					@Override
					public void exec( RepositoryConnection conn ) throws RepositoryException {
						sesameconn.begin();
						sesameconn.add( conn.getStatements( null, null, null, false ) );
						sesameconn.commit();
					}
				} );
			}
		}
		catch ( Exception x ) {
			log.fatal( x, x );
		}
		finally {
			if ( null != tmpconn ) {
				try {
					tmpconn.close();
				}
				catch ( RepositoryException re ) {
					log.warn( re, re );
				}
			}

			if ( null != sesame ) {
				try {
					sesame.shutDown();
				}
				catch ( RepositoryException re ) {
					log.warn( re, re );
				}
			}
		}
	}

	public void upgrade( CommandLine cmd ) throws IOException, RepositoryException {
		if ( !dbfile.exists() ) {
			throw new FileNotFoundException( dbfile.getAbsolutePath() );
		}

		String exportnames[] = cmd.getOptionValues( "export" );
		File exportfile = null;
		if ( 2 == exportnames.length ) {
			exportfile = new File( exportnames[1] );
			if ( exportfile.exists() ) {
				throw new IOException( "Output file already exists." );
			}

			File parentdir = exportfile.getAbsoluteFile().getParentFile();
			if ( !( parentdir.exists() || parentdir.mkdirs() ) ) {
				throw new IOException( "Could not create export file directory" );
			}
		}

		LegacyUpgrader upg = new LegacyUpgrader( dbfile );
		upg.upgradeTo( exportfile );
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
}
