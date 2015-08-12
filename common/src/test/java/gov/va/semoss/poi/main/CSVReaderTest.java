/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author ryan
 */
public class CSVReaderTest {

	private static final Logger log = Logger.getLogger( CSVReaderTest.class );
	private static final File LOADER = new File( "src/test/resources/airplanes.txt" );
	private static final File DATA = new File( "src/test/resources/airplanes.csv" );

	public CSVReaderTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testImportWithControl() throws Exception {
		CSVReader rdr = new CSVReader( LOADER );
		ImportData id = rdr.readOneFile( DATA );
		assertTrue( !id.isEmpty() );
	}

	@Test( expected = IOException.class )
	public void testImportWithoutControl() throws Exception {
		CSVReader rdr = new CSVReader();

		ImportData id = rdr.readOneFile( DATA );
		// we should never get here (missing propfile exception should be thrown first)
		assertTrue( id.isEmpty() );
	}
}
