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
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class SemtoolStructureManagerImplTest {

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab
			= UriBuilder.getBuilder( "http://os-em.com/semtool/database/Ke42d9335-1c26-475a-96bd-9bde6a2ab5e5/" );
	private static final UriBuilder owlb
			= UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool" );
	private static final IRI HUMAN = owlb.build( "Human_Being" );
	private static final IRI PURCHASE = owlb.build( "Purchased" );
	private static final IRI CAR = owlb.build( "Car" );
	private static final IRI DATE = owlb.build( "Date" );
	private static final IRI PRICE = owlb.build( "Price" );

	private static final IRI YUGO = datab.build( "Yugo" );
	private static final IRI YURI = datab.build( "Yuri" );
	private static final IRI YPY = datab.build( "Yuri_Purchased_Yugo" );
	private InMemorySesameEngine engine;
	private SemtoolStructureManagerImpl structman;

	public SemtoolStructureManagerImplTest() {
	}

	@Before
	public void setUp() throws Exception {
		engine = InMemorySesameEngine.open( true );
		engine.setBuilders( datab, owlb );
		engine.getRawConnection().begin();
		engine.getRawConnection().add( LOADFILE, null, RDFFormat.NTRIPLES );
		engine.getRawConnection().commit();
		structman = new SemtoolStructureManagerImpl( engine );
	}

	@After
	public void tearDown() {
		engine.closeDB();
	}

	@Test
	public void testGetLinksBetween1() {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Model model = structman.getLinksBetween( HUMAN, CAR );
		assertEquals( expResult, model.predicates() );
	}

	@Test
	public void testGetLinksBetween2() {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Model model = structman.getLinksBetween( YURI, YUGO );
		assertEquals( expResult, model.predicates() );
	}

	@Test
	public void testGetConnectedConceptTypes1() throws Exception {
		Set<Value> expResult = new HashSet<>( Arrays.asList( CAR ) );
		Model model = structman.getConnectedConceptTypes( Arrays.asList( YURI ) );
		assertEquals( expResult, model.objects() );
	}

	@Test
	public void testGetConnectedConceptTypes2() throws Exception {
		Set<Value> expResult = new HashSet<>( Arrays.asList( HUMAN ) );
		Model model = structman.getConnectedConceptTypes( Arrays.asList( YUGO ) );
		assertEquals( expResult, model.subjects() );
		assertEquals( new HashSet<>( Arrays.asList( CAR ) ), model.objects() );
	}

	@Test
	public void testTopLevelRelsGeneric() throws Exception {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Set<IRI> result = structman.getTopLevelRelations( expResult );
		assertEquals( expResult, result );
	}

	@Test
	public void testTopLevelRelsSpecific() throws Exception {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Set<IRI> result = structman.getTopLevelRelations( Arrays.asList( YPY ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testTopLevelRelsNull() throws Exception {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PURCHASE ) );
		Set<IRI> result = structman.getTopLevelRelations( null );
		assertEquals( expResult, result );
	}

	@Test
	public void testTopLevelRelsEmpty() throws Exception {
		assertTrue( structman.getTopLevelRelations( new ArrayList<>() ).isEmpty() );
	}

	@Test
	public void testGetProperties1() throws Exception {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PRICE, DATE ) );
		Set<IRI> result = structman.getPropertiesOf( YPY );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetProperties2() throws Exception {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( PRICE, DATE ) );
		Set<IRI> result = structman.getPropertiesOf( YPY );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetProperties3() throws Exception {
		Set<IRI> expResult = new HashSet<>();
		Set<IRI> result = structman.getPropertiesOf( owlb.build( "junker" ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetProperties4() throws Exception {
		Set<IRI> expResult = new HashSet<>( Arrays.asList( owlb.build( "First_Name" ),
				owlb.build( "Last_Name" ) ) );
		Set<IRI> result = structman.getPropertiesOf( YURI );
		assertEquals( expResult, result );

		result = structman.getPropertiesOf( HUMAN );
		assertEquals( expResult, result );
	}

	@Test
	public void testRebuild() throws Exception {
		Model old = structman.getModel();
		Model model = structman.rebuild( false );
		assertEquals( old, model );
	}

	@Test
	public void testRebuildSave() throws Exception {
		Model old = structman.getModel();

		// get rid of the old model
		engine.getRawConnection().begin();
		engine.getRawConnection().remove( old );
		engine.getRawConnection().commit();

		assertTrue( structman.getModel().isEmpty() );

		Model m = structman.rebuild( true );
		assertEquals( old, structman.getModel() );
		assertEquals( old, m );
	}

	@Test
	public void testTypeQuery() throws Exception {
		assertEquals( HUMAN, structman.getTopLevelType( YURI ) );
		assertEquals( CAR, structman.getTopLevelType( YUGO ) );
		assertEquals( PURCHASE, structman.getTopLevelType( YPY ) );
	}
}
