/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.components;

import com.ostrichemulators.semtool.rdf.engine.util.DBToLoadingSheetExporter;
import com.ostrichemulators.semtool.rdf.engine.util.NodeDerivationTools;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.rdf.query.util.ModificationExecutorAdapter;

import com.ostrichemulators.semtool.util.GuiUtility;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class DBToLoadingSheetExporterTest {

	private static final Logger log
			= Logger.getLogger( DBToLoadingSheetExporterTest.class );
	private File dbfile;
	private IEngine eng;

	public DBToLoadingSheetExporterTest() {
	}

	@Before
	public void setUp() {
		if ( null != dbfile ) {
			FileUtils.deleteQuietly( dbfile );
		}

		try {
			dbfile = File.createTempFile( "semoss-test-", ".jnl" );
			Files.copy( new File( "src/test/resources/test.jnl" ).toPath(),
					dbfile.toPath(), StandardCopyOption.REPLACE_EXISTING );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}

		try {
			eng = GuiUtility.loadEngine( dbfile );
		}
		catch ( IOException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@After
	public void tearDown() {
		GuiUtility.closeEngine( eng );
		FileUtils.deleteQuietly( dbfile );
	}

	@Test
	public void testSetEngine() {
		DBToLoadingSheetExporter dbtlse = new DBToLoadingSheetExporter( null );
		dbtlse.setEngine( eng );
		assertEquals( eng, dbtlse.getEngine() );
	}

	@Test
	public void testCreateConceptList() throws Exception {

		eng.execute( new ModificationExecutorAdapter() {

			@Override
			public void exec( RepositoryConnection conn ) throws RepositoryException {
				ValueFactory vf = conn.getValueFactory();
				List<Statement> owls = new ArrayList<>();
				URI concept = eng.getSchemaBuilder().getConceptUri().build();
				owls.add( new StatementImpl( vf.createURI( "http://semoss.org/ontologies/Concept/DataElement" ),
						RDFS.SUBCLASSOF, concept ) );
				owls.add( new StatementImpl( vf.createURI( "http://semoss.org/ontologies/Concept/InterfaceControlDocument" ),
						RDFS.SUBCLASSOF, concept ) );
				owls.add( new StatementImpl( vf.createURI( "http://semoss.org/ontologies/Concept/VCAMPApplicationModule" ),
						RDFS.SUBCLASSOF, concept ) );

				// add something else, just so we know we're not getting everything
				owls.add( new StatementImpl( vf.createURI( "http://semoss.org/ontologies/foo" ),
						RDFS.SUBCLASSOF, vf.createURI( "http://semoss.org/ontologies/bar" ) ) );

				conn.add( owls );
			}
		} );

		List<URI> concepts = NodeDerivationTools.createConceptList( eng );
		Collections.sort( concepts, new Comparator<URI>() {

			@Override
			public int compare( URI t, URI t1 ) {
				return t.toString().compareTo( t1.toString() );
			}
		} );

		assertEquals( "http://semoss.org/ontologies/Concept/DataElement",
				concepts.remove( 0 ).toString() );
		assertEquals( "http://semoss.org/ontologies/Concept/InterfaceControlDocument",
				concepts.remove( 0 ).toString() );
		assertEquals( "http://semoss.org/ontologies/Concept/VCAMPApplicationModule",
				concepts.remove( 0 ).toString() );

		assertTrue( concepts.isEmpty() );
	}

}
