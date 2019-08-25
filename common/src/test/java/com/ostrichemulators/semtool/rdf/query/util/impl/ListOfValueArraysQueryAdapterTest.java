/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class ListOfValueArraysQueryAdapterTest {

	private static InMemorySesameEngine eng;

	public ListOfValueArraysQueryAdapterTest() {
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
		ListOfValueArraysQueryAdapter q
				= new ListOfValueArraysQueryAdapter( "SELECT ?a ?b WHERE { ?a rdfs:label ?b }" );
		List<Value[]> results = eng.queryNoEx( q );

		assertEquals( 1, results.size() );
		Value[] vals = results.get( 0 );
		assertEquals( 2, vals.length );
		assertEquals( RDFS.DOMAIN, vals[0] );
		assertEquals( "test", vals[1].stringValue() );
	}
}
