/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class UtilityTest {

	public UtilityTest() {
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

	//@Test
	public void testGetParams() {
	}

	//@Test
	public void testGetParamTypeHash() {
	}

	//@Test
	public void testNormalizeParam() {
	}

	//@Test
	public void testFillParam() {
	}

	//@Test
	public void testGetConceptType() {
	}

	//@Test
	public void testGetQualifiedClassName() {
	}

	@Test
	public void testRound() {
		Map<Double, Double> ones = new HashMap<>();
		ones.put( 1.05d, 1.1d );
		ones.put( 1.04d, 1.0d );
		ones.put( 1.09d, 1.1d );
		ones.put( 1.04999d, 1.0d );
		ones.put( -1.04999d, -1.0d );

		for ( Map.Entry<Double, Double> e : ones.entrySet() ) {
			Double rslt = Utility.round( e.getKey(), 1 );
			assertEquals( "error rounding: " + e.getKey(), e.getValue(), rslt );
		}

		Map<Double, Double> twos = new HashMap<>();
		twos.put( 1.05d, 1.05d );
		twos.put( 1.049d, 1.05d );
		twos.put( 1.123d, 1.12d );
		twos.put( 1.00499d, 1.00d );
		twos.put( -1.04999d, -1.05d );
		twos.put( 13947.04999d, 13947.05d );

		for ( Map.Entry<Double, Double> e : twos.entrySet() ) {
			Double rslt = Utility.round( e.getKey(), 2 );
			assertEquals( "error rounding: " + e.getKey(), e.getValue(), rslt );
		}

		assertFalse( "1.045 check", Utility.round( 1.045d, 2 ) == 1.04d );
		assertTrue( "1.049 check", Utility.round( 1.049d, 0 ) == 1d );
	}

	//@Test
	public void testSciToDollar() {
	}

	//@Test
	public void testGetParamsFromString() {
	}
}
