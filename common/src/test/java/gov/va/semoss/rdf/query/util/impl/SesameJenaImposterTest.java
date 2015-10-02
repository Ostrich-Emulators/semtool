/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class SesameJenaImposterTest {

	private static InMemorySesameEngine eng;

	public SesameJenaImposterTest() {
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
	public void testQuery() {
		SesameJenaImposter sji = new SesameJenaImposter( "SELECT ?s ?o WHERE { ?s rdfs:label ?o  }");
		List<Object[]> results = eng.queryNoEx( sji );
		assertEquals( "test", results.get( 0 )[1] );	
	}

	@Test
	public void testGetVariables() {
		SesameJenaImposter sji = new SesameJenaImposter( "SELECT ?s ?o WHERE { ?s rdfs:label ?o }");
		eng.queryNoEx( sji );
		assertEquals( "s", sji.getVariables()[0] );
		assertEquals( "o", sji.getVariables()[1] );
	}

}
