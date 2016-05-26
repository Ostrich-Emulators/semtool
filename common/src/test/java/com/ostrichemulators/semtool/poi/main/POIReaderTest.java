/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import com.ostrichemulators.semtool.poi.main.ImportValidationException.ErrorType;
import java.io.File;
import java.io.IOException;
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
	private static final File BASICS = new File( "src/test/resources/basic-loading-sheet.xlsx" );
	private static final File TEST13 = new File( "src/test/resources/test13.xlsx" );
	private static final File TEST16 = new File( "src/test/resources/test16.xlsx" );

	private ImportData data = null;

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
		if ( null != data ) {
			data.release();
		}
	}

	@Test
	public void testImportLegacy() throws Exception {
		POIReader rdr = new POIReader();
		data = rdr.readOneFile( LEGACY );
		assertTrue( !data.isEmpty() );
	}

	@Test
	public void testImportModern() throws Exception {
		POIReader rdr = new POIReader();
		data = rdr.readOneFile( CUSTOM );
		assertTrue( !data.isEmpty() );
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet1() throws Exception {
		POIReader rdr = new POIReader();
		try {
			data = rdr.readOneFile( FAIL1 );
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
			data = rdr.readOneFile( FAIL2 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.TOO_MUCH_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet3() throws Exception {
		POIReader rdr = new POIReader();
		try {
			data = rdr.readOneFile( FAIL3 );
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
			data = rdr.readOneFile( FAIL4 );
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
		data = rdr.readOneFile( FAIL5 );
		assertEquals( 2, data.getSheets().size() );
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet6() throws Exception {
		POIReader rdr = new POIReader();
		try {
			data = rdr.readOneFile( FAIL6 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.NOT_A_LOADING_SHEET == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testFailLoadingSheet7() throws Exception {
		POIReader rdr = new POIReader();
		try {
			data = rdr.readOneFile( FAIL7 );
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
			data = rdr.readOneFile( FAIL8 );
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
			data = rdr.readOneFile( FAIL9 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.WRONG_TABTYPE == e.error ) {
				throw e;
			}
		}
	}

	@Test
	public void testLoadingBasics() throws Exception {
		POIReader rdr = new POIReader();
		data = rdr.readOneFile( BASICS );
		assertEquals( 2, data.getSheet( "Humans" ).rows() );
		assertEquals( "Yuri", data.getSheet( "Humans" ).iterator().next().getSubject() );
		assertEquals( 2, data.getSheet( "Purchases" ).rows() );
		assertEquals( "Yugo", data.getSheet( "Purchases" ).iterator().next().getObject() );
	}

	@Test( expected = ImportValidationException.class )
	public void testLoadingSheet13() throws Exception {
		POIReader rdr = new POIReader();
		try {
			data = rdr.readOneFile( TEST13 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testLoadingSheet16() throws Exception {
		POIReader rdr = new POIReader();
		try {
			data = rdr.readOneFile( TEST16 );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
	}

	@Test
	public void testNonLoadingSheet() throws Exception {
		data = POIReader.readNonloadingSheet( CUSTOM );

		assertEquals( 4, data.getSheetNames().size() );
	}

	@Test
	public void testMetadata() throws Exception {
		POIReader rdr = new POIReader();
		rdr.keepLoadInMemory( true );
		ImportMetadata im = rdr.getMetadata( CUSTOM );

		assertEquals( "http://purl.org/dc/terms/", im.getNamespaces().get( "dct" ) );
	}

	@Test( expected = IOException.class )
	public void testMetadata2() throws Exception {
		POIReader rdr = new POIReader();
		rdr.keepLoadInMemory( true );
		rdr.getMetadata( new File( "non-existing; file\\" ) );
	}
}
