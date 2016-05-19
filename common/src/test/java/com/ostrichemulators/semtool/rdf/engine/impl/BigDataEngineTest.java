/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class BigDataEngineTest {

	private static final File JNL = new File( "src/test/resources/test.jnl" );
	private static final File INSIGHTS = new File( "src/test/resources/insmgr.data-source.ttl" );
	private BigDataEngine eng;
	private File jnl;

	public BigDataEngineTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		jnl = File.createTempFile( "bde-test", ".jnl" );
		FileUtils.copyFile( JNL, jnl );
		eng = new BigDataEngine( jnl );
	}

	@After
	public void tearDown() {
		eng.closeDB();
		FileUtils.deleteQuietly( jnl );
	}

	@Test
	public void testUpdateInsights() throws Exception {
		InsightManagerImpl im = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( INSIGHTS, im );

		assertEquals( 1, eng.getInsightManager().getPerspectives().size() );
		eng.updateInsights( im );
		eng.closeDB();
		eng = new BigDataEngine( jnl );
		assertEquals( im.getPerspectives(), eng.getInsightManager().getPerspectives() );
	}
}
