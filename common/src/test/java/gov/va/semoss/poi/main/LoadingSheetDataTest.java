/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.vocabulary.XMLSchema;

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
	public void testIterator() {
	}

	@Test
	public void testHasSubjectTypeError() {
	}

	@Test
	public void testSetSubjectTypeIsError() {
	}

	@Test
	public void testHasObjectTypeError() {
	}

	@Test
	public void testSetObjectTypeIsError() {
	}

	@Test
	public void testHasRelationError() {
	}

	@Test
	public void testSetRelationIsError() {
	}

	@Test
	public void testSetPropertyIsError() {
		LoadingSheetData lsd = LoadingSheetData.relsheet( "sbj", "obj", "relname" );
		lsd.addProperty( "xx" );
		lsd.setPropertyIsError( "xx", false );
		assertFalse( lsd.propertyIsError( "xx" ) );

		lsd.addProperty( "yy" );
		lsd.setPropertyIsError( "yy", true );
		assertTrue( lsd.propertyIsError( "yy" ) );
	}

	@Test
	public void testSetSubjectType() {
	}

	@Test
	public void testSetObjectType() {
	}

	@Test
	public void testSetRelname() {
	}

	@Test
	public void testPropertyIsError() {
	}

	@Test
	public void testHasModelErrors() {
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
	public void testGetPropertyDataType() {
	}

	@Test
	public void testGetObjectType() {
	}

	@Test
	public void testGetRelname() {
	}

	@Test
	public void testGetName() {
	}

	@Test
	public void testAddProperty_String() {
	}

	@Test
	public void testAddProperty_String_URI() {
	}

	@Test
	public void testGetProperties() {
	}

	@Test
	public void testHasProperties() {
	}

	@Test
	public void testAddProperties() {
	}

	@Test
	public void testRelease() {
	}

	@Test
	public void testFinishLoading() {
	}

	@Test
	public void testClear() {
	}

	@Test
	public void testSetProperties() {
	}

	@Test
	public void testGetPropertiesAndDataTypes() {
	}

	@Test
	public void testGetSubjectType() {
	}

	@Test
	public void testCacheNapLabel() {
	}

	@Test
	public void testIsNapLabelCached() {
	}

	@Test
	public void testIsPropLabelCached() {
	}

	@Test
	public void testGetData() {
	}

	@Test
	public void testGetDataRef() {
	}

	@Test
	public void testSetData() {
	}

	@Test
	public void testCommit() {
	}

	@Test
	public void testAdded() {
	}

	@Test
	public void testAdd_LoadingSheetDataLoadingNodeAndPropertyValues() {
	}

	@Test
	public void testAdd_String() {
	}

	@Test
	public void testAdd_String_Map() {
	}

	@Test
	public void testAdd_String_String() {
	}

	@Test
	public void testAdd_3args() {
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
	public void testIsLink() {
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
