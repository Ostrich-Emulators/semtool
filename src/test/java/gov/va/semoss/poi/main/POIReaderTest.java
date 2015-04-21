/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.ImportValidationException.ErrorType;
import java.io.File;
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
	private static final File FAIL4 = new File( "src/test/resources/loaderfail4.xlsx" );
	private static final File FAIL5 = new File( "src/test/resources/loaderfail5.xlsx" );
	private static final File FAIL6 = new File( "src/test/resources/loaderfail6.xlsx" );
	private static final File FAIL7 = new File( "src/test/resources/loaderfail7.xlsx" );
	private static final File FAIL8 = new File( "src/test/resources/loaderfail8.xlsx" );
	private static final File FAIL9 = new File( "src/test/resources/loaderfail9.xlsx" );

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

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet1() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL1 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet2() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL2 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet3() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL3 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet4() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL4 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.TOO_MUCH_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test
	public void testFailLoadingSheet5() throws Exception {
		POIReader rdr = new POIReader();
		ImportData data = rdr.readOneFile( FAIL5 );
		assertEquals( 2, data.getSheets().size() );
	}

	@Test
	public void testFailLoadingSheet6() throws Exception {
		POIReader rdr = new POIReader();
		ImportData data = rdr.readOneFile( FAIL6 );
		assertTrue( data.isEmpty() );
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet7() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL7 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet8() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL8 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.INVALID_TYPE == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet9() throws Exception {
		POIReader rdr = new POIReader();
		try {
			rdr.readOneFile( FAIL9 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.WRONG_TABTYPE == e.error ) {
				throw e;
			}
		}
	}
}
