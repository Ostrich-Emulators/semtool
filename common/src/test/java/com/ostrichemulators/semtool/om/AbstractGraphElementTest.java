/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.om;

import com.ostrichemulators.semtool.util.RDFDatatypeTools;
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

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
		tn.setValue( RDFS.COMMENT, RDFDatatypeTools.getValueFromObject( expected ) );
		assertEquals( expected, tn.getValue( RDFS.COMMENT ).stringValue() );
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
		TestNode tn = new TestNode();
		tn.setValue( RDFS.COMMENT, RDFDatatypeTools.getValueFromObject( "test comment" ) );
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
		tn.setValue( RDFS.COMMENT, RDFS.DOMAIN );
		assertEquals( XMLSchema.ANYURI, tn.getDataType( RDFS.COMMENT ) );
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
			super( SimpleValueFactory.getInstance().createIRI( "http://va.gov/test" ) );
		}

		public TestNode( IRI id, IRI type, String label ) {
			super( id, type, label );
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
