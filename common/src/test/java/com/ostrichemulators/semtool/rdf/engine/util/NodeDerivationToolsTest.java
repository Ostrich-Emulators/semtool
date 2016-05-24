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
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class NodeDerivationToolsTest {

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final URI HUMAN = new URIImpl( "http://os-em.com/ontologies/semtool/Human_Being" );
	private static final URI CAR = new URIImpl( "http://os-em.com/ontologies/semtool/Car" );
	private static final URI YUGO
			= new URIImpl( "http://os-em.com/semtool/database/Xced94a65-e9d9-4232-b140-ecda31fbcbca/Yugo" );
	private static final URI YURI
			= new URIImpl( "http://os-em.com/semtool/database/Xced94a65-e9d9-4232-b140-ecda31fbcbca/Yuri" );
	private static InMemorySesameEngine engine;

	public NodeDerivationToolsTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		engine = InMemorySesameEngine.open();
		engine.setBuilders( UriBuilder.getBuilder( "http://os-em.com/semtool/database/Xced94a65-e9d9-4232-b140-ecda31fbcbca/" ),
				UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool" ) );
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
	public void testCreateConceptList() {
		Set<URI> result = new HashSet<>( NodeDerivationTools.createConceptList( engine ) );
		Set<URI> expected = new HashSet<>( Arrays.asList( HUMAN, CAR ) );
		assertEquals( expected, result );
	}

	@Test
	public void testGetPredicatesBetween_3args() {
		Set<URI> expResult = new HashSet<>( Arrays.asList( new URIImpl( "http://os-em.com/ontologies/semtool/Purchased" ) ) );
		Set<URI> result = new HashSet<>( NodeDerivationTools.getPredicatesBetween( HUMAN, CAR, engine ) );
		assertEquals( expResult, result );
	}

	@Test
	public void testGetType() {
		URI result = NodeDerivationTools.getType( YUGO, engine );
		assertEquals( CAR, result );
	}

	@Test
	public void testGetConnectedConceptTypes_3args_1() {
		Set<URI> expResult = new HashSet<>( Arrays.asList( CAR, OWL.CLASS ) );
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
}
