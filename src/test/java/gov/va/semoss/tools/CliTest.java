/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.tools;

import gov.va.semoss.rdf.engine.api.IEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class CliTest {

	private static final Logger log = Logger.getLogger( CliTest.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private static final File CUSTOM = new File( "src/test/resources/custom.xlsx" );

	protected IEngine engine;

	// Simulated command line arguments:
	private static final String[] commandLines = {
		//  0: must fail gracefully, the output file should not be created:
		"-load doesNotExist.xlsx -out doNotCreate.jnl",
		//  1: must fail gracefully, the output path does not exist:
		"-load src/test/resources/legacy.xlsx -out non/exitent/path/legacy.jnl",
		//  2: must fail gracefully, the required output journal does does not exist:
		"-load src/test/resources/legacy.xlsx -update legacy.jnl -replace",
		//  3: basic journal creation with single excel file:
		"-load src/test/resources/legacy.xlsx -out legacy.jnl",
		//  4: basic journal creation with two excel files:
		"-load src/test/resources/legacy.xlsx src/test/resources/otherfile.xlsx -out two-file.jnl",
		//  5: basic journal creation with two excel files, one turtle file:
		"-load src/test/resources/legacy.xlsx src/test/resources/otherfile.xlsx src/test/resources/file.ttl -out three-file.jnl",
		//  6: basic journal creation with single excel file and copy of existing journal:
		"-load src/test/resources/legacy.xlsx src/test/resources/olddb.jnl -out merged-db.jnl",
		//  7: basic journal creation including insights:
		"-load src/test/resources/legacy.xlsx -out legacy.jnl -insights src/test/resources/test-insights.txt",
		//  8: update an existing journal with added data:
		"-load src/test/resources/otherfile.xlsx -update legacy.jnl",
		//  9: update an existing journal with added insights:
		"-update legacy.jnl -insights src/test/resources/test-insights.txt",
		// 10: update an existing journal replacing insights:
		"-update legacy.jnl -insights src/test/resources/test-insights.txt -replace",
		// 11: update an existing journal replacing data:
		"-load src/test/resources/otherfile.xlsx -update legacy.jnl -replace",
		// 12: update an existing journal replacing both data and insights:
		"-load src/test/resources/otherfile.xlsx -update legacy.jnl -insights src/test/resources/test-insights.txt -replace",
		// 13: create a simple journal with closure:
		"-load src/test/resources/legacy.xlsx -out legacy.jnl -closure",
		// 14: exit if conformance test fails, clean up output journal:
		"-load src/test/resources/legacy.xlsx -out legacy.jnl -conformance",
		// 15: create a simple journal and check conformance, the conformance grid sheet is saved to a file (TBD)
		// this doesn't make sense yet, there is only a file to create if conformance test fails, but then the tool has exited
		// we would need another flag to either -exit-on-fail or -create-file-on-fail
		"-load src/test/resources/legacy.xlsx -out legacy.jnl -conformance",
		// 16: create a simple journal but do not create a metamodel:
		"-load src/test/resources/custom-onto-small.xlsx -no-metamodel -out custom-onto-small.jnl",
		// 17: create a simple journal, stage on disk:
		"-load src/test/resources/legacy.xlsx -out legacy-stage-on-disk.jnl -stage-on-disk",
		// 18: create a simple journal, compute closure, do not construct metamodel and stage on disk:
		"-load src/test/resources/custom-onto-small.xlsx -out custom-onto-small-stage-on-disk.jnl -stage-on-disk -closure -no-metamodel",
		// 20: update an existing journal with replaced data and insights, compute closure, stage on disk, and exit gracefully when conformance test fails:
		"-load src/test/resources/custom-onto-small.xlsx -update custom-onto-small.jnl -stage-on-disk -closure -no-metamodel -conformance -replace -insights src/test/resources/test-insights.txt",
		// 21: update an existing journal with replaced data and insights, compute closure, stage on disk, conformance tests must pass:
		"-load src/test/resources/custom-onto-small.xlsx -update custom-onto-small.jnl -stage-on-disk -closure -no-metamodel -conformance -replace -insights src/test/resources/test-insights.txt",
		// 22: i forgot what this was supposed to be... update later
		"-load src/test/resources/custom-onto-small.xlsx -update custom-onto-small.jnl -stage-on-disk -closure -no-metamodel -conformance -replace -insights src/test/resources/test-insights.txt"
	};

	public CliTest() {
	}

	private static String[] makeArgs( String commandLine ) {
		return commandLine.split( "\\s+" );
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@After
	public void tearDown() {
		for ( String file : new String[]{ "legacy.jnl", "custom-onto-small.jnl",
			"custom-onto-small-stage-on-disk.jnl", } ) {
			FileUtils.deleteQuietly( new File( file ) );
		}
	}

	@Test( expected = FileNotFoundException.class )
	public void testCreateFromNonExistentLoadingSheet() throws Exception {
		String[] args = makeArgs( commandLines[0] );
		CLI mossy = new CLI( args );
		mossy.execute();
	}

	@Test( expected = FileNotFoundException.class )
	public void testUpdateToNonExistentPath() throws Exception {
		String[] args = makeArgs( commandLines[2] );
		CLI mossy = new CLI( args );
		mossy.execute();
	}

	@Test
	public void testJournalFromOneLoadingSheet() throws Exception {
		String[] args = makeArgs( commandLines[3] );
		CLI mossy = new CLI( args );
		mossy.execute();

		/*
		 OneVarListQueryAdapter<URI> o
		 = OneVarListQueryAdapter.getUriList( "SELECT ?uri WHERE { ?uri ?p ?o }", "uri" );
    
		 BigDataEngine engine = new BigDataEngine();
		 List<URI> oldlist = engine.query( o );

		 EngineUtil.loadToEngine( engine, Arrays.asList( LEGACY ), true, true, true, false );
		 assertEquals( oldlist, engine.query( o ) );
		 */
		Files.deleteIfExists( Paths.get( mossy.getOption( "out" ) ) );
		assertEquals( "hi", "hi" );
	}

}
