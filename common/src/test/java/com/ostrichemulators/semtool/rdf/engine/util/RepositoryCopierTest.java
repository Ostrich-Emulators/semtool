/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import org.eclipse.rdf4j.model.Model;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author ryan
 */
public class RepositoryCopierTest {

	private RepositoryConnection from;
	private RepositoryConnection to;

	public RepositoryCopierTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		Repository fromrepo = new SailRepository( new MemoryStore() );
		Repository torepo = new SailRepository( new MemoryStore() );

		fromrepo.init();
		torepo.init();

		from = fromrepo.getConnection();
		to = torepo.getConnection();

		from.add( fromrepo.getValueFactory().createStatement( RDFS.DOMAIN, RDFS.LABEL,
				fromrepo.getValueFactory().createLiteral( "test" ) ) );
		from.add( fromrepo.getValueFactory().createStatement( RDFS.DOMAIN, RDFS.LABEL,
				fromrepo.getValueFactory().createLiteral( "test2" ) ) );
		from.setNamespace( OWL.PREFIX, OWL.NAMESPACE );
		from.commit();
	}

	@After
	public void tearDown() throws Exception {
		from.close();
		from.getRepository().shutDown();

		to.close();
		to.getRepository().shutDown();
	}

	@Test
	public void testCopy() throws Exception {
		RepositoryCopier rc = new RepositoryCopier( to );
		rc.setCommitLimit( 1 );

		from.export( rc );

		assertEquals( 2, to.size() );


		Model m = QueryResults.asModel( to.getStatements( RDFS.DOMAIN, null, null, true ));
		assertTrue( m.contains( null, RDFS.LABEL, null ) );
	}
}
