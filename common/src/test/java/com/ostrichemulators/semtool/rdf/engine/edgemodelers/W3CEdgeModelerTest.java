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
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class W3CEdgeModelerTest {

	private static final Logger log = Logger.getLogger( W3CEdgeModelerTest.class );
	private static final ValueFactory vf = SimpleValueFactory.getInstance();
	private static final Date now;
	private static final String SCHEMA = "http://semoss.org/ontologies/";
	private static final String DATA = "http://va.gov/ontologies/";

	private static final File REL1 = new File( "src/test/resources/w3cedge-rel1.ttl" );
	private static final File REL2 = new File( "src/test/resources/w3cedge-rel2.ttl" );
	private static final File REL3 = new File( "src/test/resources/w3cedge-rel3.ttl" );
	private static final File T608 = new File( "src/test/resources/w3cedge-608.ttl" );

	private QaChecker qaer;
	private InMemorySesameEngine engine;
	private EngineLoader loader;
	private LoadingSheetData rels;
	private LoadingSheetData nodes;
	private ImportData data;

	static {
		TimeZone.setDefault( TimeZone.getTimeZone( "GMT-04:00" ) );
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
		loader.setDefaultBaseUri( vf.createIRI( "http://sales.data/purchases/2015" ),
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
		qaer.release();
		engine.closeDB();
		loader.release();
	}

	private static Model getExpectedGraph( File rdf ) {
		SailRepository repo = new SailRepository( new MemoryStore() );
		RepositoryConnection expectedrc = null;
		List<Statement> stmts = new ArrayList<>();
		try {
			repo.init();
			expectedrc = repo.getConnection();
			expectedrc.add( rdf, null, RDFFormat.TURTLE );
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

	private static void compare( InMemorySesameEngine engine, File expected )
			throws IOException, RepositoryException, RDFHandlerException {
		compare( engine, expected, false );
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

		Model model = getExpectedGraph( expected );
		List<Statement> stmts = QueryResults.stream( engine.getRawConnection().getStatements( null, null,
					null, true ) ).collect( Collectors.toList() );

		assertEquals( model.size(), stmts.size() );

		if ( doCountsOnly ) {
			// do counts instead of checking exact URIs
		}
		else {
			for ( Statement s : stmts ) {
				assertTrue( "not in model: " + s.getSubject()
						+ "->" + s.getPredicate() + "->" + s.getObject().stringValue(),
						model.contains( s ) );
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
		Model model = instance.createMetamodel( data, new HashMap<>(), null );
		engine.getRawConnection().add( model );

		instance.addRel( rel, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, REL1 );
	}

	@Test
	public void testAddRel2() throws Exception {
		LoadingNodeAndPropertyValues rel = rels.add( "Alan", "Cadillac" );

		W3CEdgeModeler instance = new W3CEdgeModeler( qaer );
		Model model = instance.createMetamodel( data, new HashMap<>(), null );
		engine.getRawConnection().add( model );

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
		Model model = instance.createMetamodel( data, new HashMap<>(), null );
		engine.getRawConnection().add( model );

		instance.addRel( rel1, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );
		instance.addRel( rel2, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, REL3 );
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
		Model model = instance.createMetamodel( id, new HashMap<>(), null );
		engine.getRawConnection().add( model );

		instance.addRel( apple, new HashMap<>(), apples, id.getMetadata(),
				engine.getRawConnection() );
		instance.addRel( orange, new HashMap<>(), oranges, id.getMetadata(),
				engine.getRawConnection() );

		compare( engine, T608, true );
	}
}
