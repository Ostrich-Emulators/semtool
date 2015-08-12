/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import gov.va.semoss.util.MultiMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class MultiMapTest {

	public MultiMapTest() {
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
	public void testFlip() {
		Map<Integer, String> map = new HashMap<>();
		map.put( 1, "A" );
		map.put( 2, "A" );

		MultiMap<String, Integer> flipped = MultiMap.flip( map );
		assertEquals( 2, flipped.get( "A" ).size() );
		assertNull( flipped.get( "B" ) );
	}

	@Test
	public void testLossyflip() {
		Map<Integer, String> map = new HashMap<>();
		map.put( 1, "A" );
		map.put( 2, "A" );

		Map<String, Integer> flipped = MultiMap.lossyflip( map );
		assertEquals( Integer.valueOf( 2 ), flipped.get( "A" ) );
	}
}
