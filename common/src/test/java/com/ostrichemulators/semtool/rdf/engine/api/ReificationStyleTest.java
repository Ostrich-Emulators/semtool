/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.api;

import com.ostrichemulators.semtool.rdf.engine.api.ReificationStyle;
import com.ostrichemulators.semtool.model.vocabulary.VAS;
import com.ostrichemulators.semtool.util.Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan
 */
public class ReificationStyleTest {

	public ReificationStyleTest() {
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
	public void testValueOf1() {
		ReificationStyle style = ReificationStyle.valueOf( "RDR" );
		assertEquals( ReificationStyle.RDR, style );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testValueOf2() {
		ReificationStyle style = ReificationStyle.valueOf( "broken" );
		assertEquals( ReificationStyle.RDR, style );
	}

	@Test
	public void testFromUri() {
		ReificationStyle style = ReificationStyle.fromUri( VAS.RDR_Reification );
		assertEquals( ReificationStyle.RDR, style );
	}

	@Test
	public void testFromUri2() {
		ReificationStyle style = ReificationStyle.fromUri( null );
		assertEquals( ReificationStyle.LEGACY, style );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testFromUri3() {
		ReificationStyle style = ReificationStyle.fromUri( Constants.ANYNODE );
		assertEquals( ReificationStyle.RDR, style );
	}

}
