/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class VocabularyRegistryTest {

	public VocabularyRegistryTest() {
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
	public void testRegisterVocabulary() throws Exception {
		VocabularyRegistry.registerVocabulary( "test", new URL( "http://va.gov/test" ), 
				true );
		VocabularyRegistry.registerVocabulary( "test2", new URL( "http://va.gov/test" ), 
				false );
		assertTrue( VocabularyRegistry.isEnabled( "test" ) );
		assertFalse( VocabularyRegistry.isEnabled( "test2" ) );
	}

	@Test
	public void testGetVocabularies_0args() {
	}

	@Test
	public void testGetVocabularies_boolean() throws Exception{
		VocabularyRegistry.registerVocabulary( "test", new URL( "http://va.gov/test" ), 
				true );
		VocabularyRegistry.registerVocabulary( "test2", new URL( "http://va.gov/test" ), 
				false );
		VocabularyRegistry.registerVocabulary( "test3", new URL( "http://va.gov/test" ), 
				false );
		assertEquals( 1, VocabularyRegistry.getVocabularies( true ).size() );
		assertEquals( 2, VocabularyRegistry.getVocabularies( false ).size() );
		
	}
}
