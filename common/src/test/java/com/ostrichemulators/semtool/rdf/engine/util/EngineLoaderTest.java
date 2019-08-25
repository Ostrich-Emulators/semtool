/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.poi.main.CSVReader;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportValidationException;
import com.ostrichemulators.semtool.poi.main.ImportValidationException.ErrorType;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.POIReader;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import com.ostrichemulators.semtool.util.DeterministicSanitizer;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class EngineLoaderTest {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final Logger log = Logger.getLogger( EngineLoaderTest.class );

	private static final IRI BASEURI = VF.createIRI( "http://junk.com/testfiles" );
	private static final IRI OWLSTART = VF.createIRI( "http://owl.junk.com/testfiles" );
	private static final IRI DATAURI = VF.createIRI( "http://seman.tc/data/northwind/" );
	private static final IRI SCHEMAURI = VF.createIRI( "http://seman.tc/models/northwind#" );

	private static final File CSVLOADER = new File( "src/test/resources/airplanes.txt" );
	private static final File CSVDATA = new File( "src/test/resources/airplanes.csv" );
	private static final File CSV_EXP = new File( "src/test/resources/airplanes-mm.nt" );
	private static final File CSV_NOMM_EXP = new File( "src/test/resources/airplanes-nomm.nt" );

	private static final File CSVLOADER2 = new File( "src/test/resources/systems.txt" );
	private static final File CSVDATA2 = new File( "src/test/resources/systems.csv" );
	private static final File CSV_EXP2 = new File( "src/test/resources/systems-mm.nt" );

	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private static final File LEGACY_EXP = new File( "src/test/resources/legacy-mm.nt" );
	private static final File LEGACY_EXP2 = new File( "src/test/resources/legacy-3args.ttl" );
	private static final File LEGACY_NOMM_EXP = new File( "src/test/resources/legacy-nomm.nt" );

	private static final File CUSTOM = new File( "src/test/resources/custom.xlsx" );
	private static final File CUSTOM_EXP = new File( "src/test/resources/custom-mm.nt" );
	private static final File CUSTOM_NOMM_EXP = new File( "src/test/resources/custom-nomm.nt" );

	private static final File CUSTOM2 = new File( "src/test/resources/custom2.xlsx" );
	private static final File CUSTOM2_EXP = new File( "src/test/resources/custom2-mm.nt" );
	private static final File CUSTOM2_NOMM_EXP = new File( "src/test/resources/custom2-nomm.nt" );

	private static final File TICKETBASE = new File( "src/test/resources/ticket-setup.ttl" );
	private static final File TICKET583 = new File( "src/test/resources/ticket583.xlsx" );
	private static final File TICKET583_EXP = new File( "src/test/resources/ticket583.nt" );
	private static final File TICKET584 = new File( "src/test/resources/ticket584.xlsx" );
	private static final File TICKET584_EXP = new File( "src/test/resources/ticket584.nt" );

	private static final File TICKET608 = new File( "src/test/resources/ticket608.xlsx" );
	private static final File TICKET608_EXP = new File( "src/test/resources/ticket608.nt" );

	private static final File TEST10 = new File( "src/test/resources/test10.xlsx" );
	private static final File TEST10_EXP = new File( "src/test/resources/test10.nt" );
	private static final File TEST10_EXP2 = new File( "src/test/resources/test10.ttl" );

	private static final File TEST14 = new File( "src/test/resources/test14.xlsx" );
	private static final File TEST14_EXP = new File( "src/test/resources/test14.nt" );

	private static final File TEST15 = new File( "src/test/resources/test15.xlsx" );
	private static final File TEST15_EXP = new File( "src/test/resources/test15.nt" );

	private static final File TEST17 = new File( "src/test/resources/test17.xlsx" );
	private static final File TEST17_EXP = new File( "src/test/resources/test17.nt" );

	private static final File TESTSPECIAL = new File( "src/test/resources/test-special.xlsx" );
	private static final File TESTSPECIAL_EXP = new File( "src/test/resources/test-special.nt" );

	private InMemorySesameEngine engine;
	private File dbfile;

	private IEngine extractKb() throws Exception {
		throw new IllegalArgumentException( "BigData/Blazegraph is no longer supported" );
//		if ( null != dbfile ) {
//			FileUtils.deleteQuietly( dbfile );
//		}
//
//		try {
//			dbfile = File.createTempFile( "semoss-test-", ".jnl" );
//		}
//		catch ( IOException e ) {
//			log.error( e, e );
//		}
//
//		UriBuilder schema = UriBuilder.getBuilder( OWLSTART );
//		final UriBuilder data = UriBuilder.getBuilder( BASEURI );
//		data.setSanitizer( new DefaultSanitizer() );
//		schema.setSanitizer( new DefaultSanitizer() );
//
//		Properties props = BigDataEngine.generateProperties( dbfile );
//		props.setProperty( Constants.SEMOSS_IRI, OWLSTART.stringValue() );
//		props.setProperty( Constants.ENGINE_NAME, "Empty KB" );
//		IEngine eng = EngineFactory.getEngine( props );
//		eng.setDataBuilder( data );
//		eng.setSchemaBuilder( schema );
//		return eng;
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
		engine = InMemorySesameEngine.open();
		engine.setEngineName( "engine loader tester" );
	}

	@After
	public void tearDown() {
		if ( null != engine ) {
			engine.closeDB();
		}
	}

	@Test
	public void testCsvImport() throws Exception {
		CSVReader rdr = new CSVReader( CSVLOADER );
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.setReader( "csv", rdr );
		el.loadToEngine( Arrays.asList( CSVDATA ), engine, true, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"airplanes-mm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( CSV_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testCsvImportNoMetamodel() throws Exception {
		CSVReader rdr = new CSVReader( CSVLOADER );
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.setReader( "csv", rdr );
		el.loadToEngine( Arrays.asList( CSVDATA ), engine, false, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"airplanes-nomm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(),
				getExpectedGraph( CSV_NOMM_EXP ), engine.getSchemaBuilder(),
				engine.getDataBuilder() );
	}

	@Test
	public void testCsvImport2() throws Exception {
		CSVReader rdr = new CSVReader( CSVLOADER2 );
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		EngineLoader el = new EngineLoader();
		el.setReader( "csv", rdr );
		el.setDefaultBaseUri( BASEURI, true );
		try {
			el.loadToEngine( Arrays.asList( CSVDATA2 ), engine, true, null );
		}
		finally {
			el.release();
		}

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"systems-mm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( CSV_EXP2 ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportXlsLegacy() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		ImportData id = new ImportData();
		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		try {
			el.loadToEngine( Arrays.asList( LEGACY ), engine, true, id );
		}
		finally {
			el.release();
		}

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"legacy-mm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( LEGACY_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportXlsLegacyNoMetamodel() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		ImportData id = new ImportData();
		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.loadToEngine( Arrays.asList( LEGACY ), engine, false, id );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"legacy-nomm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( LEGACY_NOMM_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportXlsModern() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		ImportData errors = new ImportData();
		el.loadToEngine( Arrays.asList( CUSTOM ), engine, true, errors );
		Model owls = el.getMetamodel();
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom-mm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareOwls( owls, CUSTOM_EXP, engine.getSchemaBuilder() );
		compareData( engine.getRawConnection(), getExpectedGraph( CUSTOM_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportNamespaceHeavyXlsModern() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		ImportData errors = new ImportData();
		el.loadToEngine( Arrays.asList( CUSTOM2 ), engine, true, errors );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom2-mm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( CUSTOM2_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportXlsSpecial() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		ImportData errors = new ImportData();
		el.loadToEngine( Arrays.asList( TESTSPECIAL ), engine, true, errors );
		Model owls = el.getMetamodel();
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"test-special.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareOwls( owls, TESTSPECIAL_EXP, engine.getSchemaBuilder() );
		compareData( engine.getRawConnection(), getExpectedGraph( TESTSPECIAL_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportRDF() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		ImportData errors = new ImportData();
		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( TICKET584_EXP ), engine, true, errors );

		// cleanup
		engine.getRawConnection().remove( (Resource) null, RDF.TYPE, SEMTOOL.Database );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"import-rdf.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}

		}

		compareData( engine.getRawConnection(), getExpectedGraph( TICKET584_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test( expected = FileNotFoundException.class )
	public void testImportRDFFail() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		ImportData errors = new ImportData();
		EngineLoader el = new EngineLoader();
		try {
			el.loadToEngine( Arrays.asList( new File( "notexisting.rdf" ) ),
					engine, true, errors );
		}
		finally {
			el.release();
		}
	}

	@Test( expected = ImportValidationException.class )
	public void testImportRDFFail2() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		ImportData errors = new ImportData();
		EngineLoader el = new EngineLoader();
		try {
			el.loadToEngine( Arrays.asList( new File( "notexisting" ) ),
					engine, true, errors );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.INVALID_DATA == e.error ) {
				throw e;
			}
		}
		finally {
			el.release();
		}
	}

	@Test
	public void testImportNamespaceHeavyXlsModernNoMetamodel() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		ImportData errors = new ImportData();
		el.loadToEngine( Arrays.asList( CUSTOM2 ), engine, false, errors );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom2-nomm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( CUSTOM2_NOMM_EXP ),
				engine.getSchemaBuilder(), engine.getDataBuilder() );
	}

	@Test
	public void testImportXlsModernNoMetamodel() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.loadToEngine( Arrays.asList( CUSTOM ), engine, false, null );
		Model owls = el.getMetamodel();
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom-nomm.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		Model expected = getExpectedGraph( CUSTOM_NOMM_EXP );
		assertTrue( owls.isEmpty() );
		compareData( engine.getRawConnection(), expected, engine.getSchemaBuilder(),
				engine.getDataBuilder() );
	}

//	@Test
//	public void testLoadToEngine_two_loadsLegacy() throws Exception {
//		// check to make sure if we load the same data twice, we don't expand the
//		// data in the KB (basically, make sure the caching works)
//		IEngine eng = extractKb();
//		EngineLoader el = new EngineLoader();
//		el.setDefaultBaseUri( BASEURI, false );
//		el.loadToEngine( Arrays.asList( LEGACY ), eng, true, null );
//		el.release();
//		OneVarListQueryAdapter<IRI> o
//				= OneVarListQueryAdapter.getIriList( "SELECT ?IRI WHERE { ?IRI ?p ?o . FILTER( isUri( ?IRI ) ) }",
//						"IRI" );
//		List<IRI> oldlist = eng.query( o );
//
//		EngineLoader el2 = new EngineLoader();
//		el2.setDefaultBaseUri( BASEURI, false );
//		el2.loadToEngine( Arrays.asList( LEGACY ), eng, true, null );
//		el2.release();
//		List<IRI> newlist = engine.query( o );
//		removeKb( eng );
//		assertEquals( oldlist, newlist );
//	}
//
//	@Test
//	public void testLoadToEngine_two_loadsCurrent() throws Exception {
//		// same as the two_loads1 test, but in the custom metamodel mode
//		IEngine eng = extractKb();
//		EngineLoader el = new EngineLoader();
//		el.setDefaultBaseUri( BASEURI, false );
//		el.loadToEngine( Arrays.asList( CUSTOM ), eng, true, null );
//		el.release();
//		OneVarListQueryAdapter<IRI> o
//				= OneVarListQueryAdapter.getIriList( "SELECT ?IRI WHERE { ?IRI ?p ?o . FILTER( isUri( ?IRI ) ) }",
//						"IRI" );
//		List<IRI> oldlist = eng.query( o );
//
//		EngineLoader el2 = new EngineLoader();
//		el2.setDefaultBaseUri( BASEURI, false );
//		el2.loadToEngine( Arrays.asList( CUSTOM ), eng, true, null );
//		el2.release();
//
//		List<IRI> newlist = engine.query( o );
//		removeKb( eng );
//		assertEquals( oldlist, newlist );
//	}
	@Test
	public void testTicket583() throws Exception {
		RepositoryConnection rc = engine.getRawConnection();
		rc.add( TICKETBASE, null, RDFFormat.TURTLE );
		engine.setBuilders( UriBuilder.getBuilder( "http://example.org/ex1" ),
				UriBuilder.getBuilder( "http://foo.bar/model#" ) );

		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( TICKET583 ), engine, false, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"ticket583.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( TICKET583_EXP ),
				UriBuilder.getBuilder( "http://example.org/ex1" ),
				UriBuilder.getBuilder( "http://foo.bar/model#" ) );
	}

	// @Test
	public void testTicket584() throws Exception {
		RepositoryConnection rc = engine.getRawConnection();

		rc.add( TICKETBASE, null, RDFFormat.TURTLE );
		engine.setBuilders( UriBuilder.getBuilder( "http://example.org/ex1" ),
				UriBuilder.getBuilder( "http://foo.bar/model#" ) );

		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( TICKET584 ), engine, false, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"ticket584.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

//		compareData( rc, getExpectedGraph( TICKET584_EXP ),
//				UriBuilder.getBuilder( "http://example.org/ex1" ),
//				UriBuilder.getBuilder( "http://foo.bar/model#" ) );
	}

	@Test
	public void testVerySimilarProperties() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.loadToEngine( Arrays.asList( TICKET608 ), engine, true, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"ticket608.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( TICKET608_EXP ),
				engine.getDataBuilder(), engine.getSchemaBuilder() );
	}

	@Test
	public void testLoadingSheet10() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.loadToEngine( Arrays.asList( TEST10 ), engine, true, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"test10.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( TEST10_EXP ),
				engine.getDataBuilder(), engine.getSchemaBuilder() );
	}

	@Test
	public void testSemossEdgeTest10() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		engine.getRawConnection().add( engine.getBaseIri(), SEMTOOL.ReificationModel,
				SEMTOOL.SEMTOOL_Reification );
		engine.getRawConnection().commit();

		POIReader rdr = new POIReader();
		ImportData id = rdr.readOneFile( TEST10 );

		EngineLoader el = new EngineLoader();
		el.loadToEngine( id, engine, null );

		engine.getRawConnection().setNamespace( "testdata", id.getMetadata().getDataBuilder().toString() );
		engine.getRawConnection().setNamespace( "testowl", id.getMetadata().getSchemaBuilder().toString() );
		trace( TEST10_EXP2 );
		el.release();

		compare( engine, TEST10_EXP2, true );
	}

	@Test
	public void testSetDefaultBaseUriOverride() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( VF.createIRI( "test://something-different/blah" ), true );

		ImportData errs = new ImportData();
		ImportData id = new ImportData();
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "testtype" );
		id.getMetadata().setSchemaBuilder( "http://schema.foo.bar/" );
		id.getMetadata().setDataBuilder( "http://data.foo.bar/" );
		id.getMetadata().setBase( VF.createIRI( "http://base.foo.bar" ) );

		id.add( lsd );
		lsd.add( "uno" );

		el.loadToEngine( id, engine, errs );
		el.release();

		OneVarListQueryAdapter<IRI> q
				= OneVarListQueryAdapter.getIriList( "SELECT ?file { ?db ?subset ?file } ", "file" );
		q.bind( "db", engine.getBaseIri() );
		q.bind( "subset", MetadataConstants.VOID_SUBSET );
		List<IRI> uris = engine.query( q );
		assertEquals( "test://something-different/blah", uris.get( 0 ).stringValue() );
	}

	@Test
	public void testSetDefaultBaseUri() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( VF.createIRI( "test://something-different/blah" ), false );

		ImportData errs = new ImportData();
		ImportData id = new ImportData();
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "testtype" );
		id.getMetadata().setSchemaBuilder( "http://schema.foo.bar/" );
		id.getMetadata().setDataBuilder( "http://data.foo.bar/" );
		id.getMetadata().setBase( VF.createIRI( "http://base.foo.bar" ) );

		id.add( lsd );
		lsd.add( "uno" );

		try {
			el.loadToEngine( id, engine, errs );
		}
		finally {
			el.release();
		}

		OneVarListQueryAdapter<IRI> q
				= OneVarListQueryAdapter.getIriList( "SELECT ?file { ?db ?subset ?file } ", "file" );
		q.bind( "db", engine.getBaseIri() );
		q.bind( "subset", MetadataConstants.VOID_SUBSET );
		List<IRI> uris = engine.query( q );
		assertEquals( "http://base.foo.bar", uris.get( 0 ).stringValue() );
	}

	@Test
	public void testSetDefaultBaseUriDefault() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( VF.createIRI( "test://something-different/blah" ), false );

		ImportData errs = new ImportData();
		ImportData id = new ImportData();
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "testtype" );
		id.getMetadata().setSchemaBuilder( "http://schema.foo.bar/" );
		id.getMetadata().setDataBuilder( "http://data.foo.bar/" );

		id.add( lsd );
		lsd.add( "uno" );

		try {
			el.loadToEngine( id, engine, errs );
		}
		finally {
			el.release();
		}

		OneVarListQueryAdapter<IRI> q
				= OneVarListQueryAdapter.getIriList( "SELECT ?file { ?db ?subset ?file } ", "file" );
		q.bind( "db", engine.getBaseIri() );
		q.bind( "subset", MetadataConstants.VOID_SUBSET );
		List<IRI> uris = engine.query( q );
		assertEquals( "test://something-different/blah", uris.get( 0 ).stringValue() );
	}

	@Test( expected = ImportValidationException.class )
	public void testSetDefaultBaseUriNoSet() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		//el.setDefaultBaseUri( VF.createIRI( "test://something-different/blah" ), false );

		ImportData errs = new ImportData();
		ImportData id = new ImportData();
		LoadingSheetData lsd = LoadingSheetData.nodesheet( "testtype" );
		id.getMetadata().setSchemaBuilder( "http://schema.foo.bar/" );
		id.getMetadata().setDataBuilder( "http://data.foo.bar/" );

		id.add( lsd );
		lsd.add( "uno" );

		try {
			el.loadToEngine( id, engine, errs );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.MISSING_DATA == e.error ) {
				throw e;
			}
		}
		finally {
			el.release();
		}

	}

	@Test
	public void testInitNamespaces() {
		ImportData data = new ImportData();
		EngineLoader.initNamespaces( data );
		assertEquals( 10, data.getMetadata().getNamespaces().size() );
		assertEquals( SEMTOOL.NAMESPACE, data.getMetadata().getNamespaces().get( SEMTOOL.PREFIX ) );
	}

	@Test
	public void testGetReader() {
		EngineLoader el = new EngineLoader();
		POIReader rdr = new POIReader();
		el.setReader( "xekd", rdr );
		assertEquals( rdr, el.getReader( new File( "test.xekd" ) ) );
		el.release();
	}

	@Test
	public void testLoadToEngine_3args() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		POIReader poi = new POIReader();
		ImportData data = poi.readOneFile( LEGACY );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		try {
			el.loadToEngine( data, engine, null );
		}
		finally {
			el.release();
		}

		engine.getRawConnection().setNamespace( "testdata",
				data.getMetadata().getDataBuilder().toString() );
		engine.getRawConnection().setNamespace( "testowl",
				data.getMetadata().getSchemaBuilder().toString() );

		trace( LEGACY_EXP2 );
		compare( engine, LEGACY_EXP2, true );
	}

	@Test
	public void testRelease() throws IOException {
		// this is a little hard to test because everything is private.
		// but when we stage to disk, we will get a file on the filesystem
		// that we can check gets deleted. We'll check before we run anything,
		// after we've created the file, and then that it gets deleted.
		File f = File.createTempFile( "semoss-staging-", "" );
		f.delete();

		FileFilter lister = new FileFilter() {

			@Override
			public boolean accept( File pathname ) {
				return ( pathname.isDirectory()
						&& pathname.getName().startsWith( "semoss-staging-" ) );
			}
		};

		Set<File> before
				= new HashSet<>( Arrays.asList( f.getParentFile().listFiles( lister ) ) );

		EngineLoader el = new EngineLoader( false );

		Set<File> after
				= new HashSet<>( Arrays.asList( f.getParentFile().listFiles( lister ) ) );

		after.removeAll( before );

		// hopefully have just one file left--the one that just got created
		assertEquals( 1, after.size() );

		el.release();

		assertTrue( !after.iterator().next().exists() );
	}

	@Test( expected = ImportValidationException.class )
	public void testBadTriple() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		engine.getRawConnection().add( LEGACY_EXP, "", RDFFormat.NTRIPLES );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );

		ImportData test = new ImportData();
		test.getMetadata().setDataBuilder( engine.getDataBuilder().toString() );
		test.getMetadata().setSchemaBuilder( engine.getSchemaBuilder().toString() );

		test.getMetadata().add( "<http://foo.bar.bah/bash"/*missing >*/,
				RDFS.LABEL.toString(), "a label" );
		LoadingSheetData lsd
				= LoadingSheetData.relsheet( "Product-x", "Category-x", "Category-y" );
		test.add( lsd );

		try {
			el.loadToEngine( test, engine, null );
		}
		catch ( ImportValidationException e ) {
			if ( ErrorType.INVALID_DATA == e.error ) {
				throw e;
			}
		}
		finally {
			el.release();
		}
	}

	@Test
	public void testCleanValue() {
		Value old = VF.createLiteral( "this is a test" );
		Value result = EngineLoader.cleanValue( old, VF );

		assertEquals( SimpleLiteral.class, result.getClass() );
		//assertNotEquals( old.getClass(), result.getClass() );
	}

	@Test
	public void testLoadingSheet14() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		try {
			el.loadToEngine( Arrays.asList( TEST14 ), engine, true, null );
		}
		finally {
			el.release();
		}

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"test14.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( TEST14_EXP ),
				engine.getDataBuilder(), engine.getSchemaBuilder() );
	}

	@Test
	public void testLoadingSheet15() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( "http://sales.data/purchases#" ),
				UriBuilder.getBuilder( "http://sales.data/schema#" ) );

		engine.getRawConnection().add( VF.createIRI( "http://sales.data/purchases/2015/vocab" ),
				RDF.TYPE, OWL.ONTOLOGY );
		engine.getRawConnection().add( VF.createIRI( "http://sales.data/schema#xyz" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://sales.data/schema#Relation" ) );
		engine.getRawConnection().add( VF.createIRI( "http://sales.data/schema#xyz" ),
				RDFS.SUBPROPERTYOF, VF.createIRI( "http://sales.data/schema#Relation/Contains" ) );
		engine.getRawConnection().add( VF.createIRI( "http://sales.data/schema#xyz" ),
				RDFS.LABEL, VF.createLiteral( "508 Compliant?" ) );

		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );

		try {
			el.loadToEngine( Arrays.asList( TEST15 ), engine, true, null );
		}
		finally {
			el.release();
		}

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"test15.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( TEST15_EXP ),
				engine.getDataBuilder(), engine.getSchemaBuilder() );
	}

	@Test
	public void testLoadingSheet17() throws Exception {
		engine.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		EngineLoader el = new EngineLoader();
		try {
			el.loadToEngine( Arrays.asList( TEST17 ), engine, true, null );
		}
		finally {
			el.release();
		}

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"test17.nt" ) ) ) ) {
				engine.getRawConnection().export( new NTriplesWriter( w ) );
			}
		}

		compareData( engine.getRawConnection(), getExpectedGraph( TEST17_EXP ),
				engine.getDataBuilder(), engine.getSchemaBuilder() );
	}

	private static Model getExpectedGraph( File rdf ) {
		return getExpectedGraph( rdf, RDFFormat.NTRIPLES );
	}

	private static Model getExpectedGraph( File rdf, RDFFormat fmt ) {
		SailRepository repo = new SailRepository( new MemoryStore() );
		RepositoryConnection expectedrc = null;
		List<Statement> stmts = new ArrayList<>();
		try {
			repo.init();
			expectedrc = repo.getConnection();
			expectedrc.add( rdf, null, fmt );
			stmts.addAll( QueryResults.stream( expectedrc.getStatements( null, null,
					null, true ) ).collect( Collectors.toList() ) );
		}
		catch ( RepositoryException | IOException | RDFParseException e ) {
		}
		finally {
			if ( null != expectedrc ) {
				try {
					expectedrc.close();
				}
				catch ( Exception ex ) {
					// don't care
				}

				try {
					repo.shutDown();
				}
				catch ( Exception exc ) {
					// don't care
				}
			}
		}

		return new LinkedHashModel( stmts );
	}

	/**
	 * Run a couple heuristics comparing the OWL statements and the expected
	 * output file
	 *
	 * @param owls the owl statements from the load
	 * @param filename an RDF file containing the expected results of the load
	 *
	 * @throws Exception
	 */
	private void compareOwls( Collection<Statement> owls, File filename,
			UriBuilder bldr ) throws Exception {
		Model expected = getExpectedGraph( filename );
		Model test = new LinkedHashModel( owls );

		compare( "category concept", expected, test, bldr.getConceptIRI( "Category" ), null, null );
		compare( "product concept", expected, test, bldr.getConceptIRI( "Product" ), null, null );
		compare( "category relation", expected, test, bldr.getRelationIri( "Category" ), null, null );
		compare( "contains relation", expected, test, bldr.getConceptIRI( "Contains" ), null, null );
		IRI contains = bldr.getRelationIri( "Contains" );
		UriBuilder bldr2 = UriBuilder.getBuilder( contains );
		compare( "contains relation description",
				expected, test, bldr2.add( "Description" ).build(), null, null );
		compare( "contains relation extraprop",
				expected, test, bldr2.add( "extraprop" ).build(), null, null );
	}

	private void compareData( RepositoryConnection testRc, Model expected,
			UriBuilder owl, UriBuilder base ) throws Exception {
		List<Statement> stmts = QueryResults.stream( testRc.getStatements( null, null, null, false ) )
				.collect( Collectors.toList() );

		Model test = new LinkedHashModel( stmts );

		assertEquals( "dataset size", expected.size(), test.size() );

		compare( "category concept", expected, test, null, RDF.TYPE,
				owl.getConceptIRI( "Category" ) );
		compare( "beverages concept", expected, test,
				base.getConceptIri().add( "Category" ).add( "Beverages" ).build(),
				null, null );
		compare( "dairy products", expected, test,
				null, RDFS.LABEL, VF.createLiteral( "Dairy Products" ) );
		compare( "chai-beverages link", expected, test,
				null, RDFS.LABEL, VF.createLiteral( "Chai Category Beverages" ) );

		IRI bev
				= base.getRelationIri().add( "Category" ).add( "Chai_x_Beverages" ).build();
		compare( "chai-beverages category", expected, test,
				bev, RDFS.SUBPROPERTYOF, owl.getRelationIri( "Category" ) );
	}

	private void compare( String label, Model expected, Model test, Resource s,
			IRI p, Value o ) {
		Model exp = new LinkedHashModel( expected.filter( s, p, o ) );
		Model tst = new LinkedHashModel( test.filter( s, p, o ) );
		assertEquals( label + " size mismatch", exp.size(), tst.size() );
		assertEquals( label + " predicates mismatch", exp.predicates(), tst.predicates() );
		assertEquals( label + " subjects mismatch", exp.subjects(), tst.subjects() );
		assertEquals( label + " subjects mismatch", exp.objects(), tst.objects() );
	}

	private void trace( File f ) throws Exception {
		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			File tracefile = new File( tmpdir, f.getName() );
			try ( Writer w = new BufferedWriter( new FileWriter( tracefile ) ) ) {
				engine.getRawConnection().export( new TurtleWriter( w ) );
			}
		}
	}

	private static void compare( InMemorySesameEngine engine, File expected,
			boolean doCountsOnly ) throws IOException, RepositoryException, RDFHandlerException {

		// get rid of the random database id
		engine.getRawConnection().remove( (Resource) null, RDF.TYPE, SEMTOOL.Database );

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					expected.getName() ) ) ) ) {
				engine.getRawConnection().export( new TurtleWriter( w ) );
			}
		}

		Model model = getExpectedGraph( expected, RDFFormat.TURTLE );
		List<Statement> stmts = QueryResults.stream( engine.getRawConnection()
				.getStatements( null, null, null, false ) ).collect( Collectors.toList() );

		assertEquals( model.size(), stmts.size() );

		if ( doCountsOnly ) {
			// do counts instead of checking exact URIs
		}
		else {
			for ( Statement s : stmts ) {
				assertTrue( model.contains( s ) );
			}
		}
	}

}
