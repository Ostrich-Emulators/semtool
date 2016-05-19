/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.edgemodelers;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineLoader;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import com.ostrichemulators.semtool.rdf.engine.util.QaChecker;
import com.ostrichemulators.semtool.util.DeterministicSanitizer;
import com.ostrichemulators.semtool.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class W3CEdgeModelerTest {

	private static final Logger log = Logger.getLogger( W3CEdgeModelerTest.class );
	private static final ValueFactory vf = new ValueFactoryImpl();
	private static final Date now;
	private static final String SCHEMA = "http://semoss.org/ontologies/";
	private static final String DATA = "http://va.gov/ontologies/";

	private static final File REL1 = new File( "src/test/resources/w3cedge-rel1.ttl" );
	private static final File REL2 = new File( "src/test/resources/w3cedge-rel2.ttl" );
	private static final File REL3 = new File( "src/test/resources/w3cedge-rel3.ttl" );
	private static final File META = new File( "src/test/resources/w3cedge-mm.ttl" );
	private static final File NODE = new File( "src/test/resources/w3cedge-node.ttl" );
	private static final File T608 = new File( "src/test/resources/w3cedge-608.ttl" );

	private QaChecker qaer;
	private InMemorySesameEngine engine;
	private EngineLoader loader;
	private LoadingSheetData rels;
	private LoadingSheetData nodes;
	private ImportData data;

	static {
		Calendar cal = Calendar.getInstance();
		cal.set( 2031, 9, 22, 6, 58, 59 );
		cal.set( Calendar.MILLISECOND, 15 );
		now = cal.getTime();
	}

	@BeforeClass
	public static void setUpClass() {
		// a deterministic sanitizer ensures we get repeatable results for URIs
		UriBuilder.setDefaultSanitizerClass( DeterministicSanitizer.class );
	}

	@Before
	public void setUp() throws RepositoryException {
		// this basically duplicates the data from test12.xlsx

		engine = InMemorySesameEngine.open();
		engine.setBuilders( UriBuilder.getBuilder( DATA ), UriBuilder.getBuilder( SCHEMA ) );
		engine.getRawConnection().setNamespace( "vcamp", DATA );
		engine.getRawConnection().setNamespace( "semoss", SCHEMA );
		engine.getRawConnection().setNamespace( RDFS.PREFIX, RDFS.NAMESPACE );
		engine.getRawConnection().setNamespace( RDF.PREFIX, RDF.NAMESPACE );
		engine.getRawConnection().setNamespace( OWL.PREFIX, OWL.NAMESPACE );
		engine.getRawConnection().setNamespace( XMLSchema.PREFIX, XMLSchema.NAMESPACE );

		loader = new EngineLoader();
		loader.setDefaultBaseUri( new URIImpl( "http://sales.data/purchases/2015" ),
				false );

		qaer = new QaChecker();

		rels = LoadingSheetData.relsheet( "Human Being", "Car", "Purchased" );
		rels.addProperties( Arrays.asList( "Price", "Date" ) );

		nodes = LoadingSheetData.nodesheet( "Human Being" );
		nodes.addProperties( Arrays.asList( "First Name", "Last Name" ) );

		data = EngineUtil2.createImportData( engine );
		data.add( rels );
		data.add( nodes );
	}

	@After
	public void tearDown() {
		engine.closeDB();
		loader.release();
		qaer.release();
	}

	private static Model getExpectedGraph( File rdf ) {
		SailRepository repo = new SailRepository( new MemoryStore() );
		RepositoryConnection expectedrc = null;
		List<Statement> stmts = new ArrayList<>();
		try {
			repo.initialize();
			expectedrc = repo.getConnection();
			expectedrc.add( rdf, null, RDFFormat.TURTLE );
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

	private static void compare( InMemorySesameEngine engine, File expected )
			throws IOException, RepositoryException, RDFHandlerException {
		compare( engine, expected, false );
	}

	private static void compare( InMemorySesameEngine engine, File expected,
			boolean doCountsOnly ) throws IOException, RepositoryException, RDFHandlerException {

		// get rid of the random database id
		engine.getRawConnection().remove((Resource) null, RDF.TYPE, SEMTOOL.Database );

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					expected.getName() ) ) ) ) {
				engine.getRawConnection().export( new TurtleWriter( w ) );
			}
		}

		Model model = getExpectedGraph( expected );
		List<Statement> stmts = Iterations.asList( engine.getRawConnection()
				.getStatements( null, null, null, false ) );

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

	@Test
	public void testAddRel1() throws Exception {
		Map<String, Value> props = new HashMap<>();
		props.put( "Price", vf.createLiteral( "3000 USD" ) );
		props.put( "Date", vf.createLiteral( now ) );
		LoadingNodeAndPropertyValues rel = rels.add( "Yuri", "Yugo", props );

		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		instance.createMetamodel( data, new HashMap<>(), engine.getRawConnection() );

		instance.addRel( rel, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, REL1 );
	}

	@Test
	public void testAddRel2() throws Exception {
		LoadingNodeAndPropertyValues rel = rels.add( "Alan", "Cadillac" );

		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		instance.createMetamodel( data, new HashMap<>(), engine.getRawConnection() );

		instance.addRel( rel, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, REL2 );
	}

	@Test
	public void testAddRel3() throws Exception {
		Map<String, Value> props = new HashMap<>();
		props.put( "Price", vf.createLiteral( "3000 USD" ) );
		props.put( "Date", vf.createLiteral( now ) );
		LoadingNodeAndPropertyValues rel1 = rels.add( "Yuri", "Yugo", props );
		LoadingNodeAndPropertyValues rel2 = rels.add( "Yuri", "Pinto" );

		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		instance.createMetamodel( data, new HashMap<>(), engine.getRawConnection() );

		instance.addRel( rel1, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );
		instance.addRel( rel2, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, REL3 );
	}

	@Test
	public void testCreateMetamodel() throws Exception {
		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		instance.createMetamodel( data, new HashMap<>(), engine.getRawConnection() );
		compare( engine, META );
	}

	@Test
	public void testAddNode() throws Exception {
		Map<String, Value> props = new HashMap<>();
		props.put( "First Name", vf.createLiteral( "Yuri" ) );
		props.put( "Last Name", vf.createLiteral( "Gagarin" ) );
		LoadingNodeAndPropertyValues node = nodes.add( "Yuri", props );

		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		instance.createMetamodel( data, new HashMap<>(), engine.getRawConnection() );

		instance.addNode( node, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, NODE );
	}

	@Test
	public void testTicket608() throws Exception {
		LoadingSheetData apples = LoadingSheetData.relsheet( "Person", "Apple", "likes" );
		LoadingNodeAndPropertyValues apple = apples.add( "John", "Golden Delicious" );

		LoadingSheetData oranges = LoadingSheetData.relsheet( "Person", "Orange", "hates" );
		LoadingNodeAndPropertyValues orange = oranges.add( "John", "Golden Delicious" );

		ImportData id = EngineUtil2.createImportData( engine );
		id.add( apples );
		id.add( oranges );

		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		instance.createMetamodel( id, new HashMap<>(), engine.getRawConnection() );

		instance.addRel( apple, new HashMap<>(), apples, id.getMetadata(),
				engine.getRawConnection() );
		instance.addRel( orange, new HashMap<>(), oranges, id.getMetadata(),
				engine.getRawConnection() );

		compare( engine, T608, true );
	}
}
