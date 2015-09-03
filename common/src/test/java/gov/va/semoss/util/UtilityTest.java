/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import info.aduna.iteration.Iterations;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class UtilityTest {

	public UtilityTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	//@Test
	public void testGetParams() {
	}

	//@Test
	public void testGetParamTypeHash() {
	}

	//@Test
	public void testNormalizeParam() {
	}

	//@Test
	public void testFillParam() {
	}

	@Test
	public void testGetInstanceName() {
		Map<String, String> tests = new HashMap<>();
		tests.put( "http://www.google.com/name/one", "one" );
		tests.put( "http://www.google.com/name#two", "name#two" );
		tests.put( "http://www.google.com/name/one/", "one" );
		tests.put( "http://www.google.com/name/one#", "one#" );
		tests.put( "something strange", "something strange" );

		for ( Map.Entry<String, String> e : tests.entrySet() ) {
			String rslt = Utility.getInstanceName( e.getKey() );
			assertEquals( "failed to parse: " + e.getKey(), e.getValue(), rslt );
		}
	}

	//@Test
	public void testGetConceptType() {
	}

	@Test
	public void testGetClassName() {
		Map<String, String> tests = new HashMap<>();
		tests.put( "http://www.google.com/name/one", "name" );
		tests.put( "http://www.google.com/name#two", "www.google.com" );
		tests.put( "http://www.google.com/name/one/", "name" );
		tests.put( "http://www.google.com/name/one#", "name" );
		tests.put( "something strange", null );

		for ( Map.Entry<String, String> e : tests.entrySet() ) {
			String rslt = Utility.getClassName( e.getKey() );
			assertEquals( "failed to parse: " + e.getKey(), e.getValue(), rslt );
		}
	}

	//@Test
	public void testGetQualifiedClassName() {
	}

	@Test
	public void testCheckPatternInString() {
		Map<String, String> trues = new HashMap<>();
		trues.put( "http://www.google.com/name/one", "oog" );
		trues.put( "http://www.google.com/name#two; three", "three" );
		trues.put( "one, two, three; four; five; six ; seven eight nine", "seven e" );

		Map<String, String> falses = new HashMap<>();
		falses.put( "http://www.google.com/name/one", "oog1" );
		falses.put( "http://www.google.com/name#two; three", "?two" );
		falses.put( "one, two, three; four; five; six ; seven eight nine", "seven E" );

		for ( Map.Entry<String, String> e : trues.entrySet() ) {
			assertTrue( "error finding: " + e.getValue() + " in " + e.getKey(),
					Utility.checkPatternInString( e.getValue(), e.getKey() ) );
		}

		for ( Map.Entry<String, String> e : falses.entrySet() ) {
			assertFalse( "mysteriously found: " + e.getValue() + " in " + e.getKey(),
					Utility.checkPatternInString( e.getValue(), e.getKey() ) );
		}
	}

	//@Test
	public void testRunCheck() {
	}

	@Test
	public void testRound() {
		Map<Double, Double> ones = new HashMap<>();
		ones.put( 1.05d, 1.1d );
		ones.put( 1.04d, 1.0d );
		ones.put( 1.09d, 1.1d );
		ones.put( 1.04999d, 1.0d );
		ones.put( -1.04999d, -1.0d );

		for ( Map.Entry<Double, Double> e : ones.entrySet() ) {
			Double rslt = Utility.round( e.getKey(), 1 );
			assertEquals( "error rounding: " + e.getKey(), e.getValue(), rslt );
		}

		Map<Double, Double> twos = new HashMap<>();
		twos.put( 1.05d, 1.05d );
		twos.put( 1.049d, 1.05d );
		twos.put( 1.123d, 1.12d );
		twos.put( 1.00499d, 1.00d );
		twos.put( -1.04999d, -1.05d );
		twos.put( 13947.04999d, 13947.05d );

		for ( Map.Entry<Double, Double> e : twos.entrySet() ) {
			Double rslt = Utility.round( e.getKey(), 2 );
			assertEquals( "error rounding: " + e.getKey(), e.getValue(), rslt );
		}

		assertFalse( "1.045 check", Utility.round( 1.045d, 2 ) == 1.04d );
		assertTrue( "1.049 check", Utility.round( 1.049d, 0 ) == 1d );
	}

	//@Test
	public void testSciToDollar() {
	}

	//@Test
	public void testGetParamsFromString() {
	}

	@Test
	public void testlegalizeStringForSparql() {
		String string = "\"Ryan's \"'\"Problematic\"'\" Label\"";

		String fxstr = Utility.legalizeStringForSparql( string );
		String vfstr = new LiteralImpl( string ).stringValue();

		Repository repo = new SailRepository( new MemoryStore() );
		try {
			repo.initialize();
			RepositoryConnection rc = repo.getConnection();
			// add the string to a repository
			rc.add( new StatementImpl( Constants.ANYNODE, RDFS.LABEL, new LiteralImpl( string ) ) );
			// fetch the statements
			for ( Statement s : Iterations.asList( 
					rc.getStatements( Constants.ANYNODE, RDFS.LABEL, null, false ) ) ) {
				assertEquals( string, s.getObject().stringValue() );
			}
			rc.close();
			repo.shutDown();
		}
		catch ( RepositoryException e ) {
			// should never get here
			Logger.getLogger( getClass() ).error( e,e );
		}

		//assertEquals( vfstr, fxstr );
	}
}
