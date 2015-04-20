/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.UriBuilder;

/**
 *
 * @author ryan
 */
public class AbstractFileReaderTest {

  private static final URI BASEURI = new URIImpl( "http://junk.com/testfiles" );
  private static final String OWLSTART = "http://owl.junk.com/testfiles/";

  private RepositoryConnection rc;

  public AbstractFileReaderTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws Exception {
    rc = null;
    SailRepository repo = new SailRepository( new MemoryStore() );
    repo.initialize();

    try {
      rc = repo.getConnection();
    }
    catch ( Exception e ) {
      repo.shutDown();
      throw e;
    }
  }

  @After
  public void tearDown() {
    if ( null != rc ) {
      try {
        rc.commit();
      }
      catch ( Exception e ) {
        // don't care
      }
      try {
        rc.close();
      }
      catch ( Exception e ) {
        // don't care
      }

      try {
        rc.getRepository().shutDown();
      }
      catch ( Exception e ) {
        // don't care
      }
    }
  }

  @Test
  public void testGetCustomBaseURI() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    assertEquals( BASEURI, rdr.getDataBuilder().toUri() );
  }

  @Test
  public void testSetCustomBaseURI_URI() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    URIImpl newuri = new URIImpl( "http://blob.com/bloob" );
    rdr.setDataBuilder( UriBuilder.getBuilder( newuri ) );
    assertEquals( newuri, rdr.getDataBuilder().toUri() );
  }

  @Test
  public void testSetCustomBaseURI_String() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    String newuri = "http://blob.com/bloob";
    rdr.setDataBuilder( UriBuilder.getBuilder( newuri ) );
    assertEquals( new URIImpl( newuri ), rdr.getDataBuilder().toUri() );
  }

  @Test
  public void testSetNamespace1() throws Exception {
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/", rc );
    String ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/", ns );
  }

  @Test
  public void testSetNamespace2() throws Exception {
    // make sure we can overright a namespace
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/", rc );
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/new/", rc );
    String ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/new/", ns );
  }

  @Test
  public void testSetNamespace3() throws Exception {
    // make sure we can't set an empty namespace
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/", rc );
    AbstractFileReader.setNamespace( "mantech", null, rc );
    String ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/", ns );
  }

  @Test
  public void testSetNamespace3b() throws Exception {
    // make sure we can't set an empty namespace
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/", rc );
    AbstractFileReader.setNamespace( "mantech", "", rc );
    String ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/", ns );
  }

  @Test
  public void testSetNamespace3c() throws Exception {
    // make sure we can't set an empty namespace
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/", rc );
    AbstractFileReader.setNamespace( "mantech", "    ", rc );
    String ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/", ns );
  }

  @Test
  public void testSetNamespace3d() throws Exception {
    // make sure we can't set an empty namespace
    AbstractFileReader.setNamespace( "mantech", "http://mantech.com/   ", rc );
    String ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/", ns );
  }

  @Test
  public void testSetNamespace3e() throws Exception {
    // make sure we can't set an empty namespace
    AbstractFileReader.setNamespace( "  mantech", "http://mantech.com/", rc );
    String ns = rc.getNamespace( "  mantech" );
    assertNull( ns );

    ns = rc.getNamespace( "mantech" );
    assertEquals( "http://mantech.com/", ns );
  }

  @Test
  public void testSetNamespace4() throws Exception {
    // don't allow colons in namespaces
    AbstractFileReader.setNamespace( "mantech:2", "http://mantech.com:8080/", rc );
    String ns = rc.getNamespace( "mantech:2" );
    assertNull( ns );

    ns = rc.getNamespace( "mantech2" );
    assertEquals( "http://mantech.com:8080/", ns );
  }

  // @Test
  public void testGetRelationInstanceURI() throws Exception {
  }

  // @Test
  public void testGetRelationClassURI() throws Exception {
  }

  // @Test
  public void testGetConceptURI() throws Exception {
  }

  // @Test
  public void testGetInstanceURI() throws Exception {
  }

  // @Test
  public void testCreateRelationship() throws Exception {
  }

  // @Test
  public void testAddNodeProperties() throws Exception {
  }

  // @Test
  public void testAddProperties() throws Exception {
  }

  @Test
  public void testGetBasePropUri() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    URI basePropURI = new URIImpl( OWLSTART + Constants.DEFAULT_RELATION_CLASS
        + "/" + Constants.CONTAINS );
    assertEquals( basePropURI, rdr.getBasePropUri() );
  }

  @Test
  public void testGetOwlStarter() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    assertEquals( OWLSTART, rdr.getSchemaBuilder().toUri()+"/" );
  }

  // @Test
  public void testCreateBaseRelations() throws Exception {
  }

  @Test
  public void testSetRdfMap() {
    Map<String, String> rdfMap = new HashMap<>();
    rdfMap.put( "one", "two" );
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    rdr.setRdfMap( rdfMap );
    assertEquals( rdfMap, rdr.rdfMap );
  }

  @Test
  public void testSetAutocreateMetamodel() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    rdr.setAutocreateMetamodel( true );
    assertTrue( rdr.isAutocreateMetamodel() );
  }

  @Test
  public void testSetAutocreateMetamodel2() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    rdr.setAutocreateMetamodel( false );
    assertFalse( rdr.isAutocreateMetamodel() );
  }

  @Test
  public void testIsAutocreateMetamodel() {
    AbstractFileReader rdr = new AbstractFileReaderImpl( UriBuilder.getBuilder( BASEURI ),
        UriBuilder.getBuilder( OWLSTART ) );
    assertTrue( rdr.isAutocreateMetamodel() );
  }

  public class AbstractFileReaderImpl extends AbstractFileReader {

    public AbstractFileReaderImpl() {
    }

    public AbstractFileReaderImpl( UriBuilder _customBaseURI, UriBuilder owlUri ) {
      super( _customBaseURI, owlUri );
    }

    @Override
    public void importOneFile( File f, RepositoryConnection rcOWL )
        throws IOException, RepositoryException {
    }
  }
}
