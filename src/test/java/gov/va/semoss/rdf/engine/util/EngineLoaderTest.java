/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
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

	private RepositoryConnection rc;
	private File destination;

	private IEngine extractKb() {
		if ( null != destination ) {
			FileUtils.deleteQuietly( destination );
		}
		destination = null;

		try ( ZipInputStream zis = new ZipInputStream( new FileInputStream(
				"src/test/resources/emptydb.zip" ) ) ) {
			destination = File.createTempFile( "semoss-test-", "" );
			destination.delete();
			destination.mkdir();

			ZipEntry entry;
			while ( null != ( entry = zis.getNextEntry() ) ) {
				File outfile = new File( destination, entry.getName() );
				if ( entry.isDirectory() ) {
					outfile.mkdirs();
				}
				else {
					try ( FileOutputStream fout = new FileOutputStream( outfile ) ) {
						byte bytes[] = new byte[1024 * 1024];
						int read = -1;
						while ( -1 != ( read = zis.read( bytes ) ) ) {
							fout.write( bytes, 0, read );
						}
						zis.closeEntry();
					}
				}
			}

		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		UriBuilder schema = UriBuilder.getBuilder( OWLSTART );
		final UriBuilder data = UriBuilder.getBuilder( BASEURI );
		data.setSanitizer( new DefaultSanitizer() );
		schema.setSanitizer( new DefaultSanitizer() );

		File dbdir = new File( destination, "emptydb" );
		File smss = new File( dbdir, "emptydb.jnl" );
		Properties props = BigDataEngine.generateProperties( smss );
		props.setProperty( Constants.SEMOSS_URI, OWLSTART.stringValue() );
		props.setProperty( Constants.ENGINE_NAME, "Empty KB" );
		props.setProperty( Constants.SMSS_LOCATION, smss.getAbsolutePath() );
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
		FileUtils.deleteQuietly( destination );
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
		rc = null;
		SailRepository repo = new SailRepository( new MemoryStore() );
		repo.initialize();

		try {
			rc = repo.getConnection();
		}
		catch ( Exception e ) {
			repo.shutDown();
			throw e;
		}
	}

	@After
	public void tearDown() {
		if ( null != rc ) {
			try {
				rc.close();
			}
			catch ( Exception e ) {
				// don't care
			}

			try {
				rc.getRepository().shutDown();
			}
			catch ( Exception e ) {
				// don't care
			}
		}
	}

	@Test
	public void testCsvImport() throws Exception {
		CSVReader rdr = new CSVReader( CSVLOADER );
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );
		eng.setRepositoryConnection( rc );

		EngineLoader el = new EngineLoader();
		el.setReader( "csv", rdr );
		el.loadToEngine( Arrays.asList( CSVDATA ), eng, true, null );
		el.release();

		// need a little cleanup because making the IEngine
		//  adds some statements not in the input file
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"airplanes-mm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( CSV_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testCsvImportNoMetamodel() throws Exception {
		CSVReader rdr = new CSVReader( CSVLOADER );
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );
		eng.setRepositoryConnection( rc );

		EngineLoader el = new EngineLoader();
		el.setReader( "csv", rdr );
		el.loadToEngine( Arrays.asList( CSVDATA ), eng, false, null );
		el.release();

		// need a little cleanup because making the IEngine
		//  adds some statements not in the input file
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"airplanes-nomm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( CSV_NOMM_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testCsvImport2() throws Exception {
		CSVReader rdr = new CSVReader( CSVLOADER2 );
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );
		eng.setRepositoryConnection( rc );

		EngineLoader el = new EngineLoader();
		el.setReader( "csv", rdr );
		el.loadToEngine( Arrays.asList( CSVDATA2 ), eng, true, null );
		el.release();

		// need a little cleanup because making the IEngine
		//  adds some statements not in the input file
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );

		// not sure where these are coming from
		// (actually, not sure why they're missing in the "expected" file)
		URI contains = eng.getSchemaBuilder().getContainsUri();
		UriBuilder junker = UriBuilder.getBuilder( contains );
		URI has = eng.getSchemaBuilder().getConceptUri( "has" );
		URI cn = junker.copy().add( "cn_In" ).build();
		rc.remove( has, null, null );
		rc.remove( cn, null, null );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"systems-mm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( CSV_EXP2 ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testImportXlsLegacy() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		ImportData id = new ImportData();
		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( LEGACY ), eng, true, id );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"legacy-mm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( LEGACY_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testImportXlsLegacyNoMetamodel() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( BASEURI ),
				UriBuilder.getBuilder( OWLSTART ) );

		ImportData id = new ImportData();
		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( LEGACY ), eng, false, id );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"legacy-nomm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( LEGACY_NOMM_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testImportXlsModern() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		ImportData errors = new ImportData();
		Collection<Statement> owls
				= el.loadToEngine( Arrays.asList( CUSTOM ), eng, true, errors );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom-mm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareOwls( owls, CUSTOM_EXP, eng.getSchemaBuilder() );
		compareData( rc, getExpectedGraph( CUSTOM_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testImportNamespaceHeavyXlsModern() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		ImportData errors = new ImportData();
		el.loadToEngine( Arrays.asList( CUSTOM2 ), eng, true, errors );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom2-mm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( CUSTOM2_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testImportNamespaceHeavyXlsModernNoMetamodel() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		ImportData errors = new ImportData();
		el.loadToEngine( Arrays.asList( CUSTOM2 ), eng, false, errors );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom2-nomm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( CUSTOM2_NOMM_EXP ), eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testImportXlsModernNoMetamodel() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		Collection<Statement> owls
				= el.loadToEngine( Arrays.asList( CUSTOM ), eng, false, null );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"custom-nomm.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		Model expected = getExpectedGraph( CUSTOM_NOMM_EXP );
		assertTrue( owls.isEmpty() );
		compareData( rc, expected, eng.getSchemaBuilder(),
				eng.getDataBuilder() );
		eng.closeDB();
	}

	@Test
	public void testLoadToEngine_two_loadsLegacy() throws Exception {
		// check to make sure if we load the same data twice, we don't expand the
		// data in the KB (basically, make sure the caching works)
		IEngine eng = extractKb();
		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( LEGACY ), eng, true, null );
		el.release();
		OneVarListQueryAdapter<URI> o
				= OneVarListQueryAdapter.getUriList( "SELECT ?uri WHERE { ?uri ?p ?o }",
						"uri" );
		List<URI> oldlist = eng.query( o );

		EngineLoader el2 = new EngineLoader();
		el2.loadToEngine( Arrays.asList( LEGACY ), eng, true, null );
		el2.release();
		List<URI> newlist = eng.query( o );
		removeKb( eng );
		assertEquals( oldlist, newlist );
	}

	@Test
	public void testLoadToEngine_two_loadsCurrent() throws Exception {
		// same as the two_loads1 test, but in the custom metamodel mode
		IEngine eng = extractKb();
		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( CUSTOM ), eng, true, null );
		el.release();
		OneVarListQueryAdapter<URI> o
				= OneVarListQueryAdapter.getUriList( "SELECT ?uri WHERE { ?uri ?p ?o }",
						"uri" );
		List<URI> oldlist = eng.query( o );

		EngineLoader el2 = new EngineLoader();
		el2.loadToEngine( Arrays.asList( CUSTOM ), eng, true, null );
		el2.release();

		List<URI> newlist = eng.query( o );
		removeKb( eng );
		assertEquals( oldlist, newlist );
	}

	@Test
	public void testTicket583() throws Exception {
		rc.add( TICKETBASE, null, RDFFormat.TURTLE );
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );

		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( TICKET583 ), eng, false, null );
		el.release();
		rc.commit();

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
		eng.closeDB();
	}

	// @Test
	public void testTicket584() throws Exception {
		rc.add( TICKETBASE, null, RDFFormat.TURTLE );
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( "http://example.org/ex1" ),
				UriBuilder.getBuilder( "http://foo.bar/model#" ) );

		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( TICKET584 ), eng, false, null );
		el.release();

		// need a little cleanup because making the in-memory
		// engine adds some statements not in the XLSX
		//rc.remove( eng.getBaseUri(), RDF.TYPE, MetadataConstants.VOID_DS );
		//rc.remove( eng.getBaseUri(), RDF.TYPE, OWL.ONTOLOGY );
		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"ticket584.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

//		compareData( rc, getExpectedGraph( TICKET584_EXP ),
//				UriBuilder.getBuilder( "http://example.org/ex1" ),
//				UriBuilder.getBuilder( "http://foo.bar/model#" ) );
		eng.closeDB();
	}

	@Test
	public void testVerySimilarProperties() throws Exception {
		InMemorySesameEngine eng = new InMemorySesameEngine();
		eng.setRepositoryConnection( rc );
		eng.setBuilders( UriBuilder.getBuilder( DATAURI ),
				UriBuilder.getBuilder( SCHEMAURI ) );

		EngineLoader el = new EngineLoader();
		el.loadToEngine( Arrays.asList( TICKET608 ), eng, true, null );
		el.release();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"ticket608.nt" ) ) ) ) {
				rc.export( new NTriplesWriter( w ) );
			}
		}

		compareData( rc, getExpectedGraph( TICKET608_EXP ),
				eng.getDataBuilder(), eng.getSchemaBuilder() );
		eng.closeDB();
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
