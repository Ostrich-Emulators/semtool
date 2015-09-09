/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
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
		eng = new BigDataEngine(props);
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

		try {
			EngineCreateBuilder ecb = new EngineCreateBuilder( topdir, "testdb")
					.setDefaultBaseUri( new URIImpl( "http://va.gov/ontologies" ), true )
					.setReificationModel( ReificationStyle.LEGACY )
					.setFiles( Arrays.asList( LEGACY ) )
					.setBooleans( true, true, true );
			EngineUtil.createNew( ecb, null );
		}
		finally {
			FileUtils.deleteQuietly( topdir );
		}
	}
}
