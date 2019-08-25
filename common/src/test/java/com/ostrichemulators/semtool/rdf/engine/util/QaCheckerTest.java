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
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker.CacheType;
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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author ryan
 */
public class QaCheckerTest {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();

	private static final IRI BASEURI = VF.createIRI( "http://junk.com/testfiles" );
	private static final IRI OWLSTART = VF.createIRI( "http://owl.junk.com/testfiles/" );
	private static final IRI DATAURI = VF.createIRI( "http://seman.tc/data/northwind/" );
	private static final UriBuilder OWLB = UriBuilder.getBuilder( OWLSTART );
	private static final UriBuilder DATAB = UriBuilder.getBuilder( DATAURI );

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
		engine = InMemorySesameEngine.open( true );
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
		Map<String, IRI> types = new HashMap<>();
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
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ), OWLB );

		RepositoryConnection rc = engine.getRawConnection();
		rc.begin();

		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				VF.createIRI( "http://owl.junk.com/testfiles/Description" ),
				SimpleValueFactory.getInstance().createLiteral( "Soft drinks, coffees, teas, beers, and ales" ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				RDF.TYPE, VF.createIRI( "http://owl.junk.com/testfiles/Category" ) );

		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Beverages" ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Product/Chai" ),
				VF.createIRI( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				VF.createIRI( "http://junk.com/testfiles/Concept/Category/Beverages" ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Product/Chai" ),
				RDF.TYPE, VF.createIRI( "http://owl.junk.com/testfiles/Product" ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Product/Chai" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Chai" ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Concept/Product/Chang" ),
				VF.createIRI( "http://junk.com/testfiles/Relation/Category/Chang_x_Beverages" ),
				VF.createIRI( "http://junk.com/testfiles/Concept/Category/Beverages" ) );

		rc.add( VF.createIRI( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				VF.createIRI( "http://owl.junk.com/testfiles/extraprop" ),
				SimpleValueFactory.getInstance().createLiteral( "1.0", VF.createIRI( "http://www.w3.org/2001/XMLSchema#double" ) ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Chai Category Beverages" ) );
		rc.add( VF.createIRI( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ) );

		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDF.TYPE, OWL.OBJECTPROPERTY );
		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Category" ) );
		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation" ) );

		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Description" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Description" ) );
		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Description" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation" ) );
		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Description" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation/Contains" ) );

		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDF.TYPE, OWL.OBJECTPROPERTY );
		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Category" ) );
		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation/Category" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation" ) );

		rc.add( VF.createIRI( "http://schema.org/xyz" ),
				RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "508 Compliant?" ) );
		rc.add( VF.createIRI( "http://schema.org/xyz" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation" ) );
		rc.add( VF.createIRI( "http://schema.org/xyz" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://owl.junk.com/testfiles/Relation/Contains" ) );

		rc.add( VF.createIRI( "http://owl.junk.com/testfiles/Relation" ),
				RDF.TYPE, VF.createIRI( "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property" ) );

		rc.commit();

		el.loadCaches( engine );

		assertTrue( el.hasCachedPropertyClass( "Description" ) );
		assertTrue( el.hasCachedPropertyClass( "508 Compliant?" ) );
	}

	@Test
	public void testLoadCachesModern() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ), OWLB );
		engine.getRawConnection().add( engine.getBaseIri(), SEMTOOL.ReificationModel,
				SEMTOOL.SEMTOOL_Reification );

		RepositoryConnection rc = engine.getRawConnection();
		rc.begin();
		rc.setNamespace( "schema", OWLB.toString() );
		rc.setNamespace( "data", DATAURI.stringValue() );

		final IRI DESC = OWLB.build( "Description" );
		rc.add( DESC, RDF.TYPE, OWL.DATATYPEPROPERTY );
		rc.add( DESC, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "508 Compliance" ) );

		final IRI RELDESC = OWLB.build( "RelDesc" );
		rc.add( RELDESC, RDF.TYPE, OWL.DATATYPEPROPERTY );
		rc.add( RELDESC, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "Zippalee!" ) );

		final IRI conceptclass = OWLB.build( "myconceptclass1" );
		rc.add( conceptclass, RDF.TYPE, RDFS.CLASS );
		rc.add( conceptclass, RDFS.SUBCLASSOF, OWLB.getConceptIri().build() );
		rc.add( conceptclass, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "My Concept Class 1" ) );

		final IRI conceptclass2 = OWLB.build( "myconceptclass2" );
		rc.add( conceptclass2, RDF.TYPE, RDFS.CLASS );
		rc.add( conceptclass2, RDFS.SUBCLASSOF, OWLB.getConceptIri().build() );
		rc.add( conceptclass2, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "My Concept Class 2" ) );

		final IRI concept = DATAB.build( "myconcept1" );
		rc.add( concept, RDF.TYPE, RDFS.CLASS );
		rc.add( concept, RDFS.SUBCLASSOF, conceptclass );
		rc.add( concept, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "My Concept" ) );
		rc.add( concept, DESC, SimpleValueFactory.getInstance().createLiteral( "508 Compliant?" ) );

		final IRI concept2 = DATAB.build( "myconcept2" );
		rc.add( concept2, RDF.TYPE, RDFS.CLASS );
		rc.add( concept2, RDFS.SUBCLASSOF, conceptclass2 );
		rc.add( concept2, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "My Other Concept" ) );
		rc.add( concept2, DESC, SimpleValueFactory.getInstance().createLiteral( "feliz cumpleaños" ) );

		final IRI relclass = OWLB.build( "relationclass" );
		rc.add( relclass, RDF.TYPE, OWL.OBJECTPROPERTY );
		rc.add( relclass, RDFS.SUBPROPERTYOF, OWLB.getRelationIri().build() );
		rc.add( relclass, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "A Relation Class" ) );

		final IRI rel = DATAB.build( "myrel" );
		rc.add( rel, RDFS.SUBPROPERTYOF, relclass );
		rc.add( rel, RDFS.LABEL, SimpleValueFactory.getInstance().createLiteral( "My Relation" ) );
		rc.add( rel, RELDESC, SimpleValueFactory.getInstance().createLiteral( "A Relation Prop" ) );
		rc.add( concept, rel, concept2 );

		rc.commit();

		el.loadCaches( engine );

		assertFalse( el.hasCachedPropertyClass( "Description" ) );
		assertTrue( el.hasCachedPropertyClass( "508 Compliance" ) );
		assertTrue( el.hasCachedPropertyClass( "Zippalee!" ) );

		assertEquals( 2, el.getCache( CacheType.CONCEPTCLASS ).size() );
		assertEquals( 2, el.getCache( CacheType.PROPERTYCLASS ).size() );
		assertEquals( 1, el.getCache( CacheType.RELATIONCLASS ).size() );
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
		engine.setBuilders( DATAB, OWLB );

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
		Map<String, IRI> types = new HashMap<>();
		types.put( "rawlabel", BASEURI );
		el.cacheConceptInstances( types, "testType" );
		el.clear();
		assertFalse( el.instanceExists( "testType", "rawlabel" ) );
	}
}
