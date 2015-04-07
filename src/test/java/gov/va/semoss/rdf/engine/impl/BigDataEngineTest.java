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
import gov.va.semoss.util.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class BigDataEngineTest {

	private static final Logger log
			= Logger.getLogger( DBToLoadingSheetExporterTest.class );
	private File destination;
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
		if ( null != destination ) {
			FileUtils.deleteQuietly( destination );
		}
		destination = null;

		try ( ZipInputStream zis = new ZipInputStream( new FileInputStream(
				"src/test/resources/testdb.zip" ) ) ) {
			destination = File.createTempFile( "semoss-test-", "" );
			destination.delete();
			destination.mkdir();

			ZipEntry entry;
			while ( null != ( entry = zis.getNextEntry() ) ) {
				File outfile = new File( destination, entry.getName() );
				if ( entry.isDirectory() ) {
					outfile.mkdirs();
				}
				else {
					try ( FileOutputStream fout = new FileOutputStream( outfile ) ) {
						byte bytes[] = new byte[1024 * 1024];
						int read = -1;
						while ( -1 != ( read = zis.read( bytes ) ) ) {
							fout.write( bytes, 0, read );
						}
						zis.closeEntry();
					}
				}
			}

		}
		catch ( Exception e ) {
			log.error( e, e );
		}
	}

	@Before
	public void setUp() {
		extractKb();

		File dbdir = new File( destination, "testdb" );
		File smss = new File( dbdir, "testdb.smss" );
		try {
			eng = Utility.loadEngine( smss );
		}
		catch ( IOException ioe ) {
			log.error( ioe, ioe );
		}
	}

	@After
	public void tearDown() {
		Utility.closeEngine( eng );
		FileUtils.deleteQuietly( destination );
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

		assertEquals( "perspective not added", oldps.size()+1,
				im.getPerspectives().size() );
		
		assertEquals( "commit failed", im.getPerspectives().size(), 
				wim.getPerspectives().size() );
		wim.release();
	}
}
