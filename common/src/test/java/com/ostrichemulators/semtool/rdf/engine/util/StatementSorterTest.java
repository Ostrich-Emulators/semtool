/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class StatementSorterTest {
	private static final ValueFactory VF = SimpleValueFactory.getInstance();

	public StatementSorterTest() {
	}

	@Test
	public void testCompare1() {
		Statement o1 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "a" ) );
		Statement o2 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "b" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result < 0 );
	}

	@Test
	public void testCompare2() {
		Statement o1 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "a" ) );
		Statement o2 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result > 0 );
	}

	@Test
	public void testCompare3() {
		Statement o1 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "A" ) );
		Statement o2 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertEquals( 0, result );
	}

	@Test
	public void testCompare4() {
		Statement o1 = VF.createStatement( RDFS.LABEL, RDFS.CLASS, VF.createLiteral( "A" ) );
		Statement o2 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result < 0 );
	}

	@Test
	public void testCompare5() {
		Statement o1 = VF.createStatement( RDFS.CLASS, RDFS.LABEL, VF.createLiteral( "A" ) );
		Statement o2 = VF.createStatement( RDFS.LABEL, RDFS.LABEL, VF.createLiteral( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result < 0 );
	}
}
