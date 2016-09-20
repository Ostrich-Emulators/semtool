/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.vocabulary.FOAF;

/**
 *
 * @author ryan
 */
public class LowMemXlsReaderTest {

	private static final File CUSTOM = new File( "src/test/resources/custom.xlsx" );

	public LowMemXlsReaderTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testKeepSheetDataInMemory() {
	}

	@Test
	public void testRelease() {
	}

	@Test
	public void testGetSheetTypes() {
	}

	@Test
	public void testGetMetadata() {
	}

	@Test
	public void testGetData() {
	}

	@Test
	public void testGetSheetNames() throws IOException {
		LowMemXlsReader rdr = new LowMemXlsReader( CUSTOM );
		ImportMetadata data = rdr.getMetadata();


		Map<String, String> namespaces = new HashMap<>( Utility.DEFAULTNAMESPACES );
		namespaces.put( FOAF.PREFIX, FOAF.NAMESPACE );
		namespaces.put( "northwind", "http://seman.tc/models/northwind#" );
		namespaces.put( "dct", "http://purl.org/dc/terms/" );
		namespaces.put( "schema", "http://schema.org/" );
		assertEquals( namespaces, data.getNamespaces() );
	}
}
