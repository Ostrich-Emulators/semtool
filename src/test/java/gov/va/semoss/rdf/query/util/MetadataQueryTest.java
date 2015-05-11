/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.query.util;

import gov.va.semoss.model.vocabulary.VAS;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.util.UriBuilder;
import org.openrdf.model.Value;

/**
 *
 * @author ryan
 */
public class MetadataQueryTest {

  private Repository repo;
  private InMemorySesameEngine engine;
  private final UriBuilder bldr = UriBuilder.getBuilder( "http://test.va.gov/unit/" );

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    if ( null != engine ) {
      engine.closeDB();
    }
    if ( null != repo ) {
      try {
        repo.shutDown();
      }
      catch ( Exception e ) {
        // don't care
      }
    }

    engine = new InMemorySesameEngine();
    repo = new SailRepository( new MemoryStore() );
    try {
      repo.initialize();
      RepositoryConnection rc = repo.getConnection();
      engine.setRepositoryConnection( rc );
      rc.add( bldr.toUri(), RDF.TYPE, VAS.Database );
    }
    catch ( Exception e ) {
    }
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetOne() {
  }

  @Test
  public void testHandleTupleExists() throws Exception {
    ValueFactory vf = engine.getRawConnection().getValueFactory();
    String raw = "The Team!";

    engine.getRawConnection().add( bldr.toUri(), MetadataConstants.DCT_CREATOR,
        vf.createLiteral( raw ) );
    MetadataQuery mq = new MetadataQuery();
    engine.query( mq );
		Map<URI, String> data = mq.asStrings();
    assertTrue( 2 == data.size() );
    assertEquals( raw, data.get( MetadataConstants.DCT_CREATOR ) );
  }

  @Test
  public void testHandleTupleUpgradeDcToDct() throws Exception {
    ValueFactory vf = engine.getRawConnection().getValueFactory();
    String raw = "old publisher predicate";
    engine.getRawConnection().add( bldr.toUri(), DC.PUBLISHER,
        vf.createLiteral( raw ) );
    MetadataQuery mq = new MetadataQuery();
    engine.query( mq );
		Map<URI, String> data = mq.asStrings();

    assertEquals( raw, data.get( MetadataConstants.DCT_PUBLISHER ) );
    assertTrue( 2 == data.size() );
  }

  @Test
  public void testHandleTupleNonUpgradeDcToDct() throws Exception {
    ValueFactory vf = engine.getRawConnection().getValueFactory();
    String raw = "should be a date, but that's okay";
    engine.getRawConnection().add(bldr.toUri(), MetadataConstants.DCT_CREATED,
        vf.createLiteral( raw ) );
    MetadataQuery mq = new MetadataQuery();
    engine.query( mq );
		Map<URI, String> data = mq.asStrings();

    assertEquals( raw, data.get(MetadataConstants.DCT_CREATED ) );
    assertTrue( 2 == data.size() );
  }

  @Test
  public void testHandleTupleMissingDc() throws Exception {
    ValueFactory vf = engine.getRawConnection().getValueFactory();
    String raw = "desc";
    engine.getRawConnection().add( bldr.toUri(), MetadataConstants.DCT_DESC,
        vf.createLiteral( raw ) );
    MetadataQuery mq = new MetadataQuery();
    engine.query( mq );
		Map<URI, String> data = mq.asStrings();

    assertFalse( data.containsKey(MetadataConstants.DCT_CREATED ) );
    assertTrue( 2 == data.size() );
  }
}
