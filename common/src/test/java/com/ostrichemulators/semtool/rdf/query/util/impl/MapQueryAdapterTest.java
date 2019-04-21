/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class MapQueryAdapterTest {

	private static InMemorySesameEngine eng;

	public MapQueryAdapterTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		eng = InMemorySesameEngine.open();
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
	public void testClear() {
		MapQueryAdapterImpl mapper 
				= new MapQueryAdapterImpl("SELECT ?a ?b WHERE { ?a rdfs:label ?b }");
		Map<URI, String> results = eng.queryNoEx( mapper );		
		assertEquals( "test", results.get( RDFS.DOMAIN ) );

		results.clear();
		assertTrue( mapper.getResults().isEmpty() );	
	}

	@Test
	public void testAdd() {
		MapQueryAdapterImpl mapper 
				= new MapQueryAdapterImpl("SELECT ?a ?b WHERE { ?a rdfs:label ?b }");
		Map<URI, String> results = eng.queryNoEx( mapper );		
		assertEquals( "test", results.get( RDFS.DOMAIN ) );
	}

	public class MapQueryAdapterImpl extends MapQueryAdapter<URI, String> {

		public MapQueryAdapterImpl( String sparq ) {
			super( sparq );
		}

		@Override
		public void handleTuple( BindingSet set, ValueFactory fac ) {
			add( URI.class.cast( set.getValue( "a" ) ),
					set.getValue( "b" ).stringValue() );
		}
	}
}
