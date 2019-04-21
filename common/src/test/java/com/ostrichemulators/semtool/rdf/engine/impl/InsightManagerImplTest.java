/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.rdf.engine.impl;

import com.ostrichemulators.semtool.model.vocabulary.OLO;
import com.ostrichemulators.semtool.model.vocabulary.SEMCORE;
import com.ostrichemulators.semtool.model.vocabulary.SEMONTO;
import com.ostrichemulators.semtool.model.vocabulary.SEMPERS;
import com.ostrichemulators.semtool.model.vocabulary.SP;
import com.ostrichemulators.semtool.model.vocabulary.SPIN;
import com.ostrichemulators.semtool.model.vocabulary.UI;
import com.ostrichemulators.semtool.om.Perspective;
import com.ostrichemulators.semtool.rdf.engine.api.InsightManager;
import com.ostrichemulators.semtool.rdf.engine.util.EngineManagementException;
import com.ostrichemulators.semtool.rdf.engine.util.EngineUtil2;
import com.ostrichemulators.semtool.user.LocalUserImpl;
import com.ostrichemulators.semtool.util.Constants;
import com.ostrichemulators.semtool.util.UriBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class InsightManagerImplTest {

	private static final Logger log = Logger.getLogger( InsightManagerImplTest.class );
	private static final File SRCFILE = new File( "src/test/resources/insmgr.data-source.ttl" );

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

	@Test
	public void testCreateFromRepository() throws Exception {
		Repository repo = new SailRepository( new MemoryStore() );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		rc.add( SRCFILE, null, RDFFormat.TURTLE );
		rc.commit();
		rc.close();

		InsightManager imi = InsightManagerImpl.createFromRepository( repo );
		repo.shutDown();

		assertEquals( 1, imi.getPerspectives().size() );
	}

	@Test
	public void testCreateStatements() throws Exception {
		InsightManagerImpl imi = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( SRCFILE, imi );

		Collection<Statement> stmts
				= InsightManagerImpl.getModel( imi, new LocalUserImpl() );

		if ( log.isTraceEnabled() ) {
			File tmpdir = FileUtils.getTempDirectory();
			try ( Writer w = new BufferedWriter( new FileWriter( new File( tmpdir,
					SRCFILE.getName() ) ) ) ) {
				TurtleWriter tw = new TurtleWriter( w );
				tw.startRDF();

				tw.handleNamespace( SEMPERS.PREFIX, SEMPERS.NAMESPACE );
				tw.handleNamespace( SEMONTO.PREFIX, SEMONTO.NAMESPACE );
				tw.handleNamespace( SEMCORE.PREFIX, SEMCORE.NAMESPACE );
				tw.handleNamespace( SPIN.PREFIX, SPIN.NAMESPACE );
				tw.handleNamespace( SP.PREFIX, SP.NAMESPACE );
				tw.handleNamespace( UI.PREFIX, UI.NAMESPACE );
				tw.handleNamespace( RDFS.PREFIX, RDFS.NAMESPACE );
				tw.handleNamespace( RDF.PREFIX, RDF.NAMESPACE );
				tw.handleNamespace( OWL.PREFIX, OWL.NAMESPACE );
				tw.handleNamespace( OLO.PREFIX, OLO.NAMESPACE );
				tw.handleNamespace( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
				tw.handleNamespace( XMLSchema.PREFIX, XMLSchema.NAMESPACE );

				for ( Statement s : stmts ) {
					tw.handleStatement( s );
				}
				tw.endRDF();
			}
		}

		assertEquals(48, stmts.size() );
	}

	@Test
	public void testSystemP() {
		InsightManagerImpl imi = new InsightManagerImpl();
		InMemorySesameEngine eng = InMemorySesameEngine.open();
		eng.setBuilders( UriBuilder.getBuilder( Constants.ANYNODE + "/data/" ),
				UriBuilder.getBuilder( Constants.ANYNODE + "/schema/" ) );
		Perspective p = imi.getSystemPerspective( eng );
		assertEquals( 3, p.getInsights().size() );
	}

	@Test
	public void testCtor() throws IOException, EngineManagementException {
		InsightManagerImpl imi = new InsightManagerImpl();
		EngineUtil2.createInsightStatements( SRCFILE, imi );

		InsightManager im = new InsightManagerImpl( imi );
		assertEquals( imi.getPerspectives(), im.getPerspectives() );

	}
}
