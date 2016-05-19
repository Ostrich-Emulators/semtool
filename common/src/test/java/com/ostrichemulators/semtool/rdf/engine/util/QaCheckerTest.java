/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.DeterministicSanitizer;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class QaCheckerTest {

	private static final URI BASEURI = new URIImpl( "http://junk.com/testfiles" );
	private static final URI OWLSTART = new URIImpl( "http://owl.junk.com/testfiles" );
	private static final URI DATAURI = new URIImpl( "http://seman.tc/data/northwind/" );
	private static final URI SCHEMAURI = new URIImpl( "http://seman.tc/models/northwind#" );

	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private static final File LEGACY_EXP = new File( "src/test/resources/legacy-mm.nt" );

	private InMemorySesameEngine engine;
	private QaChecker el;

	@BeforeClass
	public static void setUpClass() {
		// a deterministic sanitizer ensures we get repeatable results for URIs
		UriBuilder.setDefaultSanitizerClass( DeterministicSanitizer.class );
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		engine = InMemorySesameEngine.open();
		el = new QaChecker();
	}

	@After
	public void tearDown() {
		el.release();
		if ( null != engine ) {
			engine.closeDB();
		}
	}

	@Test
	public void testInstanceExists() {
		String type = "test";
		String label = "label";
		Map<String, URI> types = new HashMap<>();
		types.put( label, BASEURI );
		el.cacheConceptInstances( types, type );
		assertTrue( el.instanceExists( type, label ) );
		assertFalse( el.instanceExists( type, "notthere" ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testCacheUris() throws Exception {
		el.cacheUris( null, new HashMap<>() );
	}

	@Test
	public void testLoadCachesLegacy() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		RepositoryConnection rc = engine.getRawConnection();
		rc.begin();

		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				new URIImpl( "http://owl.junk.com/testfiles/Description" ),
				new LiteralImpl( "Soft drinks, coffees, teas, beers, and ales" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				RDF.TYPE, new URIImpl( "http://owl.junk.com/testfiles/Category" ) );

		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				RDFS.LABEL, new LiteralImpl( "Beverages" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Product/Chai" ),
				new URIImpl( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Product/Chai" ),
				RDF.TYPE, new URIImpl( "http://owl.junk.com/testfiles/Product" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Product/Chai" ),
				RDFS.LABEL, new LiteralImpl( "Chai" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Product/Chang" ),
				new URIImpl( "http://junk.com/testfiles/Relation/Category/Chang_x_Beverages" ),
				new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ) );

		rc.add( new URIImpl( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				new URIImpl( "http://owl.junk.com/testfiles/extraprop" ),
				new LiteralImpl( "1.0", new URIImpl( "http://www.w3.org/2001/XMLSchema#double" ) ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				RDFS.LABEL, new LiteralImpl( "Chai Category Beverages" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ) );

		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDF.TYPE, OWL.OBJECTPROPERTY );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.LABEL, new LiteralImpl( "Category" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );

		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Description" ),
				RDFS.LABEL, new LiteralImpl( "Description" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Description" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Description" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation/Contains" ) );

		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDF.TYPE, OWL.OBJECTPROPERTY );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.LABEL, new LiteralImpl( "Category" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );

		rc.add( new URIImpl( "http://schema.org/xyz" ),
				RDFS.LABEL, new LiteralImpl( "508 Compliant?" ) );
		rc.add( new URIImpl( "http://schema.org/xyz" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );
		rc.add( new URIImpl( "http://schema.org/xyz" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation/Contains" ) );

		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Relation" ),
				RDF.TYPE, new URIImpl( "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property" ) );

		rc.commit();

		el.loadCaches( engine );

		assertTrue( el.hasCachedPropertyClass( "Description" ) );
		assertTrue( el.hasCachedPropertyClass( "508 Compliant?" ) );
	}

	@Test
	public void testLoadCachesModern() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );
		engine.getRawConnection().add(engine.getBaseUri(), SEMTOOL.ReificationModel,
				SEMTOOL.SEMTOOL_Reification );

		RepositoryConnection rc = engine.getRawConnection();
		rc.begin();

		final URI DESC = new URIImpl( "http://owl.junk.com/testfiles/Description" );
		rc.add( DESC, RDF.TYPE, OWL.DATATYPEPROPERTY );
		rc.add( DESC, RDFS.LABEL, new LiteralImpl( "Description" ) );

		rc.add( new URIImpl( "http://schema.org/xyz" ), RDFS.LABEL,
				new LiteralImpl( "508 Compliant?" ) );
		rc.add( new URIImpl( "http://schema.org/xyz" ), RDF.TYPE, OWL.DATATYPEPROPERTY );

		rc.commit();

		el.loadCaches( engine );

		assertTrue( el.hasCachedPropertyClass( "Description" ) );
		assertTrue( el.hasCachedPropertyClass( "508 Compliant?" ) );
	}

	@Test
	public void testCheckModelConformanceLegacy() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		engine.getRawConnection().add( LEGACY_EXP, "", RDFFormat.NTRIPLES );

		el.release(); // release the checker from setup
		el = new QaChecker( engine );
		ImportData test = new ImportData();
		test.getMetadata().setDataBuilder( engine.getDataBuilder().toString() );
		test.getMetadata().setSchemaBuilder( engine.getSchemaBuilder().toString() );

		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "Product-x", "Category-x", "Category-y" );
		lsd.addProperty( "extraprop-x" );
		test.add( lsd );

		LoadingSheetData errs = el.checkModelConformance( lsd );

		assertTrue( errs.hasModelErrors() );
		assertTrue( errs.hasSubjectTypeError() );
		assertTrue( errs.hasObjectTypeError() );
		assertTrue( errs.propertyIsError( "extraprop-x" ) );
	}

	@Test
	public void testSeparateConformanceErrors() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		engine.getRawConnection().add( LEGACY_EXP, "", RDFFormat.NTRIPLES );

		el.release(); // release the checker from setup
		el = new QaChecker( engine );

		ImportData test = new ImportData();
		test.getMetadata().setDataBuilder( engine.getDataBuilder().toString() );
		test.getMetadata().setSchemaBuilder( engine.getSchemaBuilder().toString() );

		LoadingSheetData lsd = LoadingSheetData.nodesheet( "Category" );
		LoadingSheetData.LoadingNodeAndPropertyValues wrong = lsd.add( "Seefood" );
		LoadingSheetData.LoadingNodeAndPropertyValues right = lsd.add( "Seafood" );
		test.add( lsd );

		ImportData errs = new ImportData();
		el.separateConformanceErrors( test, errs, engine );

		LoadingSheetData errlsd = errs.getSheet( "Category" );
		LoadingSheetData oklsd = test.getSheet( "Category" );
		assertEquals( wrong, errlsd.iterator().next() );
		assertEquals( 1, errlsd.rows() );

		assertEquals( right, oklsd.iterator().next() );
		assertEquals( 1, oklsd.rows() );

		assertNotEquals( errlsd, oklsd );
	}

	@Test
	public void testClear() {
		Map<String, URI> types = new HashMap<>();
		types.put( "rawlabel", BASEURI );
		el.cacheConceptInstances( types, "testType" );
		el.clear();
		assertFalse( el.instanceExists( "testType", "rawlabel" ) );
	}
}
