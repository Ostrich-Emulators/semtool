/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class POIReaderTest {

	private static final Logger log = Logger.getLogger( POIReaderTest.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private static final File LEGACY_EXP = new File( "src/test/resources/legacy.nt" );
	private static final File CUSTOM = new File( "src/test/resources/custom.xlsx" );
	private static final File CUSTOM_EXP = new File( "src/test/resources/custom.nt" );
	private static final URI BASEURI = new URIImpl( "http://junk.com/testfiles" );
	private static final URI OWLSTART = new URIImpl( "http://owl.junk.com/testfiles" );
	private static final URI DATAURI = new URIImpl( "http://seman.tc/data/northwind/" );
	private static final URI SCHEMAURI = new URIImpl( "http://seman.tc/models/northwind#" );

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
}
