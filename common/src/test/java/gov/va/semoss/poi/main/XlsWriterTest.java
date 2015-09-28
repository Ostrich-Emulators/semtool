/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.poi.main.XlsWriter.SheetRowCol;
import gov.va.semoss.util.Utility;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class XlsWriterTest {

	public XlsWriterTest() {
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
	public void testSetTabRowLimit() {
	}

	@Test
	public void testGetCurrentWb() {
	}

	@Test
	public void testGetCurrentSheet() {
	}

	@Test
	public void testGetCurrentRow() {
	}

	@Test
	public void testWrite_ImportData_File() throws Exception {
		ImportMetadata im = new ImportMetadata();
		URI base = new URIImpl( "http://va.gov/importer" );
		im.setBase( base );
		im.setSchemaBuilder( base.stringValue() );
		im.setDataBuilder( base.stringValue() );
		im.add( "<http://va.gov/ryan>", "<" + RDFS.LABEL.stringValue() + ">", "ryan" );
		im.addNamespaces( Utility.DEFAULTNAMESPACES );

		ImportData data = new ImportData( im );

		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperties( Arrays.asList( "xx", "yy" ) );
		Map<String, Value> props = new HashMap<>();
		props.put( "xx", new LiteralImpl( "test" ) );
		LoadingNodeAndPropertyValues nap = lsd.add( "instance", props );
		nap.setSubjectIsError( true );
		data.add( lsd );

		LoadingSheetData rsd = LoadingSheetData.relsheet( "sbjx", "objx", "relname" );
		rsd.addProperties( Arrays.asList( "xxx", "yyy" ) );
		Map<String, Value> propsx = new HashMap<>();
		propsx.put( "xxx", new LiteralImpl( "1.0", XMLSchema.DOUBLE ) );
		LoadingNodeAndPropertyValues rel = rsd.add( "relsbj", "relobj", propsx );
		rel.setObjectIsError( true );
		data.add( rsd );

		File output = File.createTempFile( "test", ".xlsx" );
		try {
			XlsWriter writer = new XlsWriter();
			writer.write( data, output );

			POIReader rdr = new POIReader();
			rdr.keepLoadInMemory( true );
			ImportData imp = rdr.readOneFile( output );
			assertEquals( base, imp.getMetadata().getSchemaBuilder().toUri() );
			assertEquals( 2, imp.getSheetNames().size() );
		}
		finally {
			FileUtils.deleteQuietly( output );
		}
	}

	@Test
	public void testWrite_ImportData_File2() throws Exception {
		ImportData data = new ImportData();

		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperties( Arrays.asList( "xx", "yy" ) );
		Map<String, Value> props = new HashMap<>();
		props.put( "xx", new LiteralImpl( "test" ) );
		LoadingNodeAndPropertyValues nap = lsd.add( "instance", props );
		nap.setSubjectIsError( true );
		data.add( lsd );

		LoadingSheetData rsd = LoadingSheetData.relsheet( "sbjx", "objx", "relname" );
		rsd.addProperties( Arrays.asList( "xxx", "yyy" ) );
		Map<String, Value> propsx = new HashMap<>();
		propsx.put( "xxx", new LiteralImpl( "1.0", XMLSchema.DOUBLE ) );
		LoadingNodeAndPropertyValues rel = rsd.add( "relsbj", "relobj", propsx );
		rel.setObjectIsError( true );
		data.add( rsd );

		File output = File.createTempFile( "test", ".xlsx" );
		try {
			XlsWriter writer = new XlsWriter();
			writer.write( data, output );

			POIReader rdr = new POIReader();
			rdr.keepLoadInMemory( true );
			ImportData imp = rdr.readOneFile( output );
			assertEquals( 2, imp.getSheetNames().size() );
		}
		finally {
			FileUtils.deleteQuietly( output );
		}
	}

	@Test
	public void testWrite_ImportData_OutputStream() throws Exception {
	}

	@Test
	public void testNextRowIsFirstRowOfTab() {
	}

	@Test
	public void testCreateWorkbook() {
	}

	@Test
	public void testCreateTab_String() {
	}

	@Test
	public void testCreateTab_String_StringArr() {
	}

	@Test
	public void testAddRow_ObjectArr() throws IOException {
		XlsWriter writer = new XlsWriter();
		writer.createTab( "test" );
		writer.addRow( new Object[]{ "one row" }, new CellStyle[]{} );
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		writer.write( aos );

		// we get slightly different sizes per builds
		assertTrue( aos.size() > 3270 && aos.size() < 3285 );
	}

	@Test
	public void testAddRow_ObjectArr_CellStyleArr() {
	}

	@Test
	public void testWrite_File() throws Exception {
	}

	@Test
	public void testWrite_OutputStream() throws Exception {

	}

	@Test
	public void testWriteLoadingSheet() {
	}

	@Test
	public void testGenerateSheetName() {
		String longname = "abcdefghijklmnopqrstuvwxyz012---3456789";
		Set<String> names = new HashSet<>();
		names.add( longname );
		names.add( "abcdefghijklmnopqrstuvwxyz012" );
		names.add( "abcdefghijklmnopqrstuvwxyz01210" );
		assertEquals( "abcdefghijklmnopqrstuvwxyz01211",
				XlsWriter.generateSheetName( longname, names ) );
	}

	@Test
	public void testGenerateSheetName2() {
		Set<String> names = new HashSet<>();
		names.add( "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOP" );
		names.add( "abcdefghijklmnopqrstuvwxyzABCDE" );
		assertEquals( "abcdefghijklmnopqrstuvwxyzABC10",
				XlsWriter.generateSheetName( "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOP",
						names ) );
		assertEquals( "test", XlsWriter.generateSheetName( "test", names ) );
	}

	@Test
	public void testSheetRowCol1() {
		SheetRowCol src1 = new SheetRowCol( "x", 1, 1 );
		SheetRowCol src2 = new SheetRowCol( "x", 1, 2 );
		assertNotEquals( src1, src2 );
	}

	@Test
	public void testSheetRowCol2() {
		SheetRowCol src1 = new SheetRowCol( "x", 1, 1 );
		SheetRowCol src2 = new SheetRowCol( "x", 1, 1 );
		assertEquals( src1, src2 );
	}

	@Test
	public void testSheetRowCol3() {
		Set<SheetRowCol> set = new HashSet<>();
		SheetRowCol src1 = new SheetRowCol( "x", 1, 1 );
		set.add( src1 );
		assertTrue( set.contains( src1 ) );
	}

	@Test
	public void testSheetRowCol4() {
		SheetRowCol src1 = new SheetRowCol( "x", 1, 1 );
		assertEquals( "x (1,1)", src1.toString() );
	}
}
