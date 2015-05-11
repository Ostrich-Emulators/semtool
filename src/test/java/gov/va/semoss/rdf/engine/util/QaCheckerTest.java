/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriBuilder;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.ntriples.NTriplesWriter;

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

	public QaCheckerTest() {
	}

	private InMemorySesameEngine engine;
	private File dbfile;

	private IEngine extractKb() {
		if ( null != dbfile ) {
			FileUtils.deleteQuietly( dbfile );
		}

		try {
			dbfile = File.createTempFile( "semoss-test-", ".jnl" );
			Files.copy( new File( "src/test/resources/test.jnl" ).toPath(),
					dbfile.toPath(), StandardCopyOption.REPLACE_EXISTING );
		}
		catch ( Exception e ) {
			Logger.getLogger( getClass() ).error( e, e );
		}

		UriBuilder schema = UriBuilder.getBuilder( OWLSTART );
		final UriBuilder data = UriBuilder.getBuilder( BASEURI );
		data.setSanitizer( new UriBuilder.DefaultSanitizer() );
		schema.setSanitizer( new UriBuilder.DefaultSanitizer() );

		Properties props = BigDataEngine.generateProperties( dbfile );
		props.setProperty( Constants.SEMOSS_URI, OWLSTART.stringValue() );
		props.setProperty( Constants.ENGINE_NAME, "Empty KB" );
		BigDataEngine eng = new BigDataEngine() {
			@Override
			public UriBuilder getDataBuilder() {
				return data;
			}
		};

		eng.openDB( props );
		eng.setSchemaBuilder( schema );
		return eng;
	}

	private void removeKb( IEngine eng ) {
		eng.closeDB();
		FileUtils.deleteQuietly( dbfile );
	}

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
		engine = new InMemorySesameEngine();
	}

	@After
	public void tearDown() {
		if ( null != engine ) {
			engine.closeDB();
		}
	}

	@Test
	public void testInstanceExists() {
		String type = "test";
		String label = "label";
		QaChecker el = new QaChecker();
		Map<String, URI> types = new HashMap<>();
		types.put( label, BASEURI );
		el.cacheConceptInstances( types, type );
		assertTrue( el.instanceExists( type, label ) );
		assertFalse( el.instanceExists( type, "notthere" ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testCacheUris() throws Exception {
		QaChecker el = new QaChecker();
		el.cacheUris( null, new HashMap<>() );
	}

	@Test
	public void testLoadCaches() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		RepositoryConnection rc = engine.getRawConnection();
		rc.begin();
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				RDF.TYPE, new URIImpl( "http://owl.junk.com/testfiles/Category" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Concept/Category/Beverages" ),
				RDFS.LABEL, rc.getValueFactory().createLiteral( "Beverages" ) );

		rc.add( new URIImpl( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				RDFS.LABEL, rc.getValueFactory().createLiteral( "Chai:Beverages" ) );
		rc.add( new URIImpl( "http://junk.com/testfiles/Relation/Category/Chai_x_Beverages" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );

		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Category" ),
				RDFS.SUBCLASSOF, new URIImpl( "http://owl.junk.com/testfiles/Concept" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Category" ), RDFS.LABEL,
				rc.getValueFactory().createLiteral( "Category" ) );

		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Description" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Description" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation/Contains" ) );
		rc.add( new URIImpl( "http://owl.junk.com/testfiles/Description" ), RDFS.LABEL,
				rc.getValueFactory().createLiteral( "Description" ) );

		rc.add( new URIImpl( "http://sales.data/schema#xyz" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation" ) );
		rc.add( new URIImpl( "http://sales.data/schema#xyz" ),
				RDFS.SUBPROPERTYOF, new URIImpl( "http://owl.junk.com/testfiles/Relation/Contains" ) );
		rc.add( new URIImpl( "http://sales.data/schema#xyz" ),
				RDFS.LABEL, new LiteralImpl( "508 Compliant?" ) );
		rc.commit();

		QaChecker el = new QaChecker();
		el.loadCaches( engine );

		assertTrue( el.hasCachedPropertyClass( "Description" ) );
		assertTrue( el.hasCachedPropertyClass( "508 Compliant?" ) );
	}

	@Test
	public void testCheckModelConformance() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		engine.getRawConnection().add( LEGACY_EXP, "", RDFFormat.NTRIPLES );

		QaChecker el = new QaChecker( engine );
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

		QaChecker el = new QaChecker( engine );

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
		assertEquals( wrong, errlsd.getData().get( 0 ) );
		assertEquals( 1, errlsd.getData().size() );

		assertEquals( right, oklsd.getData().get( 0 ) );
		assertEquals( 1, oklsd.getData().size() );

		assertNotEquals( errlsd, oklsd );
	}

	@Test
	public void testClear() {
		QaChecker el = new QaChecker();
		Map<String, URI> types = new HashMap<>();
		types.put( "rawlabel", BASEURI );
		el.cacheConceptInstances( types, "testType" );
		el.clear();
		assertFalse( el.instanceExists( "testType", "rawlabel" ) );
	}
}
