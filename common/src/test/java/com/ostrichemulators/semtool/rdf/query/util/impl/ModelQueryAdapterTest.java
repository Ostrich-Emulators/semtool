/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

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
	public void testDescribe() throws Exception {
		Model m = eng.construct( ModelQueryAdapter.describe( RDFS.DOMAIN ) );
		assertEquals( 1, m.size() );
		assertEquals( RDFS.DOMAIN, m.subjects().iterator().next() );
	}

}
