package com.ostrichemulators.semtool.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

public class PropComparatorTest {

	private URI uri1 = null;
	
	private URI uri2 = null;
	
	private URI uri3 = null;
	
	private static final PropComparator COMPARATOR = new PropComparator();
	
	private static final ValueFactory FACTORY = new ValueFactoryImpl();
	
	
	@Before
	public void setUp() throws Exception {
		uri1 = FACTORY.createURI("rdf:is_a");
		uri2 = FACTORY.createURI("rdf:is_a");
		uri3 = FACTORY.createURI("rdf:part_of");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testComparePositive() {
		int result = COMPARATOR.compare(uri1, uri2);
		assertEquals(result, 0);
	}
	
	@Test
	public void testCompareNegative() {
		int result = COMPARATOR.compare(uri2, uri3);
		assert(result < 0);
	}

}
