/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.query.util;

import com.ostrichemulators.semtool.model.vocabulary.SEMTOOL;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.util.UriBuilder;
import org.junit.After;

/**
 *
 * @author ryan
 */
public class MetadataQueryTest {

	private InMemorySesameEngine engine;
	private final UriBuilder bldr = UriBuilder.getBuilder( "http://test.os-em.com/unit/" );

	@Before
	public void setUp() {
		engine = InMemorySesameEngine.open();
		try {
			RepositoryConnection rc = engine.getRawConnection();
			rc.add(bldr.toIRI(), RDF.TYPE, SEMTOOL.Database );
		}
		catch ( Exception e ) {
		}
	}

	@After
	public void tearDown() {
		engine.closeDB();
	}

	@Test
	public void testHandleTupleExists() throws Exception {
		ValueFactory vf = engine.getRawConnection().getValueFactory();
		String raw = "The Team!";

		engine.getRawConnection().add( bldr.toIRI(), MetadataConstants.DCT_CREATOR,
				vf.createLiteral( raw ) );
		MetadataQuery mq = new MetadataQuery();
		engine.query( mq );
		Map<IRI, String> data = mq.asStrings();
		assertTrue( 2 == data.size() );
		assertEquals( raw, data.get( MetadataConstants.DCT_CREATOR ) );
	}

	@Test
	public void testHandleTupleUpgradeDcToDct() throws Exception {
		ValueFactory vf = engine.getRawConnection().getValueFactory();
		String raw = "old publisher predicate";
		engine.getRawConnection().add( bldr.toIRI(), DC.PUBLISHER,
				vf.createLiteral( raw ) );
		MetadataQuery mq = new MetadataQuery();
		engine.query( mq );
		Map<IRI, String> data = mq.asStrings();

		assertEquals( raw, data.get( MetadataConstants.DCT_PUBLISHER ) );
		assertTrue( 2 == data.size() );
	}

	@Test
	public void testHandleTupleNonUpgradeDcToDct() throws Exception {
		ValueFactory vf = engine.getRawConnection().getValueFactory();
		String raw = "should be a date, but that's okay";
		engine.getRawConnection().add( bldr.toIRI(), MetadataConstants.DCT_CREATED,
				vf.createLiteral( raw ) );
		MetadataQuery mq = new MetadataQuery();
		engine.query( mq );
		Map<IRI, String> data = mq.asStrings();

		assertEquals( raw, data.get( MetadataConstants.DCT_CREATED ) );
		assertTrue( 2 == data.size() );
	}

	@Test
	public void testHandleTupleMissingDc() throws Exception {
		ValueFactory vf = engine.getRawConnection().getValueFactory();
		String raw = "desc";
		engine.getRawConnection().add( bldr.toIRI(), MetadataConstants.DCT_DESC,
				vf.createLiteral( raw ) );
		MetadataQuery mq = new MetadataQuery();
		engine.query( mq );
		Map<IRI, String> data = mq.asStrings();

		assertFalse( data.containsKey( MetadataConstants.DCT_CREATED ) );
		assertTrue( 2 == data.size() );
	}
}
