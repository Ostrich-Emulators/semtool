/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.hp.hpl.jena.query.Dataset;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;
import com.ostrichemulators.semtool.rdf.query.util.impl.OneVarListQueryAdapter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class JenaEngineTest {

	private static final Logger log = Logger.getLogger( JenaEngineTest.class );
	private static final File loadfile = new File( "src/test/resources/test12.nt" );
	private Dataset tdb;
	private JenaEngine instance;

	public JenaEngineTest() {
	}

	@Before
	public void setUp() throws IOException, RepositoryException {
		tdb = RDFDataMgr.loadDataset( loadfile.toString() );
		instance = new JenaEngine( tdb );
	}

	@After
	public void tearDown() {
		instance.closeDB();
		tdb.close();
	}

	@Test
	public void testStartLoading() throws Exception {
		OneVarListQueryAdapter<String> lqa
				= OneVarListQueryAdapter.getStringList( "SELECT ?label { ?s rdfs:label ?label }" );
		Set<String> names = new HashSet<>( instance.query( lqa ) );
		Set<String> expected = new HashSet<>( Arrays.asList( "Reification", "RDR Reification",
				"Alan", "First Name", "Purchased", "Reification Model", "Date", "Yugo",
				"Cadillac", "Price", "Car", "Database", "Yuri", "OS-EM Semantic Toolkit Reification",
				"Has", "Last Name", "W3C Reification", "Human Being", "Data View" ) );
		assertEquals( expected, names );
	}

	@Test
	public void testModifyData() throws Exception {
		instance.execute( new ModificationExecutorAdapter( true ) {

			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				conn.add( conn.getValueFactory().createIRI("http://foo.bar/testuri" ), RDFS.LABEL,
						conn.getValueFactory().createLiteral( "extra" ) );
			}

		} );

		OneVarListQueryAdapter<String> lqa
				= OneVarListQueryAdapter.getStringList( "SELECT ?label { ?s rdfs:label ?label }" );
		Set<String> names = new HashSet<>( instance.query( lqa ) );
		Set<String> expected = new HashSet<>( Arrays.asList( "Reification", "RDR Reification",
				"Alan", "First Name", "Purchased", "Reification Model", "Date", "Yugo",
				"Cadillac", "Price", "Car", "Database", "Yuri", "OS-EM Semantic Toolkit Reification",
				"Has", "Last Name", "W3C Reification", "Human Being", "Data View", "extra" ) );
		instance.closeDB();
		assertEquals( expected, names );
	}

	@Test
	public void testGenerateProperties() {
		File dbdir = new File( "tester.tdb" );
		Properties props = JenaEngine.generateProperties( dbdir );
		assertEquals( dbdir.toString(), props.getProperty( JenaEngine.FILE_PROP ) );
		assertTrue( Boolean.parseBoolean( props.getProperty( JenaEngine.INMEM_PROP ) ) );
	}
}
