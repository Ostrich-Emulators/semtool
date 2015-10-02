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
import gov.va.semoss.rdf.query.util.impl.ListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.rdf.query.util.impl.StatementAddingExecutor;
import gov.va.semoss.util.Constants;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class EngineUtil2Test {

	private static final Logger log = Logger.getLogger(EngineUtil2Test.class );
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
		assertEquals( ReificationStyle.LEGACY, EngineUtil2.getReificationStyle( eng ) );

		assertEquals( ReificationStyle.LEGACY, EngineUtil2.getReificationStyle( null ) );

		StatementAddingExecutor sae = new StatementAddingExecutor();
		sae.addStatement( new StatementImpl( eng.getBaseUri(), VAS.ReificationModel,
				VAS.W3C_Reification ) );

		eng.execute( sae );

		ReificationStyle reif = EngineUtil2.getReificationStyle( eng );
		assertEquals( ReificationStyle.W3C, reif );
	}
}
