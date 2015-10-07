/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.rdf.engine.api.ModificationExecutor;
import gov.va.semoss.rdf.engine.api.QueryExecutor;
import gov.va.semoss.rdf.engine.api.UpdateExecutor;
import gov.va.semoss.util.Constants;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class AbstractSesameEngineTest {

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

	@Test( expected = IllegalArgumentException.class )
	public void testGetDefaultName() {
		assertEquals( "test_Questions.properties", AbstractSesameEngine.getDefaultName( Constants.DREAMER, "test" ) );
		assertEquals( "test_Custom_Map.prop", AbstractSesameEngine.getDefaultName( Constants.ONTOLOGY, "test" ) );
		assertEquals( "test_OWL.OWL", AbstractSesameEngine.getDefaultName( Constants.OWLFILE, "test" ) );
		AbstractSesameEngine.getDefaultName( "breaks!", "test" );
	}
}
