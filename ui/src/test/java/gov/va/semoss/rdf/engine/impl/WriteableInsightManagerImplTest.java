/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.om.Insight;
import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.ui.components.DBToLoadingSheetExporterTest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class WriteableInsightManagerImplTest {

	private static final Logger log
			= Logger.getLogger( DBToLoadingSheetExporterTest.class );
	private static final File JNL = new File( "src/test/resources/insmgr.jnl" );
	private static final URI PERSP
			= new URIImpl( "http://va.gov/dataset/insights#perspective-1441046366490" );
	private static final URI INS
			= new URIImpl( "http://va.gov/dataset/insights#Insight-1441046475366" );
	private File dbfile;
	private IEngine eng;
	private WriteableInsightManager wim;

	private void extractKb() {
		if ( null != dbfile ) {
			FileUtils.deleteQuietly( dbfile );
		}

		try {
			dbfile = File.createTempFile( "semoss-test-", ".jnl" );
			Files.copy( JNL.toPath(), dbfile.toPath(), StandardCopyOption.REPLACE_EXISTING );
		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Before
	public void setUp() throws RepositoryException {
		extractKb();

		eng = new BigDataEngine(BigDataEngine.generateProperties( dbfile ));
		wim = eng.getWriteableInsightManager();
	}

	@After
	public void tearDown() {
		wim.release();
		eng.closeDB();
		FileUtils.deleteQuietly( dbfile );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testDispose1() {
		Insight ins = new Insight();
		ins.setLabel( "Get All Carriages" );
		ins.setSparql( "SELECT ?id WHERE { ?id a <http://foo.bar/model#Horseless_Carriage> }" );
		URI id = wim.add( ins );

		wim.dispose();
		Insight ins2 = wim.getInsight( id );
		assertEquals( id, ins2.getId() ); // we should never get here
	}

	@Test
	public void testDispose2() {
		Perspective p = wim.getPerspective( PERSP );
		String olddescription = p.getDescription();
		p.setLabel( "a regrettable change");
		wim.update( p );
		wim.dispose();

		p = wim.getPerspective( PERSP );
		assertEquals( olddescription, p.getDescription() );
	}

	//@Test
	public void testAdd_Insight() {
		Insight ins = new Insight();
		ins.setLabel( "Get All Carriages" );
		ins.setSparql( "SELECT ?id WHERE { ?id a <http://foo.bar/model#Horseless_Carriage> }" );
		URI insightid = wim.add( ins );
		assertEquals( insightid, ins.getId() );
		assertTrue( wim.hasCommittableChanges() );

		wim.commit();

		Insight ins2 = wim.getInsight( insightid );
		assertEquals( insightid, ins2.getId() );
	}

	//@Test
	public void testAdd_Insight2() {
		Insight ins = new Insight();
		String desc = "\"Ryan's \"'\"Problematic\"'\" Label\"";
		ins.setLabel( "Lotsa quotables" );
		ins.setDescription( desc );
		ins.setSparql( "SELECT ?id WHERE { ?id a <http://foo.bar/model#Horseless_Carriage> }" );
		URI insightid = wim.add( ins );
		assertEquals( insightid, ins.getId() );
		assertTrue( wim.hasCommittableChanges() );

		wim.commit();

		Insight ins2 = wim.getInsight( insightid );
		assertEquals( insightid, ins2.getId() );
		assertEquals( desc, ins2.getDescription() );
	}

	//@Test( expected = IllegalArgumentException.class )
	public void testRemove_Insight() {
		Insight ins = wim.getInsight( INS );
		wim.remove( ins );
		wim.commit();

		Insight ins2 = wim.getInsight( ins.getId() );
		assertNull( ins2 );
	}

	//@Test
	public void testUpdate_Insight() {
		Insight ins = wim.getInsight( INS );
		ins.setDescription( "This is a new description" );
		wim.update( ins );
		wim.commit();

		Insight ins2 = wim.getInsight( ins.getId() );
		assertEquals( "This is a new description", ins2.getDescription() );
	}

	//@Test
	public void testAdd_Perspective() {
		Perspective persp = new Perspective();
		String label = "A Wiiiild and Craaazzy Label";
		persp.setDescription( "Blah" );
		persp.setLabel( label );
		URI id = wim.add( persp );
		wim.commit();

		Perspective newPer = wim.getPerspective( id );
		assertEquals( id, newPer.getUri() );
		assertEquals( label, newPer.getLabel() );
		assertEquals( "Blah", newPer.getDescription() );
	}

	//@Test( expected = IllegalArgumentException.class )
	public void testRemove_Perspective() {
		Perspective p = wim.getPerspective( PERSP );
		wim.remove( p );
		wim.commit();

		Perspective p2 = wim.getPerspective( PERSP );
		assertNull( p2 );
	}

	//@Test
	public void testUpdate_Perspective() {
		Perspective persp = wim.getPerspective( PERSP );
		String label = "A Wiiiild and Craaazzy Label";
		persp.setDescription( "Blah" );
		persp.setLabel( label );
		URI id = wim.add( persp );
		wim.commit();

		Perspective newPer = wim.getPerspective( id );
		assertEquals( id, newPer.getUri() );
		assertEquals( label, newPer.getLabel() );
		assertEquals( "Blah", newPer.getDescription() );
	}

	//@Test
	public void testSetInsights() {
		Insight ins = new Insight();
		ins.setLabel( "Get All Carriages" );
		ins.setSparql( "SELECT ?id WHERE { ?id a <http://foo.bar/model#Horseless_Carriage> }" );
		wim.add( ins );

		Perspective persp = wim.getPerspective( PERSP );
		List<Insight> insights = wim.getInsights( persp );
		insights.add( ins );

		wim.setInsights( persp, insights );
		wim.commit();
		assertEquals( 2, wim.getInsights( persp ) );
	}
}
