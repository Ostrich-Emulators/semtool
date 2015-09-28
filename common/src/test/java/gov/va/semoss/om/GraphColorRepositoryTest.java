/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import gov.va.semoss.util.Constants;
import java.awt.Color;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class GraphColorRepositoryTest {
	
	public GraphColorRepositoryTest() {
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
	public void testInstance() {
	}
	
	@Test
	public void testClearAll() {
	}
	
	@Test
	public void testGetAllColorNames() {
		GraphColorRepository gcr = GraphColorRepository.instance();
		assertEquals( 9, gcr.getAllColorNames().length );
	}
	
	@Test
	public void testGetColorName() {
		GraphColorRepository gcr = GraphColorRepository.instance();
		assertEquals( Constants.YELLOW, gcr.getColorName( new Color( 254, 208, 2 ) ) );
	}
	
	@Test
	public void testGetColorName2() {
		GraphColorRepository gcr = GraphColorRepository.instance();
		assertNull( gcr.getColorName( new Color( 255, 208, 2 ) ) );
	}

	@Test
	public void testUpdateColor() {
		GraphColorRepository gcr = GraphColorRepository.instance();
		gcr.updateColor( RDFS.LABEL, Color.yellow );
		assertEquals( Color.yellow, gcr.getColor( RDFS.LABEL ) );
		gcr.updateColor( RDFS.LABEL, Color.BLUE );
		assertEquals( Color.BLUE, gcr.getColor( RDFS.LABEL ) );
	}
	
}
