/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components.semanticexplorer;

import static org.junit.Assert.assertNotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author john
 */
public class SemanticExplorerPanelTest {
	public SemanticExplorerPanelTest() {}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testInstantiatesWithNoExceptions() {
		SemanticExplorerPanel sep = new SemanticExplorerPanel();
		assertNotNull( sep );
	}
}