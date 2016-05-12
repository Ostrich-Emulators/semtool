package com.ostrichemulators.semtool.ui.components.semanticexplorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
*
* @author john
*/
public class InstancePropertyTableModelTest {
	ValueFactoryImpl factory;

	public InstancePropertyTableModelTest() {}
	
	@Before
	public void setUp() {
		factory = new ValueFactoryImpl();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testInstantiatesWithNoExceptions() {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0f);
		
		Value testValues[] = { name, value };
		ArrayList<Value[]> properties = new ArrayList<Value[]>();
		properties.add(testValues);

		InstancePropertyTableModel utn = new InstancePropertyTableModel(properties, null);
		assertNotNull( utn );
	}
	
	@Test
	public void testGetNameLocalName() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0f);
		
		Value testValues[] = { name, value };
		ArrayList<Value[]> properties = new ArrayList<Value[]>();
		properties.add(testValues);

		InstancePropertyTableModel utn = new InstancePropertyTableModel(properties, null);
		assertEquals( "Check-In_Patient", utn.getValueAt(0,0) );
	}
	
	@Test
	public void testGetDatatype() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0f);
		
		Value testValues[] = { name, value };
		ArrayList<Value[]> properties = new ArrayList<Value[]>();
		properties.add(testValues);

		InstancePropertyTableModel utn = new InstancePropertyTableModel(properties, null);
		assertEquals( "float", utn.getValueAt(0,1) );
	}
	
	@Test
	public void testGetValueAsDisplayString() throws Exception {
		URI name = factory.createURI("http://os-em.com/ontologies/vcamp#Check-In_Patient");
		Literal value = factory.createLiteral(36.0f);
		
		Value testValues[] = { name, value };
		ArrayList<Value[]> properties = new ArrayList<Value[]>();
		properties.add(testValues);

		InstancePropertyTableModel utn = new InstancePropertyTableModel(properties, null);
		assertEquals( "36.0", utn.getValueAt(0,2) );
	}
}