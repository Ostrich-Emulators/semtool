package gov.va.semoss.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiMapTester {
	
	private MultiMap<String, String> map;

	@Before
	public void setUp() throws Exception {
		map = new MultiMap<String, String>();
	}

	@After
	public void tearDown() throws Exception {
		
	}


	@Test
	public void testAdd() {
		ArrayList<String> values = new ArrayList<String>();
		values.add("a");
		values.add("b");
		map.put("1", values);
		List<String> returnedValues = map.get("1");
		assertEquals(returnedValues.size(), values.size());
		assertEquals(returnedValues, values);
	}

	@Test
	public void testGetNN() {
		List<String> returnedValues = map.getNN("2");
		assertNotNull(returnedValues);
	}

	@Test
	public void testFlip() {
		ArrayList<String> values = new ArrayList<String>();
		values.add("c");
		values.add("d");
		map.put("2", values);
		MultiMap<List<String>, String> flipped = MultiMap.flip( map );
		List<String> returnedValues = flipped.get(values);
		assertTrue(returnedValues.contains("2"));
	}

	
	public void testLossyflip() {
		ArrayList<String> values = new ArrayList<String>();
		values.add("e");
		values.add("f");
		map.put("3", values);
		MultiMap<List<String>, String> flipped = MultiMap.flip( map );
		List<String> returnedValues = flipped.get(values);
		assertTrue(returnedValues.contains("3"));
	}

}
