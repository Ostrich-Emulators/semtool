/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class ModelQueryAdapterTest {

	private static InMemorySesameEngine eng;

	public ModelQueryAdapterTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		eng = new InMemorySesameEngine();
		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL, new LiteralImpl( "test" ) ) );
		rc.commit();
	}

	@AfterClass
	public static void tearDownClass() {
		eng.closeDB();
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testDescribe() throws Exception {
		Model m = eng.construct( ModelQueryAdapter.describe( RDFS.DOMAIN ) );
		assertEquals( 1, m.size() );
		assertEquals( RDFS.DOMAIN, m.subjects().iterator().next() );
	}

}
