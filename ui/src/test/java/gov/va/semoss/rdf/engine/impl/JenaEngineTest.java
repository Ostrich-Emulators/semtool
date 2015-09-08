/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.rdf.query.util.ModificationExecutorAdapter;
import gov.va.semoss.rdf.query.util.impl.OneVarListQueryAdapter;
import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class JenaEngineTest {
	
	private static final Logger log = Logger.getLogger( JenaEngineTest.class );
	private static final File TDB = new File( "src/test/resources/database.tdb.zip" );
	private File dbdir;
	private File randomdbdir;
	
	public JenaEngineTest() {
	}
	
	@Before
	public void setUp() throws IOException {
		randomdbdir = File.createTempFile( "semoss-test-tdb-", "" );
		randomdbdir.delete();
		Utility.unzip( new ZipInputStream( new FileInputStream( TDB ) ), randomdbdir );
		dbdir = new File( randomdbdir, "database.tdb" );
	}
	
	@After
	public void tearDown() {
		if ( !FileUtils.deleteQuietly( randomdbdir ) ) {
			log.error( "could not delete test directory: " + randomdbdir );
		}
	}
	
	@Test
	public void testStartLoading() throws Exception {
		Properties props = new Properties();
		props.setProperty( JenaEngine.FILE_PROP, dbdir.toString() );
		
		JenaEngine instance = new JenaEngine(props);
		instance.startLoading( props );
		
		OneVarListQueryAdapter<String> lqa
				= OneVarListQueryAdapter.getStringList( "SELECT ?label { ?s rdfs:label ?label }",
						"label" );
		Set<String> names = new HashSet<>( instance.query( lqa ) );
		Set<String> expected = new HashSet<>( Arrays.asList( "Yuri", "Yugo", "Pinto",
				"Yuri Purchased Yugo", "Human Being", "Car", "Price", "Date", "First Name",
				"Last Name", "Purchased" ) );
		
		assertEquals( expected, names );
		
		instance.closeDB();
	}
	
	@Test
	public void testCloseDB() throws RepositoryException {
		Properties props = new Properties();
		props.setProperty( JenaEngine.FILE_PROP, dbdir.toString() );
		props.setProperty( JenaEngine.INMEM_PROP, Boolean.toString( false ) );
		
		JenaEngine instance = new JenaEngine(props);
		instance.closeDB();
		assertTrue( !instance.getShadowFile().exists() );
	}
	
	@Test
	public void testModifyData() throws Exception {
		Properties props = new Properties();
		props.setProperty( JenaEngine.FILE_PROP, dbdir.toString() );
		
		JenaEngine instance = new JenaEngine(props);
		
		instance.execute( new ModificationExecutorAdapter( true ) {
			
			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				conn.add( new URIImpl( "http://foo.bar/testuri" ), RDFS.LABEL,
						new LiteralImpl( "extra" ) );
			}
			
		} );
		
		instance.closeDB();
		
		instance.openDB( props );
		
		OneVarListQueryAdapter<String> lqa
				= OneVarListQueryAdapter.getStringList( "SELECT ?label { ?s rdfs:label ?label }",
						"label" );
		Set<String> names = new HashSet<>( instance.query( lqa ) );
		Set<String> expected = new HashSet<>( Arrays.asList( "Yuri", "Yugo", "Pinto",
				"Yuri Purchased Yugo", "Human Being", "Car", "Price", "Date", "First Name",
				"Last Name", "Purchased", "extra" ) );
		instance.closeDB();
		assertEquals( expected, names );
	}
	
	@Test
	public void testGenerateProperties() {
		Properties props = JenaEngine.generateProperties( dbdir );
		assertEquals( dbdir.toString(), props.getProperty( JenaEngine.FILE_PROP ) );
		assertTrue( Boolean.parseBoolean( props.getProperty( JenaEngine.INMEM_PROP ) ) );
	}
}
