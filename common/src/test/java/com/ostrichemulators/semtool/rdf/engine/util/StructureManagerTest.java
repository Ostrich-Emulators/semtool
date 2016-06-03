/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 *
 * @author ryan
 */
public class StructureManagerTest {

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab
			= UriBuilder.getBuilder( "http://os-em.com/semtool/database/Ke42d9335-1c26-475a-96bd-9bde6a2ab5e5/" );
	private static final UriBuilder owlb
			= UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool" );
	private static final URI HUMAN = owlb.build( "Human_Being" );
	private static final URI PURCHASE = owlb.build( "Purchased" );
	private static final URI CAR = owlb.build( "Car" );
	private static final URI DATE = owlb.build( "Date" );
	private static final URI PRICE = owlb.build( "Price" );

	private static final URI YUGO = datab.build( "Yugo" );
	private static final URI YURI = datab.build( "Yuri" );
	private static final URI YPY = datab.build( "Yuri_Purchased_Yugo" );
	private static InMemorySesameEngine engine;
	private StructureManagerImpl structman;

	public StructureManagerTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		Properties props = new Properties();
		props.setProperty( InMemorySesameEngine.INFER, Boolean.TRUE.toString() );
		engine = InMemorySesameEngine.open( props );
		engine.setBuilders( datab, owlb );
		engine.getRawConnection().begin();
		engine.getRawConnection().add( LOADFILE, null, RDFFormat.NTRIPLES );
		engine.getRawConnection().commit();
	}

	@AfterClass
	public static void tearDownClass() {
		engine.closeDB();
	}

	@Before
	public void setUp() {
		structman = new StructureManagerImpl( engine );
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGetLinksBetween1() {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Model model = structman.getLinksBetween( HUMAN, CAR );
		assertEquals( expResult, model.predicates() );
	}

	@Test
	public void testGetLinksBetween2() {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Model model = structman.getLinksBetween( YURI, YUGO );
		assertEquals( expResult, model.predicates() );
	}

	@Test
	public void testGetConnectedConceptTypes1() throws Exception {
		Set<Value> expResult = new HashSet<>( Arrays.asList( CAR ) );
		Model model = structman.getConnectedConceptTypes( Arrays.asList( YURI ) );
		TurtleWriter tw = new TurtleWriter( System.out );
		tw.startRDF();
		for ( Statement s : model ) {
			tw.handleStatement( s );
		}
		tw.endRDF();
		assertEquals( expResult, model.objects() );
	}

	@Test
	public void testGetConnectedConceptTypes2() throws Exception {
		Set<Value> expResult = new HashSet<>( Arrays.asList( HUMAN ) );
		Model model = structman.getConnectedConceptTypes( Arrays.asList( YUGO ) );
		TurtleWriter tw = new TurtleWriter( System.out );
		tw.startRDF();
		for ( Statement s : model ) {
			tw.handleStatement( s );
		}
		tw.endRDF();

		assertEquals( expResult, model.subjects() );
		assertEquals( new HashSet<>( Arrays.asList( CAR ) ), model.objects() );
	}

	@Test
	public void testTopLevelRelsGeneric() throws Exception {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Set<URI> result = structman.getTopLevelRelations( expResult );
		assertEquals( expResult, result );
	}

	@Test
	public void testTopLevelRelsSpecific() throws Exception {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Set<URI> result = structman.getTopLevelRelations( Arrays.asList( YPY ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testTopLevelRelsNull() throws Exception {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Set<URI> result = structman.getTopLevelRelations( null );
		assertEquals( expResult, result );
	}

	@Test
	public void testTopLevelRelsEmpty() throws Exception {
		assertTrue( structman.getTopLevelRelations( new ArrayList<>() ).isEmpty() );
	}

	@Test
	public void testGetProperties1() throws Exception {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PRICE, DATE ) );
		Set<URI> result = structman.getPropertiesOf( YPY );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetProperties2() throws Exception {
		Set<URI> expResult = new HashSet<>( Arrays.asList( PRICE, DATE ) );
		Set<URI> result = structman.getPropertiesOf( YPY );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetProperties3() throws Exception {
		Set<URI> expResult = new HashSet<>();
		Set<URI> result = structman.getPropertiesOf( owlb.build( "junker" ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetProperties4() throws Exception {
		Set<URI> expResult = new HashSet<>( Arrays.asList( owlb.build( "First_Name" ),
				owlb.build( "Last_Name" ) ) );
		Set<URI> result = structman.getPropertiesOf( YURI );
		assertEquals( expResult, result );

		result = structman.getPropertiesOf( HUMAN );
		assertEquals( expResult, result );
	}
}
