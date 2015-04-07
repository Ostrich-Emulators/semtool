/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author ryan
 */
public class UriBuilderTest {

  private static final String OWLSTART = "http://owl.junk.com/testfiles/";
  private static final String OWLSTART_NOSLASH = "http://owl.junk.com/testfiles";

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
    assertEquals( OWLSTART, bldr.toUri().stringValue() + "/" );
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
    URI uri = new URIImpl( OWLSTART + "food" );
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    assertTrue( bldr.contains( uri ) );
  }

  @Test
  public void testContains_Resource2() {
    URI uri = new URIImpl( "http://foo.com/food" );
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    assertFalse( bldr.contains( uri ) );
  }

  @Test
  public void testGetRelationUri_String() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = new URIImpl( OWLSTART + "Relation/bobo" );
    assertEquals( uri, bldr.getRelationUri( "bobo" ) );
  }

  @Test
  public void testGetContainsUri() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = new URIImpl( OWLSTART + "Relation/Contains" );
    assertEquals( uri, bldr.getContainsUri() );
  }

  @Test
  public void testGetConceptUri() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = new URIImpl( OWLSTART + "Concept" );
    assertEquals( uri, bldr.getConceptUri().build() );
  }

  @Test
  public void testBuild1() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = bldr.add( "concept" ).add( "shmoncept" ).build();
    URI exp = new URIImpl( OWLSTART + "concept/shmoncept" );
    assertEquals( exp, uri );
  }

  @Test
  public void testBuild2() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = bldr.add( "concept" ).add( "shmoncept/" ).build();
    URI exp = new URIImpl( OWLSTART + "concept/shmoncept" );
    assertEquals( exp, uri );
  }

  @Test
  public void testSanitizer1() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = bldr.add( "concept/shmoncept/" ).build();
    URI exp = new URIImpl( OWLSTART + "L66bce8451fb3ffa8f0803f6ad51ec384" );
    assertEquals( exp, uri );
  }

  @Test
  public void testSanitizer2() {
    UriBuilder bldr = UriBuilder.getBuilder( OWLSTART );
    URI uri = bldr.add( "concept shmoncept" ).build();
    URI exp = new URIImpl( OWLSTART + "C17898a69eccedb712f4dc39ae0e7ed36" );
    assertEquals( exp, uri );
  }
}
