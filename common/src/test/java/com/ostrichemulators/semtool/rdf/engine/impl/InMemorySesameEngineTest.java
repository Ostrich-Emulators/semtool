/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.rdf.engine.impl.AbstractSesameEngine;
import com.ostrichemulators.semtool.rdf.engine.impl.InMemorySesameEngine;
import com.ostrichemulators.semtool.rdf.engine.api.MetadataConstants;
import static com.ostrichemulators.semtool.rdf.query.util.QueryExecutorAdapter.getDate;
import com.ostrichemulators.semtool.rdf.query.util.UpdateExecutorAdapter;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;
import info.aduna.iteration.Iterations;
import java.io.File;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class InMemorySesameEngineTest {

	private static final Logger log = Logger.getLogger( InMemorySesameEngineTest.class );
	private static final File DATA = new File( "src/test/resources/test12.nt" );
	public static final File LEGACY_QUESTIONS
			= new File( "src/test/resources/questions.prop" );
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
		eng = new InMemorySesameEngine();

		RepositoryConnection rc = eng.getRawConnection();
		rc.begin();
		rc.add( DATA, null, RDFFormat.NTRIPLES );
		rc.add( new StatementImpl( RDFS.DOMAIN, RDFS.LABEL,
				new LiteralImpl( "label" ) ) );

		rc.remove( eng.getBaseUri(), MetadataConstants.DCT_MODIFIED, null );
		rc.add( eng.getBaseUri(), MetadataConstants.DCT_MODIFIED,
				rc.getValueFactory().createLiteral( new Date() ) );

		rc.commit();
	}

	@After
	public void tearDown() {
		eng.closeDB();
	}

	@Test
	public void testUpdate() throws Exception {
		assertEquals( 41, eng.getRawConnection().size() );
		eng.update( new UpdateExecutorAdapter( "DELETE DATA { rdfs:domain rdfs:label \"label\" } " ) );
		assertEquals( 40, eng.getRawConnection().size() );
	}

	@Test
	public void testUpdateDate() throws Exception {
		Date date = new Date();
		assertEquals( 41, eng.getRawConnection().size() );
		eng.update( new UpdateExecutorAdapter( "DELETE DATA { rdfs:domain rdfs:label \"label\" } " ) );
		assertEquals( 40, eng.getRawConnection().size() );

		List<Statement> stmts = Iterations.asList( eng.getRawConnection().
				getStatements( eng.getBaseUri(), MetadataConstants.DCT_MODIFIED,
						null, false ) );
		Literal val = Literal.class.cast( stmts.get( 0 ).getObject() );
		Date upd = getDate( val.calendarValue() );
		assertTrue( upd.after( date ) );
	}

	@Test
	public void testUpdateDate2() throws Exception {
		Repository repo = new SailRepository( new MemoryStore() );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		UriBuilder urib = UriBuilder.getBuilder( Constants.ANYNODE + "/" );
		URI base = urib.uniqueUri();
		Date now = new Date();
		rc.add( new StatementImpl( base, MetadataConstants.DCT_MODIFIED,
				rc.getValueFactory().createLiteral( now ) ) );

		AbstractSesameEngine.updateLastModifiedDate( rc, null );

		List<Statement> stmts = Iterations.asList( eng.getRawConnection().
				getStatements( eng.getBaseUri(), MetadataConstants.DCT_MODIFIED,
						null, false ) );
		Literal val = Literal.class.cast( stmts.get( 0 ).getObject() );
		Date upd = getDate( val.calendarValue() );

		rc.close();
		repo.shutDown();

		// the 100 is to remove the ms, which aren't always the same because
		// they're not stored in the RDF
		assertEquals( now.getTime(), upd.getTime(), 100 );
	}
}
