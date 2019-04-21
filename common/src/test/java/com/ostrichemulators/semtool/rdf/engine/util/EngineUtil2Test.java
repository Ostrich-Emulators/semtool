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
import com.ostrichemulators.semtool.rdf.engine.impl.EngineFactory;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.InsightManagerImpl;
import com.ostrichemulators.semtool.rdf.engine.impl.SesameEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.ListQueryAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.ModelQueryAdapter;
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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;

/**
 *
 * @author ryan
 */
public class EngineUtil2Test {

	private static final Logger log = Logger.getLogger( EngineUtil2Test.class );
	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );

	private static final File COW = new File( "src/test/resources/CoW.xlsx" );
	private static final File COW_EXP = new File( "src/test/resources/CoW.nt" );

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
	public void setUp() throws Exception {
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
		sae.addStatement( new StatementImpl( eng.getBaseIri(), RDFS.LABEL,
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
		sae.addStatement( new StatementImpl( eng.getBaseIri(), SEMTOOL.ReificationModel,
				SEMTOOL.W3C_Reification ) );

		eng.execute( sae );

		ReificationStyle reif = EngineUtil2.getReificationStyle( eng );
		assertEquals( ReificationStyle.W3C, reif );
	}

	@Test
	public void testLoadEngine1() throws Exception {
		eng.closeDB();
		assertFalse( eng.isConnected() );
		eng = EngineFactory.getEngine( dbfile );
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

		eng = EngineFactory.getEngine( tmp );
		FileUtils.deleteQuietly( tmp );

		assertTrue( eng.isConnected() );
		assertEquals( dbfile.toString(), eng.getProperty( Constants.SMSS_LOCATION ) );
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
					.setInsightsFile( new File( "src/test/resources/insmgr.data-source.ttl" ) )
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
					.setInsightsFile( new File( "src/test/resources/insmgr.data-source.ttl" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.addVocabulary( new File( "src/main/resources/models/semtool.ttlx" ).toURI().toURL() )
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
	public void createNew4() throws IOException, EngineManagementException {
		File topdir = File.createTempFile( "eutest-", "" );
		topdir.delete();
		topdir.mkdirs();
		File smss = null;

		try {
			EngineCreateBuilder ecb = new EngineCreateBuilder( topdir, "testdb" )
					.setDefaultBaseUri( new URIImpl( "http://va.gov/ontologies" ), true )
					.setReificationModel( ReificationStyle.LEGACY )
					.setInsightsFile( new File( "src/test/resources/insmgr.data-source.ttl" ) )
					.setFiles( Arrays.asList( LEGACY ) )
					.addVocabulary( new File( "src/main/resources/models/semtool.ttl" ).toURI().toURL() )
					.setBooleans( true, true, true )
					.setEngineImpl( BigDataEngine.class );
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

	@Test
	public void testCoW() throws Exception {
		File topdir = File.createTempFile( "eutest-", "" );
		topdir.delete();
		topdir.mkdirs();
		File smss = null;

		try {
			EngineCreateBuilder ecb = new EngineCreateBuilder( topdir, "testdb" )
					.setDefaultBaseUri( new URIImpl( "http://va.gov/ontologies" ), true )
					.setReificationModel( ReificationStyle.SEMTOOL )
					.setFiles( Arrays.asList( COW ) )
					.addVocabulary( new File( "src/main/resources/models/semtool.ttl" ).toURI().toURL() )
					.setBooleans( true, true, true )
					.setEngineImpl( SesameEngine.class );
			smss = EngineUtil2.createNew( ecb, null );
			assertTrue( smss.exists() );

			IEngine engine = EngineFactory.getEngine( smss );
			Model model
					= engine.constructNoEx( new ModelQueryAdapter( "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }" ) );
			model.remove( (Resource) null, RDF.TYPE, SEMTOOL.Database );

			if ( log.isTraceEnabled() ) {
				try ( FileWriter fw = new FileWriter( "/tmp/CoW.nt" ) ) {
					NTriplesWriter tw = new NTriplesWriter( fw );
					tw.startRDF();
					for ( Statement s : model ) {
						tw.handleStatement( s );
					}
					tw.endRDF();
				}
			}

			InMemorySesameEngine mem = InMemorySesameEngine.open( true );
			mem.getRawConnection().add( COW_EXP, "http://va.gov/ontologies", RDFFormat.TURTLE );
			Model exp = mem.toModel();
			exp.remove( (Resource) null, RDF.TYPE, SEMTOOL.Database );

			assertEquals( exp.size(), model.size() );
		}
		finally {
			FileUtils.deleteQuietly( topdir );
			FileUtils.deleteQuietly( smss );
		}
	}

}
