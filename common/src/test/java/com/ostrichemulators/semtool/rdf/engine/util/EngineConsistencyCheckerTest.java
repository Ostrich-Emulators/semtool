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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

/**
 *
 * @author ryan
 */
public class EngineConsistencyCheckerTest {
	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static final Logger log = Logger.getLogger( EngineConsistencyCheckerTest.class );
	private static final File LOADFILE = new File( "src/test/resources/test12.nt" );
	private static final UriBuilder datab = UriBuilder.getBuilder( "http://os-em.com/semtool/database/Ke42d9335-1c26-475a-96bd-9bde6a2ab5e5" );
	private static final IRI CAR = VF.createIRI( "http://os-em.com/ontologies/semtool/Car" );
	private static final IRI YUGO = datab.build( "Yugo" );
	private static final IRI YIGO = datab.build( "Yigo" );
	private static final IRI YUGO2 = datab.build( "Yugah" );
	private static final IRI YUGO3 = datab.build( "Yugaoh" );

	private static final IRI PURCHASE = VF.createIRI( "http://os-em.com/ontologies/semtool/Purchased" );
	private static final IRI REL = VF.createIRI( "http://os-em.com/ontologies/semtool/Relation" );
	private static final IRI REL1 = datab.build( "Yuri_Purchased_Yugo" );
	private static final IRI REL2 = datab.build( "Yuri_Purchased_Yigo" );

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

		for ( IRI extra : new IRI[]{ YIGO, YUGO2, YUGO3 } ) {
			rc.add( VF.createStatement( extra, RDF.TYPE, CAR ) );
			rc.add( VF.createStatement( extra, RDFS.LABEL,
					VF.createLiteral( extra.getLocalName() ) ) );
		}

		//rc.add( VF.createStatement( REL2, RDF.TYPE, REL ) );
		rc.add( VF.createStatement( REL2, RDFS.LABEL, VF.createLiteral( "Yuri Purchased a Yigo" ) ) );
		rc.add( VF.createStatement( REL2, RDFS.SUBPROPERTYOF, PURCHASE ) );
		rc.add( VF.createStatement( REL2, VF.createIRI( "http://os-em.com/ontologies/semtool/Price" ),
				VF.createLiteral( "8000 USD" ) ) );

		rc.remove( REL1, null, null );
		rc.add( VF.createStatement( REL1, RDFS.LABEL, VF.createLiteral( "Yuri Purchased Yugo" ) ) );
		rc.add( VF.createStatement( REL1, RDFS.SUBPROPERTYOF, PURCHASE ) );
		rc.add( VF.createStatement( REL1, VF.createIRI( "http://os-em.com/ontologies/semtool/Price" ),
				VF.createLiteral( "3000 USD" ) ) );

		rc.commit();

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					"ecctest.ttl" ) ) ) ) {
				engine.getRawConnection().export( new TurtleWriter( w ) );
			}
		}
	}

	@AfterClass
	public static void tearDownClass() {
		engine.closeDB();
	}

	@Before
	public void setUp() {
		ecc = new EngineConsistencyChecker( engine, false, new LevenshteinDistance() );
	}

	@After
	public void tearDown() {
		ecc.release();
	}

	@Test
	public void testAdd() {
		ecc.add( Arrays.asList( CAR ), EngineConsistencyChecker.Type.CONCEPT );
		ecc.add( Arrays.asList( PURCHASE ), EngineConsistencyChecker.Type.RELATIONSHIP );
		assertEquals( 5, ecc.getItemsForType( CAR ) );
		assertEquals( 2, ecc.getItemsForType( PURCHASE ) );
	}

	@Test
	public void testCheck() {
		ecc.add( Arrays.asList( CAR ), EngineConsistencyChecker.Type.CONCEPT );
		ecc.add( Arrays.asList( PURCHASE ), EngineConsistencyChecker.Type.RELATIONSHIP );

		MultiMap<IRI, Hit> hits = ecc.check( CAR, 0.8f );

		assertEquals( 2, hits.size() );
		assertEquals( 1, hits.getNN( YUGO2 ).size() );

		hits = ecc.check( CAR, 0.6f );
		assertEquals( 4, hits.size() );
		// if this assertion fails, make sure the test12.nt file has the
		// right database name (it changes everytime the db is regenerated)
		assertEquals( 2, hits.getNN( YUGO ).size() );

		hits = ecc.check( PURCHASE, 0.8f );
		assertEquals( 2, hits.size() );
	}

}
