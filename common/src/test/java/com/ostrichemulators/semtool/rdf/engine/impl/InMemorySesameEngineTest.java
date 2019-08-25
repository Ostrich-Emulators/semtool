/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import static com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter.getDate;
import com.ostrichemulators.semtool.rdf.query.util.UpdateExecutorAdapter;
import com.ostrichemulators.semtool.util.Utility;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class InMemorySesameEngineTest {

	private static final Logger log = Logger.getLogger( InMemorySesameEngineTest.class );
	private static final File DATA = new File( "src/test/resources/test12.nt" );
	private InMemorySesameEngine eng;

	public InMemorySesameEngineTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		eng = InMemorySesameEngine.open();

		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( DATA, null, RDFFormat.NTRIPLES );
		rc.add( rc.getValueFactory().createStatement( RDFS.DOMAIN, RDFS.LABEL,
				rc.getValueFactory().createLiteral( "label" ) ) );

		rc.remove( eng.getBaseIri(), MetadataConstants.DCT_MODIFIED, null );
		rc.add( eng.getBaseIri(), MetadataConstants.DCT_MODIFIED,
				rc.getValueFactory().createLiteral( new Date() ) );

		rc.commit();
	}

	@After
	public void tearDown() {
		eng.closeDB();
	}

	@Test
	public void testUpdate() throws Exception {
		assertEquals( 95, eng.getRawConnection().size() );
		eng.update( new UpdateExecutorAdapter( "DELETE DATA { rdfs:domain rdfs:label \"label\" } " ) );
		assertEquals( 94, eng.getRawConnection().size() );
	}

	@Test
	public void testUpdateDate() throws Exception {
		Date date = new Date();
		assertEquals( 95, eng.getRawConnection().size() );
		eng.update( new UpdateExecutorAdapter( "DELETE DATA { rdfs:domain rdfs:label \"label\" } " ) );
		assertEquals( 94, eng.getRawConnection().size() );

		List<Statement> stmts = QueryResults.stream( eng.getRawConnection().
				getStatements( eng.getBaseIri(), MetadataConstants.DCT_MODIFIED,
						null, false ) ).collect( Collectors.toList() );
		Literal val = Literal.class.cast( stmts.get( 0 ).getObject() );
		Date upd = getDate( val.calendarValue() );
		assertTrue( upd.after( date ) );
	}

	@Test
	public void testUpdateDate2() throws Exception {
		Repository repo = new SailRepository( new MemoryStore() );
		repo.init();
		Date now;
		Date upd;
		try (RepositoryConnection rc = repo.getConnection()) {
			IRI base = Utility.getUniqueIri();
			now = new Date();
			rc.add( rc.getValueFactory().createStatement( base, MetadataConstants.DCT_MODIFIED,
					rc.getValueFactory().createLiteral( now ) ) );
			AbstractSesameEngine.updateLastModifiedDate( rc, null );
			List<Statement> stmts = QueryResults.stream( eng.getRawConnection().
					getStatements( eng.getBaseIri(), MetadataConstants.DCT_MODIFIED,
							null, false ) ).collect( Collectors.toList() );
			Literal val = Literal.class.cast( stmts.get( 0 ).getObject() );
			upd = getDate( val.calendarValue() );
		}
		repo.shutDown();

		// the 100 is to remove the ms, which aren't always the same because
		// they're not stored in the RDF
		assertEquals( now.getTime(), upd.getTime(), 100 );
	}
}
