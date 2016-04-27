package com.ostrichemulators.semtool.util;

import static org.junit.Assert.*;

import java.awt.Shape;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DIHelperTest {

	private DIHelper helper;
	
	@Before
	public void setUp() throws Exception {
		helper = DIHelper.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		helper = null;
	}

	@Test
	public void getHex() {
		Shape shape = helper.createHex(10d);
		assertNotNull(shape);
	}
	
	@Test
	public void getHexL() {
		Shape shape = helper.createHexL();
		assertNotNull(shape);
	}
	
	@Test
	public void getPent() {
		Shape shape = helper.createPent(10d);
		assertNotNull(shape);
	}
	
	@Test
	public void getPentL() {
		Shape shape = helper.createPentL();
		assertNotNull(shape);
	}

	@Test
	public void getRhombus() {
		Shape shape = helper.createRhombus(10d);
		assertNotNull(shape);
	}
	
	@Test
	public void getRhombusL() {
		Shape shape = helper.createRhombusL();
		assertNotNull(shape);
	}
	
	@Test
	public void getStar() {
		Shape shape = helper.createStar();
		assertNotNull(shape);
	}
	
	@Test
	public void getStarL() {
		Shape shape = helper.createStarL();
		assertNotNull(shape);
	}
	
	@Test
	public void getTriangle() {
		Shape shape = helper.createUpTriangle(10d);
		assertNotNull(shape);
	}
	
	@Test
	public void getTriangleL() {
		Shape shape = helper.createStarL();
		assertNotNull(shape);
	}
}
