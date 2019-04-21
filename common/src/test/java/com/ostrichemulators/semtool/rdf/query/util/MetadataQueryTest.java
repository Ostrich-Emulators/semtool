/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class MetadataQueryTest {

	private static InMemorySesameEngine eng;

	public MetadataQueryTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		eng = InMemorySesameEngine.open();
		
		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL, new LiteralImpl( "test" ) ) );
		// DC.PULISHER should get silently upgraded to MetadataConstants.DCT_PUBLISHER
		rc.add( new StatementImpl( eng.getBaseIri(), DC.PUBLISHER,
				new LiteralImpl( "me" ) ) );
		rc.add(new StatementImpl( eng.getBaseIri(), RDF.TYPE, SEMTOOL.Database ) );
		rc.commit();
	}

	@AfterClass
	public static void tearDownClass() {
		eng.closeDB();
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testAsStrings() {
		MetadataQuery mq = new MetadataQuery( SEMTOOL.Database );
		eng.queryNoEx( mq );
		assertEquals( 1, mq.asStrings().size() );
		assertEquals(eng.getBaseIri().stringValue(), mq.asStrings().get(SEMTOOL.Database ) );
	}

	@Test
	public void testSilentUpgrade() {
		MetadataQuery mq = new MetadataQuery( DC.PUBLISHER );
		eng.queryNoEx( mq );
		Map<URI, Value> results = mq.getResults();
		assertEquals( new HashSet<>( Arrays.asList( MetadataConstants.DCT_PUBLISHER ) ), 
				results.keySet() );
	}

	@Test
	public void testGetOne2() {
		MetadataQuery mq = new MetadataQuery( DC.PUBLISHER );
		eng.queryNoEx( mq );
		assertEquals( "me", mq.getOne().stringValue() );
	}
}
