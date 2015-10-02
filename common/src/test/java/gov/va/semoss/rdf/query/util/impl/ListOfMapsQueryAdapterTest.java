/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import java.util.List;
import java.util.Map;
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
public class ListOfMapsQueryAdapterTest {

	private static InMemorySesameEngine eng;

	public ListOfMapsQueryAdapterTest() {
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
	public void testHandleTuple() {
		ListOfMapsQueryAdapter<String> mapper
				= ListOfMapsQueryAdapter.forStrings( "SELECT ?a ?b WHERE { ?a rdfs:label ?b }" );
		List<Map<String, String>> results = eng.queryNoEx( mapper );

		assertEquals( 1, results.size() );
		Map<String, String> map = results.get( 0 );
		assertEquals( RDFS.DOMAIN.stringValue(), map.get( "a" ) );
		assertEquals( "test", map.get( "b" ) );
	}
}
