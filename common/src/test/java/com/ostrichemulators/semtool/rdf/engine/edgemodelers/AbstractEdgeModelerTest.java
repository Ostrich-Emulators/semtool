/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.edgemodelers;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.poi.main.ImportData;
import com.ostrichemulators.semtool.poi.main.ImportMetadata;
import com.ostrichemulators.semtool.poi.main.LoadingSheetData;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
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
public class AbstractEdgeModelerTest {

	private static final Logger log = Logger.getLogger( SemtoolEdgeModelerTest.class );
	private static final ValueFactory vf = SimpleValueFactory.getInstance();
	private static final String SCHEMA = "http://os-em.com/ontologies/semtool/test-onto/";
	private static final String DATA = "http://os-em.com/ontologies/semtool/test-data/";

	private static final File META = new File( "src/test/resources/semossedge-mm.ttl" );
	private static final File NODE = new File( "src/test/resources/semossedge-node.ttl" );

	private QaChecker qaer;
	private InMemorySesameEngine engine;
	private EngineLoader loader;
	private LoadingSheetData rels;
	private LoadingSheetData nodes;
	private ImportData data;

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
		engine.getRawConnection().setNamespace( "data", DATA );
		engine.getRawConnection().setNamespace( "schema", SCHEMA );
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
		List<Statement> stmts = QueryResults.stream( engine.getRawConnection()
				.getStatements( null, null, null, false ) ).collect( Collectors.toList() );
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
	public void testCreateMetamodel() throws Exception {
		TestModeler instance = new TestModeler( qaer );
		Model mod = instance.createMetamodel( data, new HashMap<>(), null );
		engine.getRawConnection().add( mod );
		compare( engine, META );
	}

	@Test
	public void testAddNode() throws Exception {
		Map<String, Value> props = new HashMap<>();
		props.put( "First Name", vf.createLiteral( "Yuri" ) );
		props.put( "Last Name", vf.createLiteral( "Gagarin" ) );
		LoadingSheetData.LoadingNodeAndPropertyValues node = nodes.add( "Yuri", props );

		TestModeler instance = new TestModeler( qaer );
		Model model = instance.createMetamodel( data, new HashMap<>(), SimpleValueFactory.getInstance() );
		engine.getRawConnection().add( model );

		instance.addNode( node, new HashMap<>(), rels, data.getMetadata(),
				engine.getRawConnection() );

		compare( engine, NODE );
	}

	public class TestModeler extends AbstractEdgeModeler {

		public TestModeler( QaChecker qa ) {
			super( qa );
		}

		@Override
		public IRI addRel( LoadingSheetData.LoadingNodeAndPropertyValues nap, Map<String, String> namespaces, LoadingSheetData sheet, ImportMetadata metas, RepositoryConnection rc ) throws RepositoryException {
			throw new UnsupportedOperationException( "not yet implemented" );
		}
	}
}
