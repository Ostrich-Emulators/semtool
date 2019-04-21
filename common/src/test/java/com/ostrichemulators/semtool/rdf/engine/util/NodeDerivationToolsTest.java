/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class NodeDerivationToolsTest {

	private static final UriBuilder OWLB
			= UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool/" );
	private static final UriBuilder DATAB
			= UriBuilder.getBuilder( "http://os-em.com/semtool/database/Ke42d9335-1c26-475a-96bd-9bde6a2ab5e5/" );

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final URI HUMAN = OWLB.build( "Human_Being" );
	private static final URI CAR = OWLB.build( "Car" );
	private static final URI PURCHASE = OWLB.build( "Purchased" );
	private static final URI YUGO = DATAB.build( "Yugo" );
	private static final URI YURI = DATAB.build( "Yuri" );
	private static final URI YPY = DATAB.build( "Yuri_Purchased_Yugo" );
	private static InMemorySesameEngine engine;

	public NodeDerivationToolsTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		engine = InMemorySesameEngine.open( true );
		engine.setBuilders( DATAB, OWLB );
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
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGetConnectedConceptTypes_3args_1() {
		Set<URI> expResult = new HashSet<>( Arrays.asList( CAR ) );
		Set<URI> result
				= new HashSet<>( NodeDerivationTools.getConnectedConceptTypes( YURI, engine, true ) );
		assertEquals( expResult, result );

		expResult.clear();
		expResult.add( HUMAN );
		result.clear();
		result.addAll( NodeDerivationTools.getConnectedConceptTypes( YUGO, engine, false ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetConnectedConceptTypes_3args_2() {
		Set<URI> expResult = new HashSet<>( Arrays.asList( CAR ) );
		Set<URI> result = new HashSet<>( NodeDerivationTools.
				getConnectedConceptTypes( Arrays.asList( YURI ), engine, true ) );
		assertEquals( expResult, result );

		expResult.clear();
		expResult.add( HUMAN );
		result.clear();
		result.addAll( NodeDerivationTools.getConnectedConceptTypes( Arrays.asList( YUGO ),
				engine, false ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetInstancesNoPropsAvailable() throws Exception {
		// this is just extra stuff that shouldn't be returned in the tests
		final URI REL = OWLB.getRelationIri().build();
		final URI EXTRA = OWLB.build( "AnotherRelType" );
		final URI EXTRAIMPL = DATAB.build( "AnotherRel" );

		final URI ALAN = DATAB.build( "Alan" );
		final URI CADILLAC = DATAB.build( "Cadillac" );

		engine.getRawConnection().add( EXTRA, RDFS.SUBPROPERTYOF, REL );
		engine.getRawConnection().add( EXTRAIMPL, RDF.TYPE, REL );


		Model expected = new LinkedHashModel();
		expected.add( YURI, YPY, YUGO );
		expected.add( ALAN, PURCHASE, CADILLAC );

		Model model = NodeDerivationTools.getInstances( HUMAN, PURCHASE, CAR,
				null, engine );
		assertEquals( expected, model );
	}
}
