/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.rdf.query.util.UpdateExecutorAdapter;
import java.io.File;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

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

		rc.commit();
	}

	@After
	public void tearDown() {
		eng.closeDB();
	}

	@Test
	public void testUpdate() throws Exception {
		assertEquals( 40, eng.getRawConnection().size() );
		eng.update( new UpdateExecutorAdapter( "DELETE DATA { rdfs:domain rdfs:label \"label\" } " ) );
		assertEquals( 39, eng.getRawConnection().size() );
	}
}
