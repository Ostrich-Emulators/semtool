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
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Model;
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
	private static final IRI HUMAN = OWLB.build( "Human_Being" );
	private static final IRI CAR = OWLB.build( "Car" );
	private static final IRI PURCHASE = OWLB.build( "Purchased" );
	private static final IRI YUGO = DATAB.build( "Yugo" );
	private static final IRI YURI = DATAB.build( "Yuri" );
	private static final IRI YPY = DATAB.build( "Yuri_Purchased_Yugo" );
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
		Set<IRI> expResult = new HashSet<>( Arrays.asList( CAR ) );
		Set<IRI> result
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
		Set<IRI> expResult = new HashSet<>( Arrays.asList( CAR ) );
		Set<IRI> result = new HashSet<>( NodeDerivationTools.
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
		final IRI REL = OWLB.getRelationIri().build();
		final IRI EXTRA = OWLB.build( "AnotherRelType" );
		final IRI EXTRAIMPL = DATAB.build( "AnotherRel" );

		final IRI ALAN = DATAB.build( "Alan" );
		final IRI CADILLAC = DATAB.build( "Cadillac" );

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
