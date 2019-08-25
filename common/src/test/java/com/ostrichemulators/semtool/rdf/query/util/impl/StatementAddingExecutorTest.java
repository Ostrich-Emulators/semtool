/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util.impl;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

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
		InMemorySesameEngine eng = InMemorySesameEngine.open();
		StatementAddingExecutor sae = new StatementAddingExecutor();

		Model before = eng.toModel();
		sae.addStatement( SimpleValueFactory.getInstance().createStatement( RDFS.DOMAIN, RDFS.LABEL,
				SimpleValueFactory.getInstance().createLiteral( "label" ) ) );
		eng.execute( sae );
		Model after = eng.toModel();
		eng.closeDB();
		assertTrue( ( before.size() + 1 ) == after.size() );
	}

	@Test
	public void testExec2() throws Exception {
		InMemorySesameEngine eng = InMemorySesameEngine.open();
		StatementAddingExecutor sae = new StatementAddingExecutor(
				Arrays.asList( SimpleValueFactory.getInstance().createStatement( RDFS.DOMAIN, RDFS.LABEL,
						SimpleValueFactory.getInstance().createLiteral( "label" ) ) ), true );

		Model before = eng.toModel();
		eng.execute( sae );
		Model after = eng.toModel();
		eng.closeDB();
		assertTrue( ( before.size() + 1 ) == after.size() );
	}

	@Test
	public void testExec3() throws Exception {
		InMemorySesameEngine eng = InMemorySesameEngine.open();
		StatementAddingExecutor sae = new StatementAddingExecutor(
				Arrays.asList( SimpleValueFactory.getInstance().createStatement( RDFS.DOMAIN, RDFS.LABEL,
				SimpleValueFactory.getInstance().createLiteral( "label" ) ) ), true );
		sae.resetStatements( SimpleValueFactory.getInstance().createStatement( RDFS.DOMAIN, RDFS.LABEL,
				SimpleValueFactory.getInstance().createLiteral( "tester" ) ) );

		Model before = eng.toModel();
		eng.execute( sae );
		Model after = eng.toModel();
		eng.closeDB();
		assertTrue( ( before.size() + 1 ) == after.size() );

		assertTrue( after.contains( RDFS.DOMAIN, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "tester" ) ) );
		assertFalse( after.contains( RDFS.DOMAIN, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "label" ) ) );
	}

}
