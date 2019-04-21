/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 *
 * @author ryan
 */
public class LoadingSheetDataTest {

	public LoadingSheetDataTest() {
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
	public void testRows() {
	}

	@Test
	public void testHasErrors() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "sbj", "obj", "relname" );
		LoadingNodeAndPropertyValues nap = lsd.add( "subject", "object" );
		nap.setSubjectIsError( true );
		assertTrue( lsd.hasErrors() );

		nap.setSubjectIsError( false );
		assertFalse( lsd.hasErrors() );
	}

	@Test
	public void testRemove() {
	}

	@Test
	public void testRemoveAll() {
	}

	@Test
	public void testSetPropertyIsError() {
		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "tabname", "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		lsd.setPropertyIsError( "xx", false );
		assertFalse( lsd.propertyIsError( "xx" ) );

		lsd.addProperty( "yy" );
		lsd.setPropertyIsError( "yy", true );
		assertTrue( lsd.propertyIsError( "yy" ) );
	}

	@Test
	public void testHasModelErrors() {
		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "tabname", "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		lsd.setPropertyIsError( "xx", true );
		assertTrue( lsd.hasModelErrors() );
	}

	@Test
	public void testHasModelErrors2() {
		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "tabname", "sbj", "obj", "relname" );
		assertFalse( lsd.hasModelErrors() );
	}

	@Test
	public void testHasModelErrors3() {
		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "tabname", "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		lsd.setPropertyIsError( "xx", false );
		lsd.setSubjectTypeIsError( true );
		assertTrue( lsd.hasModelErrors() );
	}

	@Test
	public void testSetPropertyDataType() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperty( "one", XMLSchema.STRING );
		lsd.addProperty( "two" );

		assertTrue( lsd.hasPropertyDataType( "one" ) );
		assertFalse( lsd.hasPropertyDataType( "two" ) );
	}

	@Test
	public void testAddProperty_String() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "tabname", "sbj" );
		lsd.addProperty( "xx" );
		lsd.addProperty( "yy" );
		Map<String, Value> props = new HashMap<>();
		ValueFactory vf = new ValueFactoryImpl();
		props.put( "xx", vf.createLiteral( "testval" ) );
		LoadingNodeAndPropertyValues naps = lsd.add( "instance", props );

		Value expected[] = { vf.createLiteral( "instance" ),
			vf.createLiteral( "testval" ), null };

		Value[] vals = naps.convertToValueArray( vf );
		assertArrayEquals( expected, vals );
	}

	@Test
	public void testAddProperty_String2() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "tabname", "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		Map<String, Value> props = new HashMap<>();
		ValueFactory vf = new ValueFactoryImpl();
		props.put( "xx", vf.createLiteral( "testval" ) );
		LoadingNodeAndPropertyValues naps = lsd.add( "instance", "object", props );

		Value expected[] = { vf.createLiteral( "instance" ),
			vf.createLiteral( "object" ), vf.createLiteral( "testval" ) };

		Value[] vals = naps.convertToValueArray( vf );
		assertArrayEquals( expected, vals );
	}

	@Test
	public void testHasProp() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperty( "xx:label" );
		Map<String, Value> props = new HashMap<>();
		ValueFactory vf = new ValueFactoryImpl();
		props.put( "xx:label", vf.createLiteral( "my label" ) );
		LoadingNodeAndPropertyValues nap = lsd.add( "instance", "object", props );

		Map<String, String> nsmap = new HashMap<>();
		nsmap.put( "xx", RDFS.NAMESPACE );
		assertTrue( nap.hasProperty( RDFS.LABEL, nsmap ) );
	}

	@Test
	public void testHasProp2() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperties( Arrays.asList( "xx:label", "yy", "yy:junk" ) );
		Map<String, Value> props = new HashMap<>();
		ValueFactory vf = new ValueFactoryImpl();
		props.put( "xx:label", vf.createLiteral( "my label" ) );
		props.put( "yy", vf.createLiteral( "my y val" ) );
		props.put( "yy:junk", vf.createLiteral( "my junk val" ) );
		LoadingNodeAndPropertyValues nap = lsd.add( "instance", "object", props );

		Map<String, String> nsmap = new HashMap<>();
		nsmap.put( RDFS.PREFIX, RDFS.NAMESPACE );
		nsmap.put( "xx", "http://google.com/" );
		assertFalse( nap.hasProperty( RDFS.LABEL, nsmap ) );
	}

	@Test
	public void testNapString() {
		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "tabname", "sbj", "obj",	"relname" );
		lsd.addProperty( "xx" );
		LoadingNodeAndPropertyValues nap = lsd.add( "instance", "target" );
		nap.setSubjectIsError( true );
		nap.setObjectIsError( true );
		
		assertEquals( "instance<e>;target<e>; relname", nap.toString() );
	}

	@Test
	public void testSetHeadersNode() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "test" );
		lsd.addProperties( Arrays.asList( "col1", "col2" ) );
		List<String> newheads = Arrays.asList( "test", "cola", "colb" );
		lsd.setHeaders( newheads );

		assertEquals( newheads, lsd.getHeaders() );
	}

	@Test
	public void testSetHeadersRel() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "sbj", "obj", "relname" );
		lsd.addProperties( Arrays.asList( "col1", "col2" ) );
		List<String> newheads = Arrays.asList( "sbj", "obj", "cola", "colb" );
		lsd.setHeaders( newheads );
		assertEquals( newheads, lsd.getHeaders() );
	}

	@Test
	public void testChangeHeaders() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperty( "one" );

		List<String> oldheads = lsd.getHeaders();
		List<String> newheads = Arrays.asList( "sbjx", "col", "two" );
		lsd.setHeaders( newheads );
		assertEquals( oldheads, lsd.getHeaders() );

	}

	@Test
	public void testSetHeadersWithError() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		lsd.addProperty( "yy" );
		lsd.addProperty( "zz" );
		lsd.setPropertyIsError( "yy", true );

		List<String> newheads = Arrays.asList( "sbj", "obj", "xx", "yy", "one" );
		lsd.setHeaders( newheads );

		assertTrue( lsd.propertyIsError( "yy" ) );
		assertEquals( "one", lsd.getHeaders().get( 4 ) );
	}

	@Test
	public void testIsRel() {
		assertTrue( LoadingSheetData.relsheet( "sbj", "obj", "relname" ).isRel() );
		LoadingSheetData data = LoadingSheetData.nodesheet( "test" );
		assertFalse( data.isRel() );

		data.setObjectType( "obj" );
		assertTrue( data.isRel() );
	}

	@Test
	public void testIsEmpty() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "test" );
		assertTrue( lsd.isEmpty() );
	}

	@Test
	public void testIsEmpty2() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "test" );
		lsd.add( "test2" );
		assertFalse( lsd.isEmpty() );
	}

	@Test
	public void testToString() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "sbj", "obj", "relname" );
		LoadingNodeAndPropertyValues nap = lsd.add( "subject", "object" );
		nap.setSubjectIsError( true );
		assertEquals( "sbj-relname-obj(rel) with 1 naps", lsd.toString() );
	}

	@Test
	public void testFindPropertyLinks() {
		LoadingSheetData people = LoadingSheetData.nodesheet( "person" );
		people.addProperty( "address" );

		LoadingSheetData lsd = LoadingSheetData.relsheet( "person", "address", "lives" );
		lsd.addProperty( "person" );

		lsd.findPropertyLinks( Arrays.asList( people ) );
		assertTrue( lsd.isLink( "person" ) );

	}

	@Test
	public void testCopyHeadersOf() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		lsd.addProperty( "yy" );
		lsd.setSubjectTypeIsError( true );

		LoadingSheetData newdata = LoadingSheetData.copyHeadersOf( lsd );
		assertEquals( lsd.getHeaders(), newdata.getHeaders() );
		assertTrue( newdata.hasSubjectTypeError() );
		assertFalse( newdata.hasObjectTypeError() );
		assertTrue( newdata.isRel() );
	}

	@Test
	public void testCopyHeadersOf2() {
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "sbj" );
		lsd.addProperty( "xx" );
		lsd.addProperty( "yy" );
		lsd.setSubjectTypeIsError( true );

		LoadingSheetData newdata = LoadingSheetData.copyHeadersOf( lsd );
		assertEquals( lsd.getHeaders(), newdata.getHeaders() );
		assertTrue( newdata.hasSubjectTypeError() );
		assertFalse( newdata.isRel() );
	}
}
