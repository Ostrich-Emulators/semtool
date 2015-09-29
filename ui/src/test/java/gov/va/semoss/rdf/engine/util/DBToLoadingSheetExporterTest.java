/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.util.UriBuilder;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class DBToLoadingSheetExporterTest {

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final URI HUMAN = new URIImpl( "http://semoss.org/ontologies/Human_Being" );
	private static final URI CAR = new URIImpl( "http://semoss.org/ontologies/Car" );
	private static final URI YUGO
			= new URIImpl( "http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/Yugo" );
	private static final URI YURI
			= new URIImpl( "http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/Yuri" );
	private static InMemorySesameEngine engine;

	public DBToLoadingSheetExporterTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		engine = new InMemorySesameEngine();
		engine.setBuilders( UriBuilder.getBuilder( "http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/" ),
				UriBuilder.getBuilder( "http://semoss.org/ontologies/" ) );
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
		assertEquals( 1, result.getSheet( "Car" ).rows() );
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
