/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util.impl;

import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;

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
