/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

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
		eng = InMemorySesameEngine.open();
		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( rc.getValueFactory().createStatement( RDFS.DOMAIN, RDFS.LABEL, rc.getValueFactory().createLiteral( "test" ) ) );
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
