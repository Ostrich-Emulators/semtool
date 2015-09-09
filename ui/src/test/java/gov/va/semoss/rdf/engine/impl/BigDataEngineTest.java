/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.impl;

import gov.va.semoss.om.Perspective;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.api.InsightManager;
import gov.va.semoss.rdf.engine.api.WriteableInsightManager;
import gov.va.semoss.ui.components.DBToLoadingSheetExporterTest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author ryan
 */
public class BigDataEngineTest {

	private static final Logger log
			= Logger.getLogger( DBToLoadingSheetExporterTest.class );
	private File dbfile;
	private IEngine eng;

	public BigDataEngineTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	private void extractKb() {
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
	}

	@Before
	public void setUp() throws RepositoryException {
		extractKb();

		eng = new BigDataEngine( BigDataEngine.generateProperties( dbfile ));
	}

	@After
	public void tearDown() {
		eng.closeDB();
		FileUtils.deleteQuietly( dbfile );
	}

	@Test
	public void testGetWriteableInsightManager() {
		InsightManager im = eng.getInsightManager();
		WriteableInsightManager wim = eng.getWriteableInsightManager();
		Collection<Perspective> oldps = im.getPerspectives();
		Collection<Perspective> newps = wim.getPerspectives();
		assertEquals( "wim not the same as im", oldps.size(), newps.size() );

		String pname = "test perspective";
		Perspective p = new Perspective( pname );
		URI uri = wim.add( p );
		assertEquals( uri, p.getUri() );

		wim.commit();

		Collection<Perspective> imps = im.getPerspectives();
		Collection<Perspective> wimps = wim.getPerspectives();
		im.release();
		wim.release();

		assertEquals( "perspective not added", oldps.size() + 1, imps.size() );
		assertEquals( "commit failed", imps.size(), wimps.size() );
	}
}
