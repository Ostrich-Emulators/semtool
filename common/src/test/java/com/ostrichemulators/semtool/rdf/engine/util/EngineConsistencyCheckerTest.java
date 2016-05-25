/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.util;

import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.rdf.engine.util.EngineConsistencyChecker.Hit;
import com.ostrichemulators.semtool.util.MultiMap;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
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
public class EngineConsistencyCheckerTest {

	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab = UriBuilder.getBuilder( "http://os-em.com/semtool/database/l2129784d-e281-45af-a69f-1650aff8bc33" );
	private static final URI CAR = new URIImpl( "http://os-em.com/ontologies/semtool/Car" );
	private static final URI YUGO = datab.build( "Yugo" );
	private static final URI YIGO = datab.build( "Yigo" );
	private static final URI YUGO2 = datab.build( "Yugah" );
	private static final URI YUGO3 = datab.build( "Yugaoh" );

	private static final URI PURCHASE = new URIImpl( "http://os-em.com/ontologies/semtool/Purchased" );
	private static final URI REL = new URIImpl( "http://os-em.com/ontologies/semtool/Relation" );
	private static final URI REL1 = datab.build( "Yuri_Purchased_Yugo" );
	private static final URI REL2 = datab.build( "Yuri_Purchased_Yigo" );

	private static InMemorySesameEngine engine;
	private EngineConsistencyChecker ecc;

	public EngineConsistencyCheckerTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		engine = InMemorySesameEngine.open();

		engine.setBuilders( datab, UriBuilder.getBuilder( "http://os-em.com/ontologies/semtool/" ) );

		RepositoryConnection rc = engine.getRawConnection();
		rc.begin();
		rc.add( LOADFILE, null, RDFFormat.NTRIPLES );

		for ( URI extra : new URI[]{ YIGO, YUGO2, YUGO3 } ) {
			rc.add( new StatementImpl( extra, RDF.TYPE, CAR ) );
			rc.add( new StatementImpl( extra, RDFS.LABEL,
					new LiteralImpl( extra.getLocalName() ) ) );
		}

		//rc.add( new StatementImpl( REL2, RDF.TYPE, REL ) );
		rc.add( new StatementImpl( REL2, RDFS.LABEL, new LiteralImpl( "Yuri Purchased a Yigo" ) ) );
		rc.add( new StatementImpl( REL2, RDF.TYPE, PURCHASE ) );
		rc.add( new StatementImpl( REL2, new URIImpl( "http://os-em.com/ontologies/semtool/Price" ),
				new LiteralImpl( "8000 USD" ) ) );

		rc.remove(  REL1, null, null );
		//rc.add( new StatementImpl( REL1, RDFS.SUBPROPERTYOF, REL ) );
		rc.add( new StatementImpl( REL1, RDFS.LABEL, new LiteralImpl( "Yuri Purchased Yugo" ) ) );
		rc.add( new StatementImpl( REL1, RDF.TYPE, PURCHASE ) );
		rc.add( new StatementImpl( REL1, new URIImpl( "http://os-em.com/ontologies/semtool/Price" ),
				new LiteralImpl( "3000 USD" ) ) );

		rc.commit();

//		try( FileWriter gw = new FileWriter( "/tmp/x.nt" ) ){
//			rc.export( new NTriplesWriter( gw ) );
//		}
	}

	@AfterClass
	public static void tearDownClass() {
		engine.closeDB();
	}

	@Before
	public void setUp() {
		ecc = new EngineConsistencyChecker( engine, false, new LevensteinDistance() );
	}

	@After
	public void tearDown() {
		ecc.release();
	}

	@Test
	public void testAdd() {
		ecc.add( Arrays.asList( CAR ), EngineConsistencyChecker.Type.CONCEPT );
		ecc.add( Arrays.asList( PURCHASE ), EngineConsistencyChecker.Type.RELATIONSHIP );
		assertEquals( 4, ecc.getItemsForType( CAR ) );
		assertEquals( 2, ecc.getItemsForType( PURCHASE ) );
	}

	@Test
	public void testCheck() {
		ecc.add( Arrays.asList( CAR ), EngineConsistencyChecker.Type.CONCEPT );
		ecc.add( Arrays.asList( PURCHASE ), EngineConsistencyChecker.Type.RELATIONSHIP );

		MultiMap<URI, Hit> hits = ecc.check( CAR, 0.8f );

		assertEquals( 2, hits.size() );
		assertEquals( 1, hits.getNN( YUGO2 ).size() );

		hits = ecc.check( CAR, 0.6f );
		assertEquals( 4, hits.size() );
		assertEquals( 2, hits.getNN( YUGO ).size() );

		hits = ecc.check( PURCHASE, 0.8f );
		assertEquals( 2, hits.size() );
	}

}
