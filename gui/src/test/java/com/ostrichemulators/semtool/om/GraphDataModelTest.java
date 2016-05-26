/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import edu.uci.ics.jung.graph.DirectedGraph;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

	private static final Comparator<SEMOSSVertex> VERTCOMP
			= new Comparator<SEMOSSVertex>() {
				@Override
				public int compare( SEMOSSVertex o1, SEMOSSVertex o2 ) {
					return o1.toString().compareTo( o2.toString() );
				}
			};
	private static final File DATAFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab = UriBuilder.getBuilder( "http://os-em.com/semtool/database/Ke42d9335-1c26-475a-96bd-9bde6a2ab5e5/" );
	private static final UriBuilder owlb = UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool" );
	private static final URI PRICE = owlb.build( "Price" );
	private static final URI YUGO = datab.build( "Yugo" );
	private static final URI YURI = datab.build( "Yuri" );
	private static final URI YPY = datab.build( "Yuri_Purchased_Yugo" );
	private static final URI PURCHASE = owlb.build( "Purchased" );

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
		eng = InMemorySesameEngine.open();
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
	public void testAddGraphLevel_model_generic() throws RepositoryException {
		Model m = new LinkedHashModel();
		m.add( new StatementImpl( YURI, PURCHASE, YUGO ) );

		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( m, eng, 1 );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gdm.getGraph();
		assertEquals( 2, graph.getVertexCount() );
		assertEquals( 1, graph.getEdgeCount() );

		SEMOSSEdge edge = graph.getEdges().iterator().next();
		assertEquals( "3000 USD", edge.getValue( PRICE ).stringValue() );
	}

	// FIXME: this test should work, but the GDM query isn't quite right
	//@Test
	public void testAddGraphLevel_model_specific() throws RepositoryException {
		Model m = new LinkedHashModel();
		m.add( new StatementImpl( YURI, PURCHASE, YUGO ) );

		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( m, eng, 1 );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = gdm.getGraph();
		assertEquals( 2, graph.getVertexCount() );
		assertEquals( 1, graph.getEdgeCount() );

		SEMOSSEdge edge = graph.getEdges().iterator().next();
		assertEquals( "3000 USD", edge.getValue( PRICE ).stringValue() );
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

		List<SEMOSSVertex> list = new ArrayList<>( gdm.getGraph().getVertices() );
		Collections.sort( list, VERTCOMP );

		SEMOSSVertex yuri = list.get( 1 );
		SEMOSSVertex yugo = list.get( 0 );

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

		List<SEMOSSVertex> list = new ArrayList<>( gdm.getGraph().getVertices() );
		Collections.sort( list, VERTCOMP );

		SEMOSSVertex yuri = list.get( 1 );
		SEMOSSVertex yugo = list.get( 0 );

		assertTrue( gdm.presentAtLevel( yuri, 2 ) );
		assertTrue( gdm.presentAtLevel( yugo, 2 ) );
		assertEquals( 1, gdm.getGraph().getEdgeCount() );

		gdm.removeElementsSinceLevel( 1 );

		assertTrue( gdm.getGraph().getEdges().isEmpty() );
		assertEquals( yuri, gdm.getGraph().getVertices().iterator().next() );
	}

	@Test
	public void testGetLevel() {
		GraphDataModel gdm = new GraphDataModel();
		gdm.addGraphLevel( Arrays.asList( YURI ), eng, 1 );
		gdm.addGraphLevel( Arrays.asList( YUGO ), eng, 2 );

		List<SEMOSSVertex> list = new ArrayList<>( gdm.getGraph().getVertices() );
		Collections.sort( list, VERTCOMP );

		SEMOSSVertex yuri = list.get( 1 );
		SEMOSSVertex yugo = list.get( 0 );

		assertEquals( 1, gdm.getLevel( yuri ) );
		assertEquals( 2, gdm.getLevel( yugo ) );
	}
}
