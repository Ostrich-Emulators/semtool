/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.tools;

import gov.va.semoss.rdf.engine.api.IEngine;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class CliTest {

	private static final Logger log = Logger.getLogger( CliTest.class );
	private static final File LEGACY = new File( "../ui/src/test/resources/legacy.xlsx" );
	private static final File CUSTOM = new File( "../ui/src/test/resources/custom.xlsx" );

	protected IEngine engine;

	// Simulated command line arguments:
	private static final String[] commandLines = {
		//  0: must fail gracefully, the output file should not be created:
		"-create legacy.jnl -data doesNotExist.xlsx",
		//  1: must fail gracefully, the output path does not exist:
		"-create non/exitent/path/legacy.jnl -data ../ui/src/test/resources/legacy.xlsx",
		//  2: must fail gracefully, the required output journal does does not exist:
		"-update legacy.jnl -data src/test/resources/legacy.xlsx",
		//  3: basic journal creation with single excel file:
		"-create legacy.jnl ../ui/src/test/resources/legacy.xlsx",
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
	}

	@Test( expected = FileNotFoundException.class )
	public void testCreateFromNonExistentLoadingSheet() throws Exception {
		File f = File.createTempFile( "cli-test-", ".jnl" );
		String[] args = makeArgs( commandLines[0].replaceAll( " legacy.jnl ",
				" " + f.getAbsolutePath() + " " ) );
		CLI mossy = new CLI( args );
		try {
			mossy.execute();
		}
		finally {
			mossy.release();
			FileUtils.deleteQuietly( f );
		}
	}

	@Test( expected = FileNotFoundException.class )
	public void testUpdateToNonExistentPath() throws Exception {
		File f = File.createTempFile( "cli-test-", ".jnl" );
		String[] args = makeArgs( commandLines[2].replaceAll( " legacy.jnl ",
				" " + f.getAbsolutePath() + " " ) );
		CLI mossy = new CLI( args );
		try {
			mossy.execute();
		}
		finally {
			mossy.release();
			FileUtils.deleteQuietly( f );
		}
	}

	@Test
	public void testJournalFromOneLoadingSheet() throws Exception {
		File f = File.createTempFile( "cli-test-", ".jnl" );
		String[] args = makeArgs( commandLines[3].replaceAll( " legacy.jnl ",
				" " + f.getAbsolutePath() + " " ) );
		f.delete();
		CLI mossy = new CLI( args );
		try {
			mossy.execute();
		}
		finally {
			mossy.release();
			FileUtils.deleteQuietly( f );
		}
	}
}
