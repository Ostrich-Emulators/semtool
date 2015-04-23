/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import gov.va.semoss.model.vocabulary.VAS;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class AbstractFileReaderTest {

	private ImportData id = new ImportData();

	public AbstractFileReaderTest() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testInitNamespaces() {
		ImportData data = new ImportData();
		AbstractFileReader.initNamespaces( data );
		assertEquals( 9, data.getMetadata().getNamespaces().size() );
		assertEquals( VAS.NAMESPACE, data.getMetadata().getNamespaces().get( VAS.PREFIX ) );
	}

	@Test
	public void testCtor() {
		AbstractFileReader ar = new AbstractFileReader() {
		};
		assertNotNull( ar );
	}

	@Test
	public void testGetUriFromRawString1() {
		AbstractFileReader.initNamespaces( id );

		// ryan:int is an invalid datatype, so this is a string
		Value val = AbstractFileReader.getRDFStringValue( "\"16\"^^ryan:int", id,
				new ValueFactoryImpl() );
		assertEquals( "\"16\"^^ryan:int", val.stringValue() );
	}

	@Test
	public void testGetUriFromRawString2() {
		AbstractFileReader.initNamespaces( id );

		// ryan:int is an invalid datatype, so this is a string
		Value val = AbstractFileReader.getRDFStringValue( "\"16\"^^xsd:int", id,
				new ValueFactoryImpl() );
		assertEquals( "16", val.stringValue() );
	}

	@Test
	public void testGetRDFStringValue1() {
		AbstractFileReader.initNamespaces( id );
		Value val = AbstractFileReader.getUriFromRawString( "vas:foobar", id );
		assertEquals( VAS.NAMESPACE + "foobar", val.stringValue() );
	}

	@Test
	public void testGetRDFStringValue2() {
		AbstractFileReader.initNamespaces( id );
		Value val = AbstractFileReader.getUriFromRawString( "vat:foobar", id );
		assertNull( val );
	}

	@Test
	public void testGetRDFStringValue3() {
		AbstractFileReader.initNamespaces( id );
		Value val = AbstractFileReader.getUriFromRawString( "vat:test:foobar", id );
		assertNull( val );
	}

	@Test
	public void testGetRDFStringValue4() {
		AbstractFileReader.initNamespaces( id );
		Value val
				= AbstractFileReader.getUriFromRawString( VAS.NAMESPACE + "foobar", id );
		assertEquals( VAS.NAMESPACE + "foobar", val.stringValue() );
	}

	@Test
	public void testGetRDFStringValue5() {
		AbstractFileReader.initNamespaces( id );
		Value val = AbstractFileReader.getUriFromRawString( "foobar", id );
		assertNull( val );
	}
}
