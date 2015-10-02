/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class AbstractBindableTest {

	public AbstractBindableTest() {
	}

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
	public void testSetNamespaces() {
		Impl impl = new Impl();
		Map<String, String> map = new HashMap<>();
		map.put( RDFS.PREFIX, RDFS.NAMESPACE );
		impl.setNamespaces( map );
		assertEquals( RDFS.NAMESPACE, impl.getNamespaces().get( RDFS.PREFIX ) );
	}

	@Test
	public void testResetNamespaces() {
		Impl impl = new Impl();
		impl.addNamespace( OWL.PREFIX, OWL.NAMESPACE );
		impl.addNamespace( RDFS.PREFIX, RDFS.NAMESPACE );

		Map<String, String> map = new HashMap<>();
		map.put( RDFS.PREFIX, RDFS.NAMESPACE );
		impl.setNamespaces( map );

		assertEquals( 1, impl.getNamespaces().size() );
		assertEquals( RDFS.NAMESPACE, impl.getNamespaces().get( RDFS.PREFIX ) );
	}

	@Test
	public void testAddNamespace() {
		Impl impl = new Impl();
		impl.addNamespace( OWL.PREFIX, OWL.NAMESPACE );
		impl.addNamespace( RDFS.PREFIX, RDFS.NAMESPACE );

		assertEquals( 2, impl.getNamespaces().size() );
		assertEquals( RDFS.NAMESPACE, impl.getNamespaces().get( RDFS.PREFIX ) );
	}

	@Test
	public void testBindAndGetSparql1() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		impl.bind( "p", RDFS.LABEL );
		assertEquals( "SELECT ?s ?p ?o WHERE { ?s ?p ?o  VALUES ?p {<http://www.w3.org/2000/01/rdf-schema#label>}}",
				impl.bindAndGetSparql() );
	}

	@Test
	public void testBindAndGetSparql2() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		impl.bind( "missing", RDFS.LABEL );
		assertEquals( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }", impl.bindAndGetSparql() );
	}

	@Test
	public void testBindURI_String_String() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		impl.bind( "o", "test", "en" );
		assertEquals( "SELECT ?s ?p ?o WHERE { ?s ?p ?o  VALUES ?o {\"test\"@en}}",
				impl.bindAndGetSparql() );
	}

	@Test
	public void testBindURI_String_String2() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		impl.bindURI( "p", RDFS.LABEL.stringValue() );
		assertEquals( "SELECT ?s ?p ?o WHERE { ?s ?p ?o  VALUES ?p {<http://www.w3.org/2000/01/rdf-schema#label>}}",
				impl.bindAndGetSparql() );
	}

	@Test
	public void testBindURI_3args() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		impl.bindURI( "p", RDFS.NAMESPACE, "label" );
		assertEquals( "SELECT ?s ?p ?o WHERE { ?s ?p ?o  VALUES ?p {<http://www.w3.org/2000/01/rdf-schema#label>}}",
				impl.bindAndGetSparql() );
	}

	@Test
	public void testDate() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		Date now = new Date();
		impl.bind( "o", now );

		assertTrue( impl.bindAndGetSparql().contains( XMLSchema.DATETIME.stringValue() ) );
	}

	@Test
	public void testDescribe() {
		Impl impl = new Impl( "DESCRIBE ?s" );
		impl.bind( "s", RDFS.DOMAIN );

		assertEquals( "DESCRIBE ?s VALUES ?s {<http://www.w3.org/2000/01/rdf-schema#domain>}",
				impl.bindAndGetSparql() );
	}

	@Test
	public void testBind_String_Value() {
		Impl impl = new Impl( "SELECT ?s ?p ?o WHERE { ?s ?p ?o }" );
		impl.bind( "o", true );
		assertEquals( "SELECT ?s ?p ?o WHERE { ?s ?p ?o  VALUES ?o {\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>}}",
				impl.bindAndGetSparql() );
	}

	public class Impl extends AbstractBindable {

		public Impl() {
		}

		public Impl( String sparq ) {
			super( sparq );
		}
	}
}
