/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.poi.main;

import com.ostrichemulators.semtool.util.UriBuilder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan
 */
public class ImportMetadataTest {

	public ImportMetadataTest() {
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
	public void testSetNamespaces() {
	}

	@Test
	public void testAddNamespaces() {
	}

	@Test
	public void testSetNamespace() {
	}

	@Test
	public void testAdd1() {
		ImportMetadata im = new ImportMetadata();
		im.add( "one", "two", "three" );
		assertEquals( 1, im.getStatements().size() );
	}

	@Test( expected = NullPointerException.class )
	public void testAdd2() {
		ImportMetadata im = new ImportMetadata();
		im.add( "one", "two", null );
	}

	@Test( expected = NullPointerException.class )
	public void testAdd3() {
		ImportMetadata im = new ImportMetadata();
		im.add( "one", null, "" );
	}

	@Test( expected = NullPointerException.class )
	public void testAdd4() {
		ImportMetadata im = new ImportMetadata();
		im.add( null, "one", "two" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testAdd5() {
		ImportMetadata im = new ImportMetadata();
		im.add( "", "one", "two" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testAdd6() {
		ImportMetadata im = new ImportMetadata();
		im.add( "one", "", "two" );
	}

	@Test
	public void testAdd7() {
		ImportMetadata im = new ImportMetadata();
		im.add( "one", "one", "" );
		assertEquals( 1, im.getStatements().size() );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testAddAll1() {
		ImportMetadata im = new ImportMetadata();
		List<String[]> strs = new ArrayList<>();
		strs.add( new String[0] );
		im.addAll( strs );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testAddAll2() {
		ImportMetadata im = new ImportMetadata();
		List<String[]> strs = new ArrayList<>();
		strs.add( new String[4] );
		im.addAll( strs );
	}

	@Test
	public void testClearStatements() {
		ImportMetadata im = new ImportMetadata();
		List<String[]> strs = new ArrayList<>();
		strs.add( new String[]{ "one", "two", "three" } );
		im.addAll( strs );

		strs.clear();
		strs.add( new String[]{ "onex", "twox", "threex" } );
		im.setStatements( strs );

		assertEquals( 1, im.getStatements().size() );
		assertEquals( "onex", im.getStatements().iterator().next()[0] );
	}

	@Test
	public void testSetAll() {
		ImportMetadata im = new ImportMetadata();
		List<String[]> strs = new ArrayList<>();
		strs.add( new String[]{ "one", "two", "three" } );
		im.addAll( strs );

		ImportMetadata data = new ImportMetadata();
		data.setAll( im );

		assertEquals( data.getStatements().iterator().next()[0],
				im.getStatements().iterator().next()[0] );
	}

	@Test
	public void testCtor() {
		IRI u = SimpleValueFactory.getInstance().createIRI( "http://va.gov/test" );
		UriBuilder b = UriBuilder.getBuilder( u );
		ImportMetadata im = new ImportMetadata( u, b, b );

		assertEquals( u, im.getBase() );
		assertEquals( b, im.getDataBuilder() );
		assertEquals( b, im.getSchemaBuilder() );
	}

	@Test
	public void testSet1() {
		String uri = "http://va.gov/test";
		
		ImportMetadata im = new ImportMetadata();
		im.setSchemaBuilder( uri );
		assertEquals( uri, im.getSchemaBuilder().toString() );

		im.setSchemaBuilder( null );
		assertNull( im.getSchemaBuilder() );	
	}

	@Test
	public void testSet2() {
		String uri = "http://va.gov/test";
		
		ImportMetadata im = new ImportMetadata();
		im.setDataBuilder( uri );
		assertEquals( uri, im.getDataBuilder().toString() );

		im.setDataBuilder( null );
		assertNull( im.getDataBuilder() );	
	}
}
