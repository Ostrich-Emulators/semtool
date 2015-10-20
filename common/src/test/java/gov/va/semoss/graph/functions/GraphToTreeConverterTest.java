/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.graph.functions;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.graph.util.EdgeType;
import gov.va.semoss.util.UriBuilder;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class GraphToTreeConverterTest {

	private static final UriBuilder datab = UriBuilder.getBuilder( "http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/" );
	private static final UriBuilder owlb = UriBuilder.getBuilder( "http://semoss.org/ontologies/" );
	private static final URI PRICE = owlb.build( "Price" );
	private static final URI YUGO = datab.build( "Yugo" );
	private static final URI YURI = datab.build( "Yuri" );
	private static final URI YPY = datab.build( "Yuri_Purchased_Yugo" );

	public GraphToTreeConverterTest() {
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
	public void testBfs() {

		DirectedGraph<URI, URI> graph = new DirectedSparseGraph<>();
		graph.addVertex( YURI );
		graph.addVertex( YUGO );
		graph.addEdge( YPY, YURI, YUGO, EdgeType.DIRECTED );

		Collection<URI> roots = Arrays.asList( YURI );
		GraphToTreeConverter.Search search = GraphToTreeConverter.Search.BFS;
		Forest<URI, URI> result = GraphToTreeConverter.convert( graph, roots, search );
		assertEquals( 1, result.getTrees().size() );

		Tree<URI, URI> tree = result.getTrees().iterator().next();
		assertEquals( YURI, tree.getRoot() );
		assertEquals( YUGO, tree.getChildren( YURI ).iterator().next() );
	}

	@Test
	public void testDfs() {

		DirectedGraph<URI, URI> graph = new DirectedSparseGraph<>();
		graph.addVertex( YURI );
		graph.addVertex( YUGO );
		graph.addEdge( YPY, YURI, YUGO, EdgeType.DIRECTED );

		Collection<URI> roots = Arrays.asList( YURI );
		GraphToTreeConverter.Search search = GraphToTreeConverter.Search.DFS;
		Forest<URI, URI> result = GraphToTreeConverter.convert( graph, roots, search );
		assertEquals( 1, result.getTrees().size() );

		Tree<URI, URI> tree = result.getTrees().iterator().next();
		assertEquals( YURI, tree.getRoot() );
		assertEquals( YUGO, tree.getChildren( YURI ).iterator().next() );
	}

	@Test
	public void testPrint() {

		DirectedGraph<URI, URI> graph = new DirectedSparseGraph<>();
		graph.addVertex( YURI );
		graph.addVertex( YUGO );
		graph.addEdge( YPY, YURI, YUGO, EdgeType.DIRECTED );

		Collection<URI> roots = Arrays.asList( YURI );
		GraphToTreeConverter.Search search = GraphToTreeConverter.Search.DFS;
		Forest<URI, URI> result = GraphToTreeConverter.convert( graph, roots, search );

		Logger log = Logger.getLogger( GraphToTreeConverter.class );
		StringWriter stringy = new StringWriter();
		WriterAppender app = new WriterAppender( new SimpleLayout(), stringy );
		log.setLevel( Level.DEBUG );
		log.addAppender( app );
		GraphToTreeConverter.printForest( result );
		String output = stringy.toString().replaceAll( "\\s", "" );
		assertEquals( "DEBUG-http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/YuriDEBUG-http://semoss.va.gov/database/T44889381-85ce-43e3-893d-6267fd480660/Yugo", output );
	}

}
