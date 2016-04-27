/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.query.util.MetadataQuery;
import com.ostrichemulators.semtool.security.BasicAuthRequestFactory;
import com.ostrichemulators.semtool.web.io.SemossService;
import com.ostrichemulators.semtool.web.io.SemossServiceImpl;
import com.ostrichemulators.semtool.web.io.ServiceClient;
import com.ostrichemulators.semtool.web.io.ServiceClientImpl;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

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
				"http://localhost:8080/semoss/databases/tester/repositories/insights" );
		props.setProperty( AbstractSesameEngine.REPOSITORY_KEY,
				"http://localhost:8080/semoss/databases/tester/repositories/data" );

		BasicAuthRequestFactory authfac = new BasicAuthRequestFactory();
		RestTemplate rt = new RestTemplate( authfac );
		
		
		ServiceClient sc = new ServiceClientImpl( rt );		
		SemossService svc = new SemossServiceImpl( "http://localhost:8080/semoss" );
		sc.setAuthentication( svc, "ryan", "1234".toCharArray() );

		SesameEngine se = new SesameEngine( props );
		MetadataQuery mq = new MetadataQuery();
		se.query( mq );
		se.closeDB();

		assertEquals( 11, mq.asStrings().size() );
	}

	@Test
	public void testGetRawConnection() {
	}

	@Test
	public void testCreateInsightManager() {
	}

}
