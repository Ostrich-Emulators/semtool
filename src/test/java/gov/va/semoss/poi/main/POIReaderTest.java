/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.FileLoadingException.ErrorType;
import java.io.File;
import static jdk.nashorn.internal.runtime.Debug.id;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author ryan
 */
public class POIReaderTest {

	private static final Logger log = Logger.getLogger( POIReaderTest.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private static final File CUSTOM = new File( "src/test/resources/custom.xlsx" );
	private static final File FAIL1 = new File( "src/test/resources/loaderfail1.xlsx" );
	private static final File FAIL2 = new File( "src/test/resources/loaderfail2.xlsx" );
	private static final File FAIL3 = new File( "src/test/resources/loaderfail3.xlsx" );

	public POIReaderTest() {
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
	public void testImportLegacy() throws Exception {
		POIReader rdr = new POIReader();
		ImportData id = rdr.readOneFile( LEGACY );
		assertTrue( !id.isEmpty() );
	}

	@Test
	public void testImportModern() throws Exception {
		POIReader rdr = new POIReader();
		ImportData id = rdr.readOneFile( CUSTOM );
		assertTrue( !id.isEmpty() );
	}

	@Test( expected = FileLoadingException.class )
	public void testFailLoadingSheet1() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL1 );
		}
		catch ( FileLoadingException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = FileLoadingException.class )
	public void testFailLoadingSheet2() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL2 );
		}
		catch ( FileLoadingException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = FileLoadingException.class )
	public void testFailLoadingSheet3() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL3 );
		}
		catch ( FileLoadingException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}
}
