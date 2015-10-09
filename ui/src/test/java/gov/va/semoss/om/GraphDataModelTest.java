/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import edu.uci.ics.jung.graph.DirectedGraph;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.util.UriBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class GraphDataModelTest {

	private static final File DATAFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab = UriBuilder.getBuilder( "http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/" );
	private static final UriBuilder owlb = UriBuilder.getBuilder( "http://semoss.org/ontologies/" );
	private static final URI PRICE = owlb.build( "Price" );
	private static final URI YUGO = datab.build( "Yugo" );
	private static final URI YURI = datab.build( "Yuri" );
	private static final URI YPY = datab.build( "Yuri_Purchased_Yugo" );

	private InMemorySesameEngine eng;

	public GraphDataModelTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		eng = new InMemorySesameEngine();
		eng.setBuilders( datab, owlb );

		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( DATAFILE, null, RDFFormat.NTRIPLES );
		rc.commit();
	}

	@After
	public void tearDown() {
		eng.closeDB();
	}

	@Test
	public void testAddGraphLevel_uris() throws RepositoryException {
		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( Arrays.asList( YURI, YUGO ), eng, 1 );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gdm.getGraph();
		assertEquals( 2, graph.getVertexCount() );
		assertEquals( 0, graph.getEdgeCount() );
	}

	@Test
	public void testAddGraphLevel_model() throws RepositoryException {
		Model m = new LinkedHashModel();
		m.add( new StatementImpl( YURI, YPY, YUGO ) );

		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( m, eng, 1 );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gdm.getGraph();
		assertEquals( 2, graph.getVertexCount() );
		assertEquals( 1, graph.getEdgeCount() );

		SEMOSSEdge edge = graph.getEdges().iterator().next();
		assertEquals( "3000 USD", edge.getProperty( PRICE ) );
	}

	@Test
	public void testAddGraphLevel_model2() throws RepositoryException {
		Model m = new LinkedHashModel();
		m.add( new StatementImpl( YURI, YPY, new LiteralImpl( "something" ) ) );

		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( m, eng, 1 );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gdm.getGraph();
		assertEquals( 2, graph.getVertexCount() );
		assertEquals( 1, graph.getEdgeCount() );
	}

	@Test
	public void testAtLevel() {
		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( Arrays.asList( YURI ), eng, 1 );
		gdm.addGraphLevel( Arrays.asList( YUGO ), eng, 2 );

		Iterator<SEMOSSVertex> vit = gdm.getGraph().getVertices().iterator();
		SEMOSSVertex yuri = vit.next();
		SEMOSSVertex yugo = vit.next();

		assertTrue( gdm.presentAtLevel( yuri, 1 ) );
		assertFalse( gdm.presentAtLevel( yugo, 1 ) );

		assertTrue( gdm.presentAtLevel( yuri, 2 ) );
		assertTrue( gdm.presentAtLevel( yugo, 2 ) );
	}

	@Test
	public void testRemoveElementsSinceLevel() {
		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( Arrays.asList( YURI ), eng, 1 );
		Model m = new LinkedHashModel();
		m.add( new StatementImpl( YURI, YPY, YUGO ) );
		gdm.addGraphLevel( m, eng, 2 );

		Iterator<SEMOSSVertex> vit = gdm.getGraph().getVertices().iterator();
		SEMOSSVertex yuri = vit.next();
		SEMOSSVertex yugo = vit.next();

		assertTrue( gdm.presentAtLevel( yuri, 2 ) );
		assertTrue( gdm.presentAtLevel( yugo, 2 ) );
		assertEquals( 1, gdm.getGraph().getEdgeCount() );

		List<SEMOSSVertex> vs = new ArrayList<>();
		List<SEMOSSEdge> es = new ArrayList<>();
		gdm.removeElementsSinceLevel( 1, vs, es );

		assertTrue( gdm.getGraph().getEdges().isEmpty() );
		assertEquals( yuri, gdm.getGraph().getVertices().iterator().next() );
	}

	@Test
	public void testGetLevel() {
		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( Arrays.asList( YURI ), eng, 1 );
		gdm.addGraphLevel( Arrays.asList( YUGO ), eng, 2 );

		Iterator<SEMOSSVertex> vit = gdm.getGraph().getVertices().iterator();
		SEMOSSVertex yuri = vit.next();
		SEMOSSVertex yugo = vit.next();

		assertEquals( 1, gdm.getLevel( yuri ) );
		assertEquals( 2, gdm.getLevel( yugo ) );
	}
}
