/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

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
		ImportData data = new ImportData();
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperties( Arrays.asList( "xx", "yy" ) );
		Map<String, Value> props = new HashMap<>();
		props.put( "xx", new LiteralImpl( "test" ) );
		LoadingNodeAndPropertyValues nap = lsd.add( "instance", props );
		nap.setSubjectIsError( true );
		data.add( lsd );

		LoadingSheetData rsd = LoadingSheetData.relsheet( "sbjx", "objx", "relname" );
		lsd.addProperties( Arrays.asList( "xxx", "yyy" ) );
		Map<String, Value> propsx = new HashMap<>();
		props.put( "xxx", new LiteralImpl( "test" ) );
		LoadingNodeAndPropertyValues rel = lsd.add( "relsbj", "relobj", propsx );
		nap.setSubjectIsError( true );
		data.add( rsd );

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XlsWriter writer = new XlsWriter();
		writer.write( data, baos );

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
	public void testAddRow_StringArr() {
	}

	@Test
	public void testAddRow_StringArr_CellStyleArr() {
	}

	@Test
	public void testAddRow_ObjectArr() {
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

}
