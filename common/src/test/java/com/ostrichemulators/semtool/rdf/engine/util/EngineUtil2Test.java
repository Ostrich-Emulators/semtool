/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.rdf.engine.impl.BigDataEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.InsightManagerImpl;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.StatementAddingExecutor;
import com.ostrichemulators.semtool.util.Constants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class EngineUtil2Test {

	private static final Logger log = Logger.getLogger( EngineUtil2Test.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private File dbfile;
	private IEngine eng;

	public EngineUtil2Test() {
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
	public void testClear() throws Exception {
		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( RDFS.DATATYPE, RDFS.LABEL,
				new LiteralImpl( "test label" ) ) );
		eng.execute( sae );

		ListQueryAdapter<URI> q
				= OneVarListQueryAdapter.getUriList( "SELECT ?s WHERE { ?s rdfs:label ?o }" );

		List<URI> addeduris = new ArrayList<>( eng.queryNoEx( q ) );
		q.clear();

		EngineUtil2.clear( eng );
		List<URI> newuris = eng.queryNoEx( q );

		assertNotEquals( addeduris, newuris );
		assertTrue( newuris.isEmpty() );
	}

	@Test
	public void testGetLabel() throws Exception {

		assertEquals( eng.getEngineName(), EngineUtil2.getEngineLabel( eng ) );

		String expected = "TEST LABEL";
		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( eng.getBaseUri(), RDFS.LABEL,
				new LiteralImpl( expected ) ) );

		eng.execute( sae );

		String label = EngineUtil2.getEngineLabel( eng );
		assertEquals( expected, label );
	}

	@Test
	public void testReifStyle() throws Exception {
		assertEquals( ReificationStyle.SEMTOOL, EngineUtil2.getReificationStyle( eng ) );

		assertEquals( ReificationStyle.LEGACY, EngineUtil2.getReificationStyle( null ) );

		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( eng.getBaseUri(), SEMTOOL.ReificationModel,
				SEMTOOL.W3C_Reification ) );

		eng.execute( sae );

		ReificationStyle reif = EngineUtil2.getReificationStyle( eng );
		assertEquals( ReificationStyle.W3C, reif );
	}

	@Test
	public void testLoadEngine1() throws Exception {
		eng.closeDB();
		assertFalse( eng.isConnected() );
		eng = EngineUtil2.loadEngine( dbfile );
		assertTrue( eng.isConnected() );
		assertEquals( dbfile.toString(), eng.getProperty( Constants.SMSS_LOCATION ) );
	}

	@Test
	public void testLoadEngine2() throws Exception {
		eng.closeDB();
		assertFalse( eng.isConnected() );
		Properties props = BigDataEngine.generateProperties( dbfile );
		File tmp = File.createTempFile( "eu-test2-", ".properties" );
		try ( Writer w = new FileWriter( tmp ) ) {
			props.store( w, null );
		}

		eng = EngineUtil2.loadEngine( tmp );
		FileUtils.deleteQuietly( tmp );

		assertTrue( eng.isConnected() );
		assertEquals( tmp.toString(), eng.getProperty( Constants.SMSS_LOCATION ) );
	}

	@Test
	public void testCloseEngine() {
		assertTrue( eng.isConnected() );
		EngineUtil2.closeEngine( eng );
		assertFalse( eng.isConnected() );
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
			smss = EngineUtil2.createNew( ecb, null );
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
							new File( "src/main/resources/models/semtool.ttl" ),
							new File( "src/test/resources/insmgr.data-source.ttl" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.addVocabulary( new File( "src/main/resources/models/semtool.ttl" ).toURI().toURL() )
					.setBooleans( true, true, true );
			smss = EngineUtil2.createNew( ecb, null );
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
							new File( "src/main/resources/models/semtool.ttl" ),
							new File( "src/test/resources/insmgr.data-source.ttl" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.addVocabulary( new File( "src/main/resources/models/va-semoss.ttlx" ).toURI().toURL() )
					.setBooleans( true, true, true );
			smss = EngineUtil2.createNew( ecb, null );
			assertTrue( smss.exists() );
		}
		finally {
			FileUtils.deleteQuietly( topdir );
			FileUtils.deleteQuietly( smss );
		}
	}

	@Test
	public void testCreateInsights() throws Exception {
		File insights = new File( "src/test/resources/insmgr.data-source.ttl" );
		InsightManagerImpl imi = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( insights, imi );
		Collection<Perspective> persps = imi.getPerspectives();
		assertEquals( 1, persps.size() );
	}

	@Test( expected = EngineManagementException.class )
	public void testMustHaveInsightDataset() throws Exception {
		File insights = new File( "src/main/resources/models/semtool.ttl" );
		InsightManagerImpl imi = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( insights, imi );
	}
}
