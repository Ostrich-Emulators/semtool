/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DIHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class EngineUtilTest {

	private static final Logger log = Logger.getLogger( EngineUtilTest.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private File destination;
	private IEngine eng;

	public EngineUtilTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	private void extractKb() {
		if ( null != destination ) {
			FileUtils.deleteQuietly( destination );
		}
		destination = null;

		try ( ZipInputStream zis = new ZipInputStream( new FileInputStream(
				"src/test/resources/emptydb.zip" ) ) ) {
			destination = File.createTempFile( "semoss-test-", "" );
			destination.delete();
			destination.mkdir();

			ZipEntry entry;
			while ( null != ( entry = zis.getNextEntry() ) ) {
				File outfile = new File( destination, entry.getName() );
				if ( entry.isDirectory() ) {
					outfile.mkdirs();
				}
				else {
					try ( FileOutputStream fout = new FileOutputStream( outfile ) ) {
						byte bytes[] = new byte[1024 * 1024];
						int read = -1;
						while ( -1 != ( read = zis.read( bytes ) ) ) {
							fout.write( bytes, 0, read );
						}
						zis.closeEntry();
					}
				}
			}

		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Before
	public void setUp() {
		extractKb();

		File dbdir = new File( destination, "emptydb" );
		File smss = new File( dbdir, "emptydb.jnl" );
		Properties props = BigDataEngine.generateProperties( smss );
		props.setProperty( Constants.SEMOSS_URI, "http://junkowl/testfile/one" );
		props.setProperty( Constants.ENGINE_NAME, "Empty KB" );
		props.setProperty( Constants.SMSS_LOCATION, smss.getAbsolutePath() );
		eng = new BigDataEngine();
		eng.openDB( props );
	}

	@After
	public void tearDown() {
		eng.closeDB();
		FileUtils.deleteQuietly( destination );
	}

	@Test
	public void createNew() throws IOException, EngineManagementException {
		File topdir = File.createTempFile( "eutest-", "" );
		topdir.delete();
		topdir.mkdirs();

		try {
			EngineUtil.createNew( topdir, "testdb", "http://va.gov/ontologies",
					null, null, null, Arrays.asList( LEGACY ), true, true, true, null );
		}
		finally {
			FileUtils.deleteQuietly( topdir );
		}
	}

}
