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
package gov.va.semoss.util;

import gov.va.semoss.poi.main.ImportValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.edgemodelers.AbstractEdgeModeler;
import gov.va.semoss.rdf.engine.util.EngineCreateBuilder;
import gov.va.semoss.ui.components.ImportDataProcessor;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;

import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class CLI {

	private CommandLine cmd = null;

	final Logger logger = Logger.getLogger( CLI.class );

	public CLI( String[] args ) {

		// First read in system properties.
		// Minimally we need the SEMOSS_URI property set.
		// For now we set everything, simply to be cautious:
		//
		final String workingDir = System.getProperty( "user.dir" );
		final File propFile = new File( workingDir, "RDF_Map.prop" );

		Properties props = DIHelper.getInstance().getCoreProp();
		try {
			props.load( CLI.class.getResourceAsStream( "/semoss.properties" ) );
		}
		catch ( IOException ioe ) {
			logger.error( ioe, ioe );
		}
		if ( propFile.exists() ) {
			try ( Reader r = new FileReader( propFile ) ) {
				logger.debug( "reading properties from " + propFile );
				Properties fsprops = new Properties();
				fsprops.load( r );
				Utility.mergeProperties( props, fsprops, false, null );
			}
			catch ( IOException ioe ) {
				logger.error( ioe, ioe );
				System.exit( 1 );
			}
		}

		Options options = new Options();
		Option help = new Option( "help", "Print this message" );

		OptionBuilder.withArgName( "file.[xlsx|csv|ttl|rdf|rdfs|owl|n3|jnl]+" );
		OptionBuilder.hasArgs( 10 );
		OptionBuilder.withDescription( "Data to import." );
		Option load = OptionBuilder.create( "load" );

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

		Option replace = new Option( "replace", "Replace existing data?" );

		OptionBuilder.withArgName( "file.sparql+" );
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
		Option create = OptionBuilder.create( "out" );

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

		OptionGroup createOrUpdate = new OptionGroup();
		createOrUpdate.addOption( create );
		createOrUpdate.addOption( update );

		options.addOption( help );
		options.addOption( load );
		options.addOption( insights );
		options.addOption( replace );
		options.addOption( sparql );
		options.addOption( update );
		options.addOption( baseuri );
		options.addOption( creator );
		options.addOption( desc );
		options.addOption( label );
		options.addOption( publisher );
		options.addOption( vocab );
		options.addOptionGroup( createOrUpdate );

		PosixParser parser = new PosixParser();

		try {
			cmd = parser.parse( options, args );
		}
		catch ( ParseException exp ) {
			System.err.println( exp );
			System.exit( -1 );
		}

		if ( cmd.hasOption( "help" ) || 0 == cmd.getOptions().length ) {
			// automatically generate the help statement
			String header = "Do something useful with an input file\n\n";
			String footer = "\nPlease report issues at http://example.com/issues";
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth( 140 );
			formatter.printHelp( "mossy", header, options, footer, true );
			System.exit( 0 );
		}
	}

	public String getOption( String option ) {
		return cmd.getOptionValue( option );
	}

	public Option[] getOptions() {
		return cmd.getOptions();
	}

	public CommandLine getCommandLine() {
		return cmd;
	}

	public void execute() throws IOException, EngineManagementException {
		// get a DB handle:
		// Engine engine = null;

		Collection<File> loads = new ArrayList<>();
		if ( cmd.hasOption( "load" ) ) {
			String[] loadArgs = cmd.getOptionValues( "load" );
			// split load by space

			for ( String load : loadArgs ) {
				File loadFile = new File( load );
				if ( !loadFile.exists() ) {
					throw new FileNotFoundException( "Could not find: " + load );
				}
				loads.add( loadFile );
			}
		}

		Collection<URL> vocabs = new ArrayList<>();
		if ( cmd.hasOption( "vocab" ) ) {
			String[] vos = cmd.getOptionValues( "vocab" );
			// split load by space

			for ( String load : vos ) {
				File loadFile = new File( load );
				if ( !loadFile.exists() ) {
					throw new FileNotFoundException( "Could not find: " + load );
				}
				vocabs.add( loadFile.toURI().toURL() );
			}
		}

		// we need parameters for the following:
		boolean stageInMemory = !cmd.hasOption( "stage-on-disk" );
		boolean closure = cmd.hasOption( "closure" ); // calculate inferences
		boolean conformance = cmd.hasOption( "conformance" ); // perform conformance tests
		boolean createMetamodel = !cmd.hasOption( "no-metamodel" ); // create metamodel
		ImportData errors = ( conformance ? new ImportData() : null );

		File smss = null;

		String baseURI = ( cmd.hasOption( "baseuri" )
				? cmd.getOptionValue( "baseuri" ) : "http://semoss.test/database" );

		if ( cmd.hasOption( "out" ) ) {
			File db = new File( cmd.getOptionValue( "out" ) ).getAbsoluteFile();
			File dbdir = db.getParentFile();
			if ( !dbdir.exists() ) {
				if ( !dbdir.mkdirs() ) {
					throw new FileNotFoundException( "Could not create output directory/file" );
				}
			}

			EngineCreateBuilder ecb = new EngineCreateBuilder( dbdir,
					FilenameUtils.getBaseName( db.getName() ) )
					.setDefaultBaseUri( new URIImpl( baseURI ), false )
					.setReificationModel( ReificationStyle.SEMOSS )
					.setFiles( loads )
					.setVocabularies( vocabs )
					.setBooleans( stageInMemory, closure, createMetamodel );

			if ( cmd.hasOption( "insights" ) ) {
				ecb.setDefaultsFiles( null, null, cmd.getOptionValue( "insights" ) );
			}

			smss = EngineUtil.createNew( ecb, errors );

			// set the metadata on the just-created database
			final Map<URI, String> metadatas = getMetadata( cmd );

			if ( !metadatas.isEmpty() ) {
				// set the metadata on the just-created database
				IEngine engine = Utility.loadEngine( smss );
				try {
					engine.execute( new ModificationExecutorAdapter() {

						@Override
						public void exec( RepositoryConnection conn ) throws RepositoryException {
							ValueFactory vf = conn.getValueFactory();
							for ( Map.Entry<URI, String> en : metadatas.entrySet() ) {
								Value val = AbstractEdgeModeler.getRDFStringValue( en.getValue(),
										engine.getNamespaces(), vf );
								conn.add( engine.getBaseUri(), en.getKey(), val );
							}
						}
					} );
					
					engine.commit();
					Utility.closeEngine( engine );
				}
				catch ( Exception e ) {
					logger.error( e, e );
				}
			}
		}
		else if ( cmd.hasOption( "update" ) ) {
			String update = cmd.getOptionValue( "update" );
			boolean replace = cmd.hasOption( "replace" );

			smss = new File( update );
			if ( null == update || !smss.exists() ) {
				throw new FileNotFoundException( "Journal not found:  " + update );
			}

			IEngine engine = Utility.loadEngine( smss );
			if ( replace ) {
				ImportDataProcessor.clearEngine( engine, loads );
			}

			try {
				EngineLoader el = new EngineLoader( stageInMemory );
				el.loadToEngine( loads, engine, createMetamodel, errors );
				el.release();
				// if we get here, no exceptions have been thrown, so we're good
			}
			catch ( ImportValidationException | RepositoryException | IOException ioe ) {
				logger.error( ioe, ioe );
			}
		}

		if ( cmd.hasOption( "sparql" ) ) {
			String sparql = cmd.getOptionValue( "sparql" );
			// run an update , save updated db
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

		/*
		 if ( null != DIHelper.getInstance().getProperty( "LOG4J" ) ) {
		 File logCheckFile = new File( DIHelper.getInstance().getProperty( "LOG4J" ) );
		 if ( logCheckFile.exists() ) {
		 PropertyConfigurator.configure( logCheckFile.toString() );
		 }
		 }
		 */
		CLI mossy = new CLI( args );
		mossy.execute();

	}

}
