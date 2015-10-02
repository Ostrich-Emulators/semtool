package gov.va.semoss.ui.components.semanticexplorer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
*
* @author john
*/
public class PropertyEditorRowTest {
	ValueFactoryImpl factory;

	public PropertyEditorRowTest() {}
	
	@Before
	public void setUp() {
		factory = new ValueFactoryImpl();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testInstantiatesWithNoExceptions() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0d);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.DOUBLE, value);
		assertNotNull( per );
	}

	@Test
	public void testDisplayDouble() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0d);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.DOUBLE, value);
		assertEquals( per.getValueAsDisplayString(), "36.0" );
	}

	@Test
	public void testDisplayFloat() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0f);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.FLOAT, value);
		assertEquals( per.getValueAsDisplayString(), "36.0" );
	}

	@Test
	public void testDisplayInteger() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.INTEGER, value);
		assertEquals( per.getValueAsDisplayString(), "36" );
	}

	@Test
	public void testDisplayBoolean() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(true);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.BOOLEAN, value);
		assertEquals( per.getValueAsDisplayString(), "true" );
	}

	@Test
	public void testDisplayUri() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");

		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.ANYURI, name);
		assertEquals( per.getValueAsDisplayString(), "Check-In_Patient" );
	}

	@Test
	public void testDisplayString() throws Exception {
		URI name = factory.createURI("http://va.gov/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral("A test String.");
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.STRING, value);
		assertEquals( per.getValueAsDisplayString(), "A test String." );
	}
}