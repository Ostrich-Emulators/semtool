/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import static gov.va.semoss.util.RDFDatatypeTools.getRDFStringValue;
import static gov.va.semoss.util.RDFDatatypeTools.getUriFromRawString;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class AbstractEdgeModelerTest {

	public AbstractEdgeModelerTest() {
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
	public void testIsUri1() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		boolean result = AbstractEdgeModeler.isUri( VAS.PREFIX + ":bobo", namespaces );
		assertTrue( result );
	}

	public void testIsUri2() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		boolean result = AbstractEdgeModeler.isUri( "<http://foo.bar/bah/bobo>", namespaces );
		assertTrue( result );
	}

	public void testIsUri3() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		boolean result = AbstractEdgeModeler.isUri( "blah", namespaces );
		assertTrue( !result );
	}

	@Test
	public void testGetUriFromRawString1() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );

		// ryan:int is an invalid datatype, so this is a string
		Value val = getRDFStringValue( "\"16\"^^ryan:int", namespaces,
				new ValueFactoryImpl() );
		assertEquals( "\"16\"^^ryan:int", val.stringValue() );
	}

	@Test
	public void testGetUriFromRawString2() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( XMLSchema.PREFIX, XMLSchema.NAMESPACE );

		// ryan:int is an invalid datatype, so this is a string
		Value val = getRDFStringValue( "\"16\"^^xsd:int", namespaces,
				new ValueFactoryImpl() );
		assertEquals( "16", val.stringValue() );
	}

	@Test
	public void testGetRDFStringValue1() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		Value val = getUriFromRawString( "vas:foobar", namespaces );
		assertEquals( VAS.NAMESPACE + "foobar", val.stringValue() );
	}

	@Test
	public void testGetRDFStringValue2() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		Value val = getUriFromRawString( "vat:foobar", namespaces );
		assertNull( val );
	}

	@Test
	public void testGetRDFStringValue3() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		Value val = getUriFromRawString( "vat:test:foobar", namespaces );
		assertNull( val );
	}

	@Test
	public void testGetRDFStringValue4() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		Value val = getUriFromRawString( VAS.NAMESPACE + "foobar",
				namespaces );
		assertEquals( VAS.NAMESPACE + "foobar", val.stringValue() );
	}

	@Test
	public void testGetRDFStringValue5() {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put( VAS.PREFIX, VAS.NAMESPACE );
		Value val = getUriFromRawString( "foobar", namespaces );
		assertNull( val );
	}

	public class AbstractEdgeModelerImpl extends AbstractEdgeModeler {

		@Override
		public URI addRel( LoadingSheetData.LoadingNodeAndPropertyValues nap, Map<String, String> namespaces, LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public URI addNode( LoadingSheetData.LoadingNodeAndPropertyValues nap, Map<String, String> namespaces, LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void addProperties( URI subject, Map<String, Value> properties, Map<String, String> namespaces, LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public void createMetamodel( ImportData alldata, Map<String, String> namespaces, RepositoryConnection rc ) throws RepositoryException {
			throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
		}
	}

}
