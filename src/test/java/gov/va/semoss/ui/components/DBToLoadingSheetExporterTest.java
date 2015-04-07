/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Utility;

/**
 *
 * @author ryan
 */
public class DBToLoadingSheetExporterTest {

  private static final Logger log
      = Logger.getLogger( DBToLoadingSheetExporterTest.class );
  private File destination;
  private IEngine eng;

  public DBToLoadingSheetExporterTest() {
  }

  private void extractKb() {
    if ( null != destination ) {
      FileUtils.deleteQuietly( destination );
    }
    destination = null;

    try ( ZipInputStream zis = new ZipInputStream( new FileInputStream( "src/test/resources/testdb.zip" ) ) ) {
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
  public void testSetEngine() {
    DBToLoadingSheetExporter dbtlse = new DBToLoadingSheetExporter( null );
    dbtlse.setEngine( eng );
    assertEquals( eng, dbtlse.getEngine() );
  }

  @Test
  public void testCreateConceptList() throws Exception {
    DBToLoadingSheetExporter dbtlse = new DBToLoadingSheetExporter( eng );
    List<URI> concepts = dbtlse.createConceptList();
    Collections.sort( concepts, new Comparator<URI>(){

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
    
    assertTrue( concepts.isEmpty( ) );
  }

}
