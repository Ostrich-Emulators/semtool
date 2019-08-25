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
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class MetadataQueryTest {

	private static final ValueFactory VF = SimpleValueFactory.getInstance();
	private static InMemorySesameEngine eng;

	public MetadataQueryTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		eng = InMemorySesameEngine.open();

		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( VF.createStatement( RDFS.DOMAIN, RDFS.LABEL, VF.createLiteral( "test" ) ) );
		// DC.PULISHER should get silently upgraded to MetadataConstants.DCT_PUBLISHER
		rc.add( VF.createStatement( eng.getBaseIri(), DC.PUBLISHER,
				VF.createLiteral( "me" ) ) );
		rc.add( VF.createStatement( eng.getBaseIri(), RDF.TYPE, SEMTOOL.Database ) );
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
		assertEquals( eng.getBaseIri().stringValue(), mq.asStrings().get( SEMTOOL.Database ) );
	}

	@Test
	public void testSilentUpgrade() {
		MetadataQuery mq = new MetadataQuery( DC.PUBLISHER );
		eng.queryNoEx( mq );
		Map<IRI, Value> results = mq.getResults();
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
