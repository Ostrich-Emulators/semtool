/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author ryan
 */
public class UriBuilderTest {

	private static final String OWLSTART = "http://owl.junk.com/testfiles/";

	public UriBuilderTest() {
	}

	@BeforeClass
	public static void setUpClass() {
		UriBuilder.setDefaultSanitizerClass( DeterministicSanitizer.class );
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
	public void testGetBuilder() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		assertEquals( OWLSTART, bldr.toIRI().stringValue() + "/" );
	}

	@Test
	public void testContains_String() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		assertTrue( bldr.contains( OWLSTART + "foo/bar" ) );
	}

	@Test
	public void testContains_String2() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		assertFalse( bldr.contains( "http://foo.com/bar" ) );
	}

	@Test
	public void testContains_Resource() {
		IRI uri = SimpleValueFactory.getInstance().createIRI( OWLSTART + "food" );
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		assertTrue( bldr.contains( uri ) );
	}

	@Test
	public void testContains_Resource2() {
		IRI uri = SimpleValueFactory.getInstance().createIRI( "http://foo.com/food" );
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		assertFalse( bldr.contains( uri ) );
	}

	@Test
	public void testGetRelationUri_String() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = SimpleValueFactory.getInstance().createIRI( OWLSTART + "Relation/bobo" );
		assertEquals( uri, bldr.getRelationIri( "bobo" ) );
	}

	@Test
	public void testGetContainsUri() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = SimpleValueFactory.getInstance().createIRI( OWLSTART + "Relation/Contains" );
		assertEquals( uri, bldr.getContainsIri() );
	}

	@Test
	public void testGetConceptUri() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = SimpleValueFactory.getInstance().createIRI( OWLSTART + "Concept" );
		assertEquals( uri, bldr.getConceptIri().build() );
	}

	@Test
	public void testBuild1() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = bldr.add( "concept" ).add( "shmoncept" ).build();
		IRI exp = SimpleValueFactory.getInstance().createIRI( OWLSTART + "concept/shmoncept" );
		assertEquals( exp, uri );
	}

	@Test
	public void testBuild2() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = bldr.add( "concept" ).add( "shmoncept/" ).build();
		IRI exp = SimpleValueFactory.getInstance().createIRI( OWLSTART + "concept/shmoncept" );
		assertEquals( exp, uri );
	}

	@Test
	public void testSanitizer1() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = bldr.add( "concept/shmoncept/" ).build();
		IRI exp = SimpleValueFactory.getInstance().createIRI( OWLSTART + "L66bce8451fb3ffa8f0803f6ad51ec384" );
		assertEquals( exp, uri );
	}

	@Test
	public void testSanitizer2() {
		UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
		IRI uri = bldr.add( "concept shmoncept" ).build();
		IRI exp = SimpleValueFactory.getInstance().createIRI( OWLSTART + "concept_shmoncept" );
		assertEquals( exp, uri );
	}
}
