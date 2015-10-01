/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.ReificationStyle;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.util.EngineUtil.DbCloneMetadata;
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.StatementAddingExecutor;
import gov.va.semoss.util.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;

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
					.setDefaultsFiles( new File( "src/main/resources/defaultdb/Default.properties" ),
							new File( "src/main/resources/models/va-semoss.ttl" ),
							new File( "src/test/resources/insights.ttl" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.addVocabulary( new File( "src/main/resources/models/va-semoss.ttl" ).toURI().toURL() )
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
	public void createNew3() throws IOException, EngineManagementException {
		File topdir = File.createTempFile( "eutest-", "" );
		topdir.delete();
		topdir.mkdirs();
		File smss = null;

		try {
			EngineCreateBuilder ecb = new EngineCreateBuilder( topdir, "testdb" )
					.setDefaultBaseUri( new URIImpl( "http://va.gov/ontologies" ), true )
					.setReificationModel( ReificationStyle.LEGACY )
					.setDefaultsFiles( new File( "src/main/resources/defaultdb/Default.properties" ),
							new File( "src/main/resources/models/va-semoss.ttl" ),
							new File( "src/test/resources/insights.ttl" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.addVocabulary( new File( "src/main/resources/models/va-semoss.ttlx" ).toURI().toURL() )
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

	@Test
	public void testClear() throws Exception {
		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( RDFS.DATATYPE, RDFS.LABEL,
				new LiteralImpl( "test label" ) ) );
		eng.execute( sae );

		ListQueryAdapter<URI> q
				= OneVarListQueryAdapter.getUriList( "SELECT ?s WHERE { ?s rdfs:label ?o }" );

		List<URI> addeduris = new ArrayList<>( eng.queryNoEx( q ) );
		q.clear();

		EngineUtil.clear( eng );
		List<URI> newuris = eng.queryNoEx( q );

		assertNotEquals( addeduris, newuris );
		assertTrue( newuris.isEmpty() );
	}

	@Test
	public void testGetLabel() throws Exception {

		assertEquals( eng.getEngineName(), EngineUtil.getEngineLabel( eng ) );

		String expected = "TEST LABEL";
		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( eng.getBaseUri(), RDFS.LABEL,
				new LiteralImpl( expected ) ) );

		eng.execute( sae );

		String label = EngineUtil.getEngineLabel( eng );
		assertEquals( expected, label );
	}

	@Test
	public void testReifStyle() throws Exception {
		assertEquals( ReificationStyle.LEGACY, EngineUtil.getReificationStyle( eng ) );

		assertEquals( ReificationStyle.LEGACY, EngineUtil.getReificationStyle( null ) );

		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( eng.getBaseUri(), VAS.ReificationModel,
				VAS.W3C_Reification ) );

		eng.execute( sae );

		ReificationStyle reif = EngineUtil.getReificationStyle( eng );
		assertEquals( ReificationStyle.W3C, reif );
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
