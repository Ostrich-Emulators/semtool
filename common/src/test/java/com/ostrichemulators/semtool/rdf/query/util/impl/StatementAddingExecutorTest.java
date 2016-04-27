/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.query.util.impl.StatementAddingExecutor;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class StatementAddingExecutorTest {

	public StatementAddingExecutorTest() {
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
	public void testExec() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		StatementAddingExecutor sae = new StatementAddingExecutor();

		Model before = eng.toModel();
		sae.addStatement( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL,
				new LiteralImpl( "label" ) ) );
		eng.execute( sae );
		Model after = eng.toModel();
		eng.closeDB();
		assertTrue( ( before.size() + 1 ) == after.size() );
	}

	@Test
	public void testExec2() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		StatementAddingExecutor sae = new StatementAddingExecutor(
				Arrays.asList( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL,
								new LiteralImpl( "label" ) ) ), true );

		Model before = eng.toModel();
		eng.execute( sae );
		Model after = eng.toModel();
		eng.closeDB();
		assertTrue( ( before.size() + 1 ) == after.size() );
	}

	@Test
	public void testExec3() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		StatementAddingExecutor sae = new StatementAddingExecutor(
				Arrays.asList( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL,
								new LiteralImpl( "label" ) ) ), true );
		sae.resetStatements( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL,
				new LiteralImpl( "tester" ) ) );

		Model before = eng.toModel();
		eng.execute( sae );
		Model after = eng.toModel();
		eng.closeDB();
		assertTrue( ( before.size() + 1 ) == after.size() );

		assertTrue( after.contains( RDFS.DOMAIN, RDFS.LABEL, new LiteralImpl( "tester" ) ) );
		assertFalse( after.contains( RDFS.DOMAIN, RDFS.LABEL, new LiteralImpl( "label" ) ) );
	}

}
