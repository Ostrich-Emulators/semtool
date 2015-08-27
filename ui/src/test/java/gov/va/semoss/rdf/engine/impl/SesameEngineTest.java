/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.rdf.query.util.MetadataQuery;
import java.util.Properties;
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
public class SesameEngineTest {

	public SesameEngineTest() {
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

	//@Test
	public void testCreateRc() throws Exception {
		Properties props = new Properties();
		
		props.setProperty( AbstractSesameEngine.REMOTE_KEY, Boolean.TRUE.toString() );
		props.setProperty( AbstractSesameEngine.INSIGHTS_KEY,
				"http://localhost:8080/semoss/databases/tester/insights" );
		props.setProperty( AbstractSesameEngine.REPOSITORY_KEY,
				"http://localhost:8080/semoss/databases/tester/data" );

		SesameEngine se = new SesameEngine();
		se.openDB( props );
		MetadataQuery mq = new MetadataQuery();
		se.query( mq );
		se.closeDB();
		
		assertEquals( 5, mq.asStrings().size() );
	}

	@Test
	public void testGetRawConnection() {
	}

	@Test
	public void testCreateInsightManager() {
	}

}
