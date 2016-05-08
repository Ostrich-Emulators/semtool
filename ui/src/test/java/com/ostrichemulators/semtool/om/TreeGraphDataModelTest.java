/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class TreeGraphDataModelTest {

	private static final File DATAFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab = UriBuilder.getBuilder( "http://semoss.os-em.com/database/T44889381-85ce-43e3-893d-6267fd480660/" );
	private static final UriBuilder owlb = UriBuilder.getBuilder( "http://semoss.org/ontologies/" );
	private static final URI PRICE = owlb.build( "Price" );
	private static final URI YUGO = datab.build( "Yugo" );
	private static final URI YURI = datab.build( "Yuri" );
	private static final URI YPY = datab.build( "Yuri_Purchased_Yugo" );

	private InMemorySesameEngine eng;

	public TreeGraphDataModelTest() {
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
	public void testGetForest() {
		SEMOSSVertex yuri = new SEMOSSVertexImpl( YURI, RDFS.MEMBER, "yuri" );
		SEMOSSVertex yugo = new SEMOSSVertexImpl( YUGO, RDFS.CONTAINER, "yugo" );
		SEMOSSEdge edge = new SEMOSSEdgeImpl( yuri, yugo, RDFS.DOMAIN );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = new DirectedSparseGraph<>();
		graph.addVertex( yuri );
		graph.addVertex( yugo );
		graph.addEdge( edge, yuri, yugo, EdgeType.DIRECTED );

		TreeGraphDataModel tgdm
				= new TreeGraphDataModel( graph, Arrays.asList( yuri ) );
		assertEquals( 2, tgdm.getGraph().getVertexCount() );
		assertEquals( 1, tgdm.getGraph().getEdgeCount() );
	}

	@Test
	public void testPropChangeReal() {
		SEMOSSVertex yuri = new SEMOSSVertexImpl( YURI, RDFS.MEMBER, "yuri" );
		SEMOSSVertex yugo = new SEMOSSVertexImpl( YUGO, RDFS.CONTAINER, "yugo" );
		SEMOSSEdge edge = new SEMOSSEdgeImpl( yuri, yugo, RDFS.DOMAIN );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = new DirectedSparseGraph<>();
		graph.addVertex( yuri );
		graph.addVertex( yugo );
		graph.addEdge( edge, yuri, yugo, EdgeType.DIRECTED );

		int val[] = { 0 };
		TreeGraphDataModel m = new TreeGraphDataModel( graph, Arrays.asList( yuri ) ) {

			@Override
			public void propertyChange( PropertyChangeEvent evt ) {
				super.propertyChange( evt );
				val[0]++;
			}
		};

		yuri.setLabel( "Yuri" );
		yuri.setValue( DCTERMS.ABSTRACT, new LiteralImpl( "one" ) );
		yuri.setValue( DCTERMS.ABSTRACT, new LiteralImpl( "three" ) );
		yuri.setValue( DCTERMS.ABSTRACT, new LiteralImpl( "two" ) );

		edge.setLabel( "edge label" );
		yuri.setValue( DCTERMS.ABSTRACT, new LiteralImpl( "two" ) );

		// 5 changes, but each one calls propertyChange twice (one for the real
		// element, once for the duplicate)
		assertEquals( 10, val[0] );
	}

	@Test
	public void testPropChangeDupe() {
		SEMOSSVertex yuri = new SEMOSSVertexImpl( YURI, RDFS.MEMBER, "yuri" );
		SEMOSSVertex yugo = new SEMOSSVertexImpl( YUGO, RDFS.CONTAINER, "yugo" );
		SEMOSSEdge edge = new SEMOSSEdgeImpl( yuri, yugo, RDFS.DOMAIN );

		DirectedGraph<SEMOSSVertex, SEMOSSEdge> graph = new DirectedSparseGraph<>();
		graph.addVertex( yuri );
		graph.addVertex( yugo );
		graph.addEdge( edge, yuri, yugo, EdgeType.DIRECTED );

		int val[] = { 0 };
		TreeGraphDataModel m = new TreeGraphDataModel( graph, Arrays.asList( yuri ) ) {

			@Override
			public void propertyChange( PropertyChangeEvent evt ) {
				super.propertyChange( evt );
				val[0]++;
			}
		};

		for ( SEMOSSVertex v : m.getDuplicatesOf( yuri ) ) {
			v.setLabel( "Yuri" );
		}

		for ( SEMOSSEdge e : m.getDuplicatesOf( edge ) ) {
			e.setLabel( "edge label" );
		}

		assertEquals( 2, val[0] );
	}
}
