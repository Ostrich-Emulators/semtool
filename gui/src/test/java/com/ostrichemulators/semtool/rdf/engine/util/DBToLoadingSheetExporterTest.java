/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class DBToLoadingSheetExporterTest {

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static InMemorySesameEngine engine;

	public DBToLoadingSheetExporterTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		Properties props = new Properties();
		props.setProperty( InMemorySesameEngine.INFER, Boolean.TRUE.toString() );
		engine = InMemorySesameEngine.open( props );
		engine.setBuilders( UriBuilder.getBuilder( "http://os-em.com/semtool/database/Ke42d9335-1c26-475a-96bd-9bde6a2ab5e5/" ),
				UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool/" ) );
		engine.getRawConnection().begin();
		engine.getRawConnection().add( LOADFILE, null, RDFFormat.NTRIPLES );
		engine.getRawConnection().commit();
	}

	@AfterClass
	public static void tearDownClass() {
		engine.closeDB();
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testRunExport() {
		DBToLoadingSheetExporter instance = new DBToLoadingSheetExporter( engine );
		ImportData result = instance.runExport( true, true );
		assertEquals( Arrays.asList( "Car", "Human Being", "Human Being-Purchased-Car" ),
				result.getSheetNames() );
		assertEquals( 2, result.getSheet( "Car" ).rows() );
	}

	@Test
	public void testGetDefaultExportFile() {
		File exploc = new File( "test" );
		File result = DBToLoadingSheetExporter.getDefaultExportFile( exploc, "fragment", true );
		assertEquals( "test", result.getName() );
	}

	@Test
	public void testGetDefaultExportFile2() throws Exception {
		File exploc = File.createTempFile( "test", "test" );
		exploc.delete();
		exploc.mkdirs();
		File result = DBToLoadingSheetExporter.getDefaultExportFile( exploc, "fragment", true );
		assertTrue( result.getName().startsWith( "All_fragment_LoadingSheet_" ) );
	}
}
