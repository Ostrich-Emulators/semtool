package com.ostrichemulators.semtool.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PropComparatorTest {

	private IRI uri1 = null;
	
	private IRI uri2 = null;
	
	private IRI uri3 = null;
	
	private static final PropComparator COMPARATOR = new PropComparator();
	
	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	
	
	@Before
	public void setUp() throws Exception {
		uri1 = FACTORY.createIRI("rdf:is_a");
		uri2 = FACTORY.createIRI("rdf:is_a");
		uri3 = FACTORY.createIRI("rdf:part_of");
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
