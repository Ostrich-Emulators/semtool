package com.ostrichemulators.semtool.ui.components.semanticexplorer;

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
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0d);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.DOUBLE, value);
		assertNotNull( per );
	}

	@Test
	public void testDisplayDouble() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0d);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.DOUBLE, value);
		assertEquals("36.0", per.getValueAsDisplayString() );
	}

	@Test
	public void testDisplayFloat() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0f);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.FLOAT, value);
		assertEquals( "36.0", per.getValueAsDisplayString() );
	}

	@Test
	public void testDisplayInteger() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.INTEGER, value);
		assertEquals( "36", per.getValueAsDisplayString() );
	}

	@Test
	public void testDisplayBoolean() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(true);
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.BOOLEAN, value);
		assertEquals( "true", per.getValueAsDisplayString() );
	}

	@Test
	public void testDisplayUri() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");

		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.ANYURI, name);
		assertEquals( "Check-In_Patient", per.getValueAsDisplayString() );
	}

	@Test
	public void testDisplayString() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral("A test String.");
		
		PropertyEditorRow per = new PropertyEditorRow(name, XMLSchema.STRING, value);
		assertEquals( "A test String.", per.getValueAsDisplayString() );
	}
}