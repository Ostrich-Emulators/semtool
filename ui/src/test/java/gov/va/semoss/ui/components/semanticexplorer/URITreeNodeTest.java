package gov.va.semoss.ui.components.semanticexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
*
* @author john
*/
public class URITreeNodeTest {
	ValueFactoryImpl factory;
	
	public URITreeNodeTest() {}
	
	@Before
	public void setUp() {
		factory = new ValueFactoryImpl();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testInstantiatesWithNoExceptions() {
		URITreeNode utn = new URITreeNode(new URIImpl("http://va.gov/ontologies/vcamp#Check-In_Patient"), false);
		assertNotNull( utn );
	}

	@Test
	public void testShowLabel() {
		URI userObject = new URIImpl("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal labelValue = factory.createLiteral("Check In Patient");

		URITreeNode utn = new URITreeNode(userObject, labelValue, true);
		assertEquals( utn.toString(), "Check In Patient" );
	}

	@Test
	public void testShowURILocalName() {
		URI userObject = new URIImpl("http://va.gov/ontologies/vcamp#Check-In_Patient");

		URITreeNode utn = new URITreeNode(userObject, false);
		assertEquals( utn.toString(), "Check-In_Patient" );
	}

	@Test
	public void testShowValueStringValue() {
		Literal value = factory.createLiteral("Check In Patient");

		URITreeNode utn = new URITreeNode(value, value, false);
		assertEquals( utn.toString(), "Check In Patient" );
	}
}