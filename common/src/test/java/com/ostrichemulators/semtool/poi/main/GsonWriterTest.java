/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import com.ostrichemulators.semtool.poi.main.GsonWriter;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 *
 * @author ryan
 */
public class GsonWriterTest {

	private ImportData data;

	public GsonWriterTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		LoadingSheetData rels
				= LoadingSheetData.relsheet( "Human Being", "Car", "Purchased" );
		rels.addProperties( Arrays.asList( "Price", "Date" ) );

		LoadingSheetData nodes = LoadingSheetData.nodesheet( "Human Being" );
		nodes.addProperties( Arrays.asList( "First Name", "Last Name" ) );

		data = new ImportData();
		data.add( rels );
		data.add( nodes );

		ValueFactory vf = new ValueFactoryImpl();
		Map<String, Value> props = new HashMap<>();
		props.put( "Price", vf.createLiteral( "3000 USD" ) );
		rels.add( "Yuri", "Yugo", props );
		rels.add( "Yuri", "Pinto" );

		Map<String, Value> hprop = new HashMap<>();
		hprop.put( "First Name", vf.createLiteral( "Yuri" ) );
		hprop.put( "Last Name", vf.createLiteral( "Gagarin" ) );
		nodes.add( "Yuri", hprop );
	}

	@After
	public void tearDown() {
		data.release();
	}

	@Test
	public void testGetGraph1() {
		Graph g = GsonWriter.getGraph( data );
		int vsize = 0;
		int esize = 0;
		for ( Vertex v : g.getVertices() ) {
			vsize++;
		}
		for ( Edge e : g.getEdges() ) {
			esize++;
		}

		assertEquals( 3, vsize );
		assertEquals( 2, esize );
	}

	@Test
	public void testGetGraphRelsOnly() {
		LoadingSheetData rels
				= LoadingSheetData.relsheet( "Human Being", "Car", "Purchased" );
		rels.addProperties( Arrays.asList( "Price", "Date" ) );
		ValueFactory vf = new ValueFactoryImpl();
		Map<String, Value> props = new HashMap<>();
		props.put( "Price", vf.createLiteral( "3000 USD" ) );
		rels.add( "Yuri", "Yugo", props );
		rels.add( "Yuri", "Pinto" );

		Graph g = GsonWriter.getGraph( data );
		int vsize = 0;
		int esize = 0;
		for ( Vertex v : g.getVertices() ) {
			vsize++;
		}
		for ( Edge e : g.getEdges() ) {
			esize++;
		}

		assertEquals( 3, vsize );
		assertEquals( 2, esize );
	}

}
