/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.user.LocalUserImpl;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class InsightManagerImplTest {

	private static final File LEGACY_QUESTIONS = new File( "src/test/resources/questions.prop" );
	private static final File DATAFILE = new File( "src/test/resources/insmgr.data" );

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testLoadLegacyData() throws IOException {
		InsightManagerImpl imi = new InsightManagerImpl();
		Properties props = Utility.loadProp( LEGACY_QUESTIONS );
		imi.loadLegacyData( props );
		assertEquals( 8, imi.getPerspectives().size() );
	}

	@Test
	public void testCreateFromRepository() throws Exception {
		File datadir = File.createTempFile( "insdata-", "" );
		datadir.delete();
		datadir.mkdirs();
		File datafile = new File( datadir, "memorystore.data" );
		FileUtils.copyFile( DATAFILE, datafile );

		Repository repo = new SailRepository( new MemoryStore( datadir ) );
		InsightManager imi = InsightManagerImpl.createFromRepository( repo );
		repo.shutDown();

		FileUtils.deleteQuietly( datadir );
		assertEquals( 1, imi.getPerspectives().size() );
	}

	@Test
	public void testCreateStatements() throws Exception {
		InsightManagerImpl imi = new InsightManagerImpl();
		Properties props = Utility.loadProp( LEGACY_QUESTIONS );
		imi.loadLegacyData( props );

		Collection<Statement> stmts
				= InsightManagerImpl.getStatements( imi, new LocalUserImpl() );
		assertEquals( 1502, stmts.size() );
	}

	@Test
	public void testSystemP() {
		InsightManagerImpl imi = new InsightManagerImpl();
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setBuilders( UriBuilder.getBuilder( Constants.ANYNODE + "/data/" ),
				UriBuilder.getBuilder( Constants.ANYNODE + "/schema/" ) );
		Perspective p = imi.getSystemPerspective( eng );
		assertEquals( 3, p.getInsights().size() );
	}
}
