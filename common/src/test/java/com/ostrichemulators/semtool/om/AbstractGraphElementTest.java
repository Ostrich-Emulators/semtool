/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import java.awt.Color;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

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
	public void testSetColor1() {
		TestNode tn = new TestNode();
		tn.setColor( Color.yellow );
		assertEquals( Color.yellow, tn.getColor() );
		tn.setColor( null );
		assertEquals( new Color( 255, 255, 255, 0 ), tn.getColor() );
	}

	@Test
	public void testGetPropertyChangeListeners() {
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
	public void testSetProperty_URI_Object() {
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
		TestNode tn = new TestNode();
		String expected = "a comment";
		tn.setProperty( RDFS.COMMENT, expected );
		assertEquals( expected, tn.getProperties().get( RDFS.COMMENT ) );
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
	public void testCtors() {
		TestNode tn = new TestNode( RDFS.LABEL, RDFS.DOMAIN, "label" );
		tn.setProperty( RDFS.COMMENT, "test comment" );
		Color c = new Color( 255, 255, 255, 0 );
		assertEquals( c, tn.getColor() );
	}

	@Test
	public void testGetDataType() {
		TestNode tn = new TestNode();
		tn.setProperty( RDFS.COMMENT, "test comment" );
		assertEquals( XMLSchema.STRING, tn.getDataType( RDFS.COMMENT ) );
	}

	@Test
	public void testGetDataType1() {
		TestNode tn = new TestNode();
		assertNull( tn.getDataType( RDFS.CONTAINER ) );
	}

	@Test
	public void testGetDataType2() {
		TestNode tn = new TestNode();
		tn.setProperty( RDFS.COMMENT, RDFS.DOMAIN );
		assertEquals( XMLSchema.ANYURI, tn.getDataType( RDFS.COMMENT ) );
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

		public TestNode( URI id, URI type, String label ) {
			super( id, type, label );
		}

		public TestNode( URI id, URI type, String label, Color col ) {
			super( id, type, label, col );
		}

		@Override
		public boolean isNode() {
			return true;
		}

		@Override
		public GraphElement duplicate() {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}
	}

}
