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

import gov.va.semoss.model.vocabulary.VAS;
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
import gov.va.semoss.rdf.engine.util.EngineCreateBuilder;
import gov.va.semoss.ui.components.ImportDataProcessor;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineUtil;

import java.util.ArrayList;
import java.util.Collection;

import java.util.Properties;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.impl.URIImpl;
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
			}
		}

		Options options = new Options();
		Option help = new Option( "help", "Print this message" );

		Option load = OptionBuilder.withArgName( "file.[xlsx|csv|ttl|rdf|rdfs|owl|n3|jnl]+" )
				.hasArg()
				.withDescription( "Data to import." )
				.create( "load" );
		Option insights = OptionBuilder.withArgName( "insights.txt+" )
				.hasArg()
				.withDescription( "Insights to import." )
				.create( "insights" );
		Option replace = new Option( "replace", "Replace existing data?" );
		Option sparql = OptionBuilder.withArgName( "file.sparql+" )
				.hasArg()
				.withDescription( "A SPARQL update expression(s) to evaluate." )
				.create( "sparql" );
		Option update = OptionBuilder.withArgName( "old.jnl" )
				.hasArg()
				.withDescription( "Update an existing database." )
				.create( "update" );
		Option out = OptionBuilder.withArgName( "new.jnl" )
				.hasArg()
				.withDescription( "Create a new database." )
				.create( "out" );
		OptionGroup createOrUpdate = new OptionGroup();
		createOrUpdate.addOption( out );
		createOrUpdate.addOption( update );

		options.addOption( help );
		options.addOption( load );
		options.addOption( insights );
		options.addOption( replace );
		options.addOption( sparql );
		options.addOption( update );
		options.addOptionGroup( createOrUpdate );

		CommandLineParser parser = new BasicParser();

		try {
			cmd = parser.parse( options, args );
		}
		catch ( ParseException exp ) {
			System.err.println( exp );
			System.exit( -1 );
		}

		if ( cmd.hasOption( "help" ) ) {
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
			String[] loadArgs = cmd.getOptionValue( "load" ).split( "\\s+" );
			// split load by space

			for ( String load : loadArgs ) {
				File loadFile = new File( load );
				if ( !loadFile.exists() ) {
					throw new FileNotFoundException( "Cound not find: " + load );
				}
				loads.add( loadFile );
			}

		}

		// we need parameters for the following:
		boolean stageInMemory = !cmd.hasOption( "stage-on-disk" );
		boolean closure = cmd.hasOption( "closure" ); // calculate inferences
		boolean conformance = cmd.hasOption( "conformance" ); // perform conformance tests
		boolean createMetamodel = !cmd.hasOption( "no-metamodel" ); // create metamodel
		ImportData errors = ( conformance ? new ImportData() : null );

		File smss = null;

		if ( cmd.hasOption( "out" ) ) {
			String databaseFileName = cmd.getOptionValue( "out" ).replaceFirst( ".jnl", "" );
			String insightFile = null;
			String baseURI = "http://semoss.test/" + databaseFileName;
			final String workingDir = System.getProperty( "user.dir" );

			final File outputFile = new File( databaseFileName );

			// final EngineUtil eutil = EngineUtil.getInstance();
			File outputFileDir = outputFile.getParentFile();
			outputFile.delete();
			if ( null == outputFileDir ) {
				outputFileDir = new File( workingDir );
			}
			if ( !outputFileDir.exists() ) {
				throw new FileNotFoundException( "Directory does not exist: " + outputFileDir.getPath() );
			}
			
			smss = EngineUtil.createNew(
					new EngineCreateBuilder( outputFileDir, databaseFileName )
					.setDefaultBaseUri( new URIImpl( baseURI ), true )
					.setReificationModel( ReificationStyle.LEGACY )
					.setDefaultsFiles( null, null, insightFile )
					.setFiles( loads )
					.setBooleans( stageInMemory, closure, createMetamodel ),
					errors
			);
		}
		else if ( cmd.hasOption( "update" ) ) {
			String update = cmd.getOptionValue( "update" );
			boolean replace = cmd.hasOption( "replace" );

			final File updateFile = new File( update );
			if ( !updateFile.exists() ) {
				throw new FileNotFoundException( "Cound not find: " + update );
			}

			if ( null == smss ) {
				throw new FileNotFoundException( "No journal found" );
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
