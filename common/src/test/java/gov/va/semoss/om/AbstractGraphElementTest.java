/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.om;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;

/**
 *
 * @author ryan
 */
public class AbstractGraphElementTest {

	public AbstractGraphElementTest() {
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
	public void testAddPropertyChangeListener() {
	}

	@Test
	public void testRemovePropertyChangeListener() {
	}

	@Test
	public void testSetColor() {
	}

	@Test
	public void testGetPropertyChangeListeners() {
	}

	@Test
	public void testFireIfPropertyChanged() {
	}

	@Test
	public void testGetColor() {
	}

	@Test
	public void testSetVisible() {
	}

	@Test
	public void testIsVisible() {
	}

	@Test
	public void testSetProperty_String_Object() {
	}

	@Test
	public void testSetLabel() {
	}

	@Test
	public void testGetPropertyKeys() {
	}

	@Test
	public void testGetLabel() {
	}

	@Test
	public void testSetProperty_URI_Object() {
	}

	@Test
	public void testSetValue() {
	}

	@Test
	public void testSetPropHash() {
	}

	@Test
	public void testGetPropHash() {
	}

	@Test
	public void testGetProperty() {
	}

	@Test
	public void testGetValue() {
	}

	@Test
	public void testGetProperties() {
	}

	@Test
	public void testGetValues() {
	}

	@Test
	public void testHasProperty() {
	}

	@Test
	public void testSetURI() {
	}

	@Test
	public void testGetURI() {
	}

	@Test
	public void testGetType() {
	}

	@Test
	public void testSetType() {
	}

	@Test
	public void testRemoveProperty() {
	}

	@Test
	public void testGetDataType() {
	}

	@Test
	public void testMark() {
	}

	@Test
	public void testGetMarkedProperties() {
		TestNode tn = new TestNode();
		tn.mark( RDFS.COMMENT, true ); // comment isn't one of our known properties
		assertFalse( tn.isMarked( RDFS.LABEL ) );

		tn.setProperty( RDFS.COMMENT, "test comment" );
		tn.mark( RDFS.COMMENT, true );
		assertTrue( tn.isMarked( RDFS.COMMENT ) );
	}

	@Test
	public void testIsMarked() {
	}

	@Test
	public void testHashCode() {
	}

	@Test
	public void testEquals() {
	}

	@Test
	public void testToString() {
		assertEquals( "http://va.gov/test; any; test", new TestNode().toString() );
	}

	public class TestNode extends AbstractGraphElement {

		public TestNode() {
			super( new URIImpl( "http://va.gov/test" ) );
		}

		@Override
		public boolean isNode() {
			return true;
		}
	}

}
