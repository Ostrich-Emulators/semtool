/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class StatementSorterTest {

	public StatementSorterTest() {
	}

	@Test
	public void testCompare1() {
		Statement o1 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "a" ) );
		Statement o2 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "b" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result < 0 );
	}

	@Test
	public void testCompare2() {
		Statement o1 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "a" ) );
		Statement o2 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result > 0 );
	}

	@Test
	public void testCompare3() {
		Statement o1 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "A" ) );
		Statement o2 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertEquals( 0, result );
	}

	@Test
	public void testCompare4() {
		Statement o1 = new StatementImpl( RDFS.LABEL, RDFS.CLASS, new LiteralImpl( "A" ) );
		Statement o2 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result < 0 );
	}

	@Test
	public void testCompare5() {
		Statement o1 = new StatementImpl( RDFS.CLASS, RDFS.LABEL, new LiteralImpl( "A" ) );
		Statement o2 = new StatementImpl( RDFS.LABEL, RDFS.LABEL, new LiteralImpl( "A" ) );
		StatementSorter instance = new StatementSorter();
		int result = instance.compare( o1, o2 );
		assertTrue( result < 0 );
	}
}
