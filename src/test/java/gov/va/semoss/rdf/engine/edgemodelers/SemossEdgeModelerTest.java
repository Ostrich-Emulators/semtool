/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.edgemodelers;

import gov.va.semoss.poi.main.ImportData;
import gov.va.semoss.poi.main.ImportMetadata;
import gov.va.semoss.poi.main.LoadingSheetData;
import gov.va.semoss.poi.main.LoadingSheetData.LoadingNodeAndPropertyValues;
import gov.va.semoss.rdf.engine.impl.InMemorySesameEngine;
import gov.va.semoss.rdf.engine.util.EngineLoader;
import gov.va.semoss.rdf.engine.util.QaChecker;
import gov.va.semoss.util.DeterministicSanitizer;
import gov.va.semoss.util.UriBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 *
 * @author ryan
 */
public class SemossEdgeModelerTest {

	private static final Logger log = Logger.getLogger( SemossEdgeModelerTest.class );
	private static final ValueFactory vf = new ValueFactoryImpl();
	private static final Date now = new Date();
	private QaChecker qaer;
	private InMemorySesameEngine engine;
	private ImportMetadata metadata;
	private EngineLoader loader;
	private LoadingSheetData rels;
	private LoadingSheetData nodes;
	private LoadingNodeAndPropertyValues rel;
	private LoadingNodeAndPropertyValues node;
	private ImportData data;

	@BeforeClass
	public static void setUpClass() {
		// a deterministic sanitizer ensures we get repeatable results for URIs
		UriBuilder.setDefaultSanitizerClass( DeterministicSanitizer.class );
	}

	@Before
	public void setUp() throws RepositoryException {
		engine = new InMemorySesameEngine();
		engine.setBuilders( UriBuilder.getBuilder( "http://sales.data/purchases#" ),
				UriBuilder.getBuilder( "http://sales.data/schema#" ) );
		engine.getRawConnection().setNamespace( "vcamp", "http://sales.data/purchases#" );
		engine.getRawConnection().setNamespace( "semoss", "http://sales.data/schema#" );

		loader = new EngineLoader();
		loader.setDefaultBaseUri( new URIImpl( "http://sales.data/purchases/2015" ),
				false );

		qaer = new QaChecker();

		rels = LoadingSheetData.relsheet( "Human Being", "Car", "Purchased" );
		rels.addProperties( Arrays.asList( "Price", "Date" ) );

		Map<String, Value> relprops = new HashMap<>();
		relprops.put( "Price", vf.createLiteral( "3000 USD" ) );
		relprops.put( "Date", vf.createLiteral( now ) );
		rel = rels.add( "Yuri", "Yugo", relprops );

		nodes = LoadingSheetData.nodesheet( "Human Being" );
		nodes.addProperties( Arrays.asList( "First Name", "Last Name" ) );
		Map<String, Value> nodeprops = new HashMap<>();
		nodeprops.put( "First Name", vf.createLiteral( "Yuri" ) );
		nodeprops.put( "Last Name", vf.createLiteral( "Gargarin" ) );
		node = nodes.add( "Yuri", nodeprops );

		data = ImportData.forEngine( engine );
		data.add( rels );
		data.add( nodes );
	}

	@After
	public void tearDown() {
		engine.closeDB();
		loader.release();
	}

	@Test
	public void testAddRel() throws Exception {

		SemossEdgeModeler instance = new SemossEdgeModeler( qaer );
		instance.createMetamodel( data, new HashMap<>(), engine.getRawConnection() );

		instance.addRel( rel, new HashMap<>(), rels, metadata, engine.getRawConnection() );

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"semossedge-rel.ttl" ) ) ) ) {
				engine.getRawConnection().export( new TurtleWriter( w ) );
			}
		}
	}

	// @Test
	public void testAddNode() throws Exception {
		System.out.println( "addNode" );
		LoadingSheetData.LoadingNodeAndPropertyValues nap = null;
		Map<String, String> namespaces = null;
		LoadingSheetData sheet = null;
		ImportMetadata metas = null;
		RepositoryConnection myrc = null;
		SemossEdgeModeler instance = new SemossEdgeModeler();
		URI expResult = null;
		URI result = instance.addNode( nap, namespaces, sheet, metas, myrc );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	// @Test
	public void testAddProperties() throws Exception {
		System.out.println( "addProperties" );
		URI subject = null;
		Map<String, Value> properties = null;
		Map<String, String> namespaces = null;
		LoadingSheetData sheet = null;
		ImportMetadata metas = null;
		RepositoryConnection myrc = null;
		SemossEdgeModeler instance = new SemossEdgeModeler();
		instance.addProperties( subject, properties, namespaces, sheet, metas, myrc );
		fail( "The test case is a prototype." );
	}

}
