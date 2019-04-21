package com.ostrichemulators.semtool.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class RDFDatatypeToolsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFigureColumnClassesFromData() {
		List<Value[]> data = new LinkedList<>();
		Value val1 = new LiteralImpl( "A label", XMLSchema.INTEGER );
		Value val2 = new LiteralImpl( "Another label", XMLSchema.BOOLEAN );
		Value val3 = new LiteralImpl( "Yet Another label", XMLSchema.DOUBLE );
		Value[] values = new Value[]{ val1, val2, val3 };
		data.add( values );
		//data.add(e)
		List<Class<?>> classes = RDFDatatypeTools.figureColumnClassesFromData( data, 3 );
		assertEquals( classes.get( 0 ), Integer.class );
		assertEquals( classes.get( 1 ), Boolean.class );
		assertEquals( classes.get( 2 ), Double.class );
	}

	@Test
	public void testGetClassForValue() {
		Value val1 = new LiteralImpl( "A label", XMLSchema.FLOAT );
		Class<?> valClass = RDFDatatypeTools.getClassForValue( val1 );
		assertEquals( valClass, Float.class );
	}

	@Test
	public void testParseXMLDatatype() {
		String id = "someID";
		URI theClass = XMLSchema.DOUBLE;
		String input = "data\"3.0\"" + theClass.stringValue() + "\"";
		Object returnedInstance = RDFDatatypeTools.parseXMLDatatype( input );
		assertEquals( returnedInstance.getClass(), Double.class );
	}

	@Test
	public void testGetDatatype() {
		Value val = new LiteralImpl( "A label", XMLSchema.FLOAT );
		URI uri = RDFDatatypeTools.getDatatype( val );
		assertEquals( XMLSchema.FLOAT, uri );
	}

	@Test
	public void testGetObjectFromValue() {
		Value val1 = new LiteralImpl( "3.0", XMLSchema.FLOAT );
		Object object = RDFDatatypeTools.getObjectFromValue( val1 );
		assertEquals( Float.class, object.getClass() );
	}

	@Test
	public void testIsNumericValue() {
		Value val1 = new LiteralImpl( "A label", XMLSchema.FLOAT );
		Value val2 = new LiteralImpl( "Another label", XMLSchema.BOOLEAN );
		assertTrue( RDFDatatypeTools.isNumericValue( val1 ) );
		assertTrue( !RDFDatatypeTools.isNumericValue( val2 ) );
	}

	@Test
	public void testGetValueFromObject() {
		Integer integer = 3;
		Value val = RDFDatatypeTools.getValueFromObject( integer );
		String s = val.stringValue();
		assertEquals( "3", s );
	}

	@Test
	public void testIsValidUriChars() {
		assertFalse( RDFDatatypeTools.isValidIriChars( "http://www.w3.org/2001/   XMLSchema#float" ) );
	}

	@Test
	public void testGetValueFromDatatypeAndString() {
		Value value = RDFDatatypeTools.getValueFromDatatypeAndString( XMLSchema.DOUBLE, "4.001" );
		assertEquals( value.stringValue(), "4.001" );
	}

	@Test
	public void testGetUriFromRawString() {
		String uriString = "http://www.w3.org/2001/XMLSchema#float";
		Map<String, String> map = new HashMap<>();
		map.put( "rdf", "http://www.w3.org/2001/" );
		URI uri = RDFDatatypeTools.getUriFromRawString( uriString, map );
		assertEquals( uri, XMLSchema.FLOAT );
	}

	@Test
	public void testGetRDFStringValue() {
		ValueFactory vf = new ValueFactoryImpl();
		Map<String, String> map = new HashMap<>();
		map.put( "rdf", "http://www.w3.org/2001/" );
		Value val = RDFDatatypeTools.getRDFStringValue( "3200", map, vf );
		String stringVal = val.stringValue();
		assertEquals( stringVal, "3200" );
	}

}
