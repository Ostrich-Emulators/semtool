/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.util.EngineUtil.DbCloneMetadata;
import gov.va.semoss.util.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class EngineUtilTest {

	private static final Logger log = Logger.getLogger( EngineUtilTest.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private File dbfile;
	private IEngine eng;

	public EngineUtilTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		if ( null != dbfile ) {
			FileUtils.deleteQuietly( dbfile );
		}

		try {
			dbfile = File.createTempFile( "semoss-test-", ".jnl" );
			Files.copy( new File( "src/test/resources/test.jnl" ).toPath(),
					dbfile.toPath(), StandardCopyOption.REPLACE_EXISTING );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		Properties props = BigDataEngine.generateProperties( dbfile );
		props.setProperty( Constants.SEMOSS_URI, "http://junkowl/testfile/one" );
		props.setProperty( Constants.ENGINE_NAME, "Empty KB" );
		eng = new BigDataEngine( props );
	}

	@After
	public void tearDown() {
		eng.closeDB();
		FileUtils.deleteQuietly( dbfile );
	}

	@Test
	public void createNew() throws IOException, EngineManagementException {
		File topdir = File.createTempFile( "eutest-", "" );
		topdir.delete();
		topdir.mkdirs();
		File smss = null;

		try {
			EngineCreateBuilder ecb = new EngineCreateBuilder( topdir, "testdb" )
					.setDefaultBaseUri( new URIImpl( "http://va.gov/ontologies" ), true )
					.setReificationModel( ReificationStyle.LEGACY )
					.setFiles( Arrays.asList( LEGACY ) )
					.setBooleans( true, true, true );
			smss = EngineUtil.createNew( ecb, null );
			assertTrue( smss.exists() );
		}
		finally {
			FileUtils.deleteQuietly( topdir );
			FileUtils.deleteQuietly( smss );
		}
	}

	@Test
	public void createNew2() throws IOException, EngineManagementException {
		File topdir = File.createTempFile( "eutest-", "" );
		topdir.delete();
		topdir.mkdirs();
		File smss = null;

		try {
			EngineCreateBuilder ecb = new EngineCreateBuilder( topdir, "testdb" )
					.setDefaultBaseUri( new URIImpl( "http://va.gov/ontologies" ), true )
					.setReificationModel( ReificationStyle.LEGACY )
					.setDefaultsFiles( new File( "src/java/resources/defaultdb/Default.properties" ),
							null, new File( "src/java/resources/defaultdb/Default_Questions.properties" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.setBooleans( true, true, true );
			smss = EngineUtil.createNew( ecb, null );
			assertTrue( smss.exists() );
		}
		finally {
			FileUtils.deleteQuietly( topdir );
			FileUtils.deleteQuietly( smss );
		}
	}

	@Test
	public void testClone1() throws Exception {
		File clonedir = File.createTempFile( "eutest-clone1", "" );
		clonedir.delete();

		try {
			EngineUtil.getInstance().clone( eng, new DbCloneMetadata( clonedir,
					"testdb", "Test Database", true, true ), false );
			assertTrue( clonedir.exists() );
		}
		finally {
			FileUtils.deleteQuietly( clonedir );
		}
	}

	// @Test 
	// RPB: this function needs GUI elements, so even if
	// it works on a dev machine, it'll fail on the server
	public void testClone2() throws Exception {
		File clonedir = File.createTempFile( "eutest-clone2", "" );
		clonedir.delete();

		try {
			EngineUtil.getInstance().clone( eng.getProperty( Constants.SMSS_LOCATION ) );
			assertTrue( clonedir.exists() );
		}
		finally {
			FileUtils.deleteQuietly( clonedir );
		}
	}
}
