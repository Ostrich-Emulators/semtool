/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import com.ostrichemulators.semtool.user.LocalUserImpl;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class InsightManagerImplTest {

	private static final File SRCFILE = new File( "src/test/resources/insmgr.data-source.ttl" );

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
	public void testCreateFromRepository() throws Exception {
		Repository repo = new SailRepository( new MemoryStore() );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		rc.add( SRCFILE, null, RDFFormat.TURTLE );
		rc.commit();
		rc.close();

		InsightManager imi = InsightManagerImpl.createFromRepository( repo );
		repo.shutDown();

		assertEquals( 1, imi.getPerspectives().size() );
	}

	@Test
	public void testCreateStatements() throws Exception {
		InsightManagerImpl imi = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( SRCFILE, imi );

		Collection<Statement> stmts
				= InsightManagerImpl.getStatements( imi, new LocalUserImpl() );
		assertEquals( 22, stmts.size() );
	}

	@Test
	public void testSystemP() {
		InsightManagerImpl imi = new InsightManagerImpl();
		InMemorySesameEngine eng = InMemorySesameEngine.open();
		eng.setBuilders( UriBuilder.getBuilder( Constants.ANYNODE + "/data/" ),
				UriBuilder.getBuilder( Constants.ANYNODE + "/schema/" ) );
		Perspective p = imi.getSystemPerspective( eng );
		assertEquals( 3, p.getInsights().size() );
	}

	@Test
	public void testCtor() throws IOException, EngineManagementException {
		InsightManagerImpl imi = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( SRCFILE, imi );

		InsightManager im = new InsightManagerImpl( imi );
		assertEquals( imi.getPerspectives(), im.getPerspectives() );

	}
}
