/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.BigDataEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.EngineFactory;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil.DbCloneMetadata;
import com.ostrichemulators.semtool.util.Constants;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class EngineUtilTest {

	private static final Logger log = Logger.getLogger( EngineUtilTest.class );
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
		eng = EngineFactory.getEngine( props );
	}

	@After
	public void tearDown() {
		eng.closeDB();
		FileUtils.deleteQuietly( dbfile );
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

	// @Test
	// RPB: this function needs GUI elements, so even if
	// it works on a dev machine, it'll fail on the server
	public void testRun() throws Exception {
		eng.closeDB();

		new Thread( EngineUtil.getInstance() ).start();

		try {
			EngineUtil.getInstance().addEngineOpListener( new EngineOperationAdapter() {

				@Override
				public void engineOpened( IEngine engx ) {
					assertEquals( eng.getEngineName(), engx.getEngineName() );
				}
			} );

			EngineUtil.getInstance().mount( dbfile, false );

		}
		finally {
			EngineUtil.getInstance().stop();
		}
	}
}
