/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import gov.va.semoss.model.vocabulary.VAS;
import gov.va.semoss.poi.main.CSVReader;
import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.MetadataConstants;
import gov.va.semoss.rdf.engine.impl.BigDataEngine;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.Constants;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriBuilder;
import gov.va.semoss.util.UriBuilder.DefaultSanitizer;
import info.aduna.iteration.Iterations;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class EngineLoaderTest {

	private static final Logger log = Logger.getLogger( EngineLoaderTest.class );
	private static final File CSVLOADER = new File( "src/test/resources/airplanes.txt" );
	private static final File CSVDATA = new File( "src/test/resources/airplanes.csv" );
	private static final File CSV_EXP = new File( "src/test/resources/airplanes-mm.nt" );
	private static final File CSV_NOMM_EXP = new File( "src/test/resources/airplanes-nomm.nt" );

	private static final File CSVLOADER2 = new File( "src/test/resources/systems.txt" );
	private static final File CSVDATA2 = new File( "src/test/resources/systems.csv" );
	private static final File CSV_EXP2 = new File( "src/test/resources/systems-mm.nt" );

	private static final File LEGACY = new File( "src/test/resources/legacy.xlsx" );
	private static final File LEGACY_EXP = new File( "src/test/resources/legacy-mm.nt" );
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

	private static final URI BASEURI = new URIImpl( "http://junk.com/testfiles" );
	private static final URI OWLSTART = new URIImpl( "http://owl.junk.com/testfiles" );
	private static final URI DATAURI = new URIImpl( "http://seman.tc/data/northwind/" );
	private static final URI SCHEMAURI = new URIImpl( "http://seman.tc/models/northwind#" );

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
			log.error( e, e );
		}

		UriBuilder schema = UriBuilder.getBuilder( OWLSTART );
		final UriBuilder data = UriBuilder.getBuilder( BASEURI );
		data.setSanitizer( new DefaultSanitizer() );
		schema.setSanitizer( new DefaultSanitizer() );

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
		el.loadToEngine( Arrays.asList( CSVDATA2 ), engine, true, null );
		el.release();

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
		el.loadToEngine( Arrays.asList( LEGACY ), engine, true, id );
		el.release();

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
		Collection<Statement> owls
				= el.loadToEngine( Arrays.asList( CUSTOM ), engine, true, errors );
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
		Collection<Statement> owls
				= el.loadToEngine( Arrays.asList( CUSTOM ), engine, false, null );
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

	@Test
	public void testLoadToEngine_two_loadsLegacy() throws Exception {
		// check to make sure if we load the same data twice, we don't expand the
		// data in the KB (basically, make sure the caching works)
		IEngine eng = extractKb();
		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.loadToEngine( Arrays.asList( LEGACY ), eng, true, null );
		el.release();
		OneVarListQueryAdapter<URI> o
				= OneVarListQueryAdapter.getUriList( "SELECT ?uri WHERE { ?uri ?p ?o . FILTER( isUri( ?uri ) ) }",
						"uri" );
		List<URI> oldlist = eng.query( o );

		EngineLoader el2 = new EngineLoader();
		el2.setDefaultBaseUri( BASEURI, false );
		el2.loadToEngine( Arrays.asList( LEGACY ), eng, true, null );
		el2.release();
		List<URI> newlist = engine.query( o );
		removeKb( eng );
		assertEquals( oldlist, newlist );
	}

	@Test
	public void testLoadToEngine_two_loadsCurrent() throws Exception {
		// same as the two_loads1 test, but in the custom metamodel mode
		IEngine eng = extractKb();
		EngineLoader el = new EngineLoader();
		el.setDefaultBaseUri( BASEURI, false );
		el.loadToEngine( Arrays.asList( CUSTOM ), eng, true, null );
		el.release();
		OneVarListQueryAdapter<URI> o
				= OneVarListQueryAdapter.getUriList( "SELECT ?uri WHERE { ?uri ?p ?o . FILTER( isUri( ?uri ) ) }",
						"uri" );
		List<URI> oldlist = eng.query( o );

		EngineLoader el2 = new EngineLoader();
		el2.setDefaultBaseUri( BASEURI, false );
		el2.loadToEngine( Arrays.asList( CUSTOM ), eng, true, null );
		el2.release();

		List<URI> newlist = engine.query( o );
		removeKb( eng );
		assertEquals( oldlist, newlist );
	}

	@Test
	public void testTicket583() throws Exception {
		RepositoryConnection rc = engine.getRawConnection();
		rc.add( TICKETBASE, null, RDFFormat.TURTLE );

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

	private Model getExpectedGraph( File rdf ) {
		SailRepository repo = new SailRepository( new MemoryStore() );
		RepositoryConnection expectedrc = null;
		List<Statement> stmts = new ArrayList<>();
		try {
			repo.initialize();
			expectedrc = repo.getConnection();
			expectedrc.add( rdf, null, RDFFormat.NTRIPLES );
			stmts.addAll( Iterations.asList( expectedrc.getStatements( null, null,
					null, true ) ) );
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

		compare( "category concept", expected, test, bldr.getConceptUri( "Category" ), null, null );
		compare( "product concept", expected, test, bldr.getConceptUri( "Product" ), null, null );
		compare( "category relation", expected, test, bldr.getRelationUri( "Category" ), null, null );
		compare( "contains relation", expected, test, bldr.getConceptUri( "Contains" ), null, null );
		URI contains = bldr.getRelationUri( "Contains" );
		UriBuilder bldr2 = UriBuilder.getBuilder( contains );
		compare( "contains relation description",
				expected, test, bldr2.add( "Description" ).build(), null, null );
		compare( "contains relation extraprop",
				expected, test, bldr2.add( "extraprop" ).build(), null, null );
	}

	private void compareData( RepositoryConnection testRc, Model expected,
			UriBuilder owl, UriBuilder base ) throws Exception {
		List<Statement> stmts
				= Iterations.asList( testRc.getStatements( null, null, null, false ) );

		Model test = new LinkedHashModel( stmts );

		assertEquals( "dataset size", expected.size(), test.size() );

		compare( "category concept", expected, test, null, RDF.TYPE,
				owl.getConceptUri( "Category" ) );
		compare( "beverages concept", expected, test,
				base.getConceptUri().add( "Category" ).add( "Beverages" ).build(),
				null, null );
		compare( "dairy products", expected, test,
				null, RDFS.LABEL, new LiteralImpl( "Dairy Products" ) );
		compare( "chai-beverages link", expected, test,
				null, RDFS.LABEL, new LiteralImpl( "Chai:Beverages" ) );

		URI bev
				= base.getRelationUri().add( "Category" ).add( "Chai_x_Beverages" ).build();
		compare( "chai-beverages category", expected, test,
				bev, RDFS.SUBPROPERTYOF, owl.getRelationUri( "Category" ) );
	}

	private void compare( String label, Model expected, Model test, Resource s,
			URI p, Value o ) {
		Model exp = new LinkedHashModel( expected.filter( s, p, o ) );
		Model tst = new LinkedHashModel( test.filter( s, p, o ) );
		assertEquals( label + " size mismatch", exp.size(), tst.size() );
		assertEquals( label + " predicates mismatch", exp.predicates(), tst.predicates() );
		assertEquals( label + " subjects mismatch", exp.subjects(), tst.subjects() );
		assertEquals( label + " subjects mismatch", exp.objects(), tst.objects() );

	}
}
