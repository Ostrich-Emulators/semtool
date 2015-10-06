/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
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
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		InsightManagerImpl imi = new InsightManagerImpl();
		imi.loadFromRepository( rc );
		rc.close();
		repo.shutDown();

		FileUtils.deleteQuietly( datadir );
		assertEquals( 1, imi.getPerspectives().size() );
	}
}
