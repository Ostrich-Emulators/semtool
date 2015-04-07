/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import gov.va.semoss.util.Constants;

/**
 *
 * @author ryan
 */
public class PropFileWriterTest {

  private static File TMPDIR = null;
  private Map<String, File> files = new HashMap<>();
  private Map<String, File> files2 = new HashMap<>();

  public PropFileWriterTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    ConsoleAppender console = new ConsoleAppender(); //create appender
    //configure the appender
    String PATTERN = "%d [%p|%c|%C{1}] %m%n";
    console.setLayout( new PatternLayout( PATTERN ) );
    console.setThreshold( Level.ALL );
    console.activateOptions();
    //add appender to any Logger (here is root)
    Logger.getRootLogger().addAppender( console );

    try {
      File f = File.createTempFile( "semoss-tests-", "");
      f.delete();
      f.mkdirs();
      TMPDIR = f;
    }
    catch ( Exception e ) {
      Logger.getLogger( PropFileWriterTest.class ).fatal( e, e );
    }
  }

  @AfterClass
  public static void tearDownClass() {
    FileUtils.deleteQuietly( TMPDIR );
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
    for ( File f : files.values() ) {
      FileUtils.deleteQuietly( f );
    }

    for ( File f : files2.values() ) {
      FileUtils.deleteQuietly( f );
    }
  }

  @Test
  public void testSetDefaultQuestionSheet() {
  }

  @Test
  public void testSetBaseDir() {
    PropFileWriter pfw = new PropFileWriter( "db" );
    pfw.setBaseDir( "db2" );
    File newer = pfw.getBaseDir();

    File expected = new File( "db2" ).getAbsoluteFile();
    assertEquals( expected, newer );
  }

  @Test
  public void testSetHasMap() {
    PropFileWriter pfw = new PropFileWriter( "db" );
    pfw.setHasMap( true );
    assertTrue( pfw.getHasMap() );
  }

  @Test
  public void testSetDefaultEngine() {
    PropFileWriter pfw = new PropFileWriter( "db" );
    String expected = "expected";
    pfw.setEngineClassName( expected );
    assertEquals( expected, pfw.getEngineClassName() );
  }

  @Test
  public void testRunWriterNullFiles() throws IOException {
    PropFileWriter pfw = new PropFileWriter( TMPDIR.getAbsolutePath() );
    File basedir = new File( TMPDIR, "testEngine" );

    files = pfw.runWriter( "testEngine", null, null, null, basedir );

    assertEquals( "incorrect base dir", basedir, files.get(Constants.ENGINE_NAME ) );

    File owl = new File( basedir, "testEngine_OWL.OWL" ).getCanonicalFile();
    assertEquals( "incorrect owl file", owl,
        files.get(Constants.OWLFILE ).getCanonicalFile() );

    File q = new File( basedir, "testEngine_Questions.properties" ).getCanonicalFile();
    assertEquals( "incorrect questions file", q,
        files.get( Constants.DREAMER ).getCanonicalFile() );
    File dq = PropFileWriter.getDefaultFile( "Default_Questions.properties" );
    assertEquals( "questions contents",
        FileUtils.readFileToString( dq ), FileUtils.readFileToString( q ) );

//    File onto = new File( basedir, "testEngine_Custom_Map.prop" ).getCanonicalFile();
//    assertEquals( "incorrect ontology file", onto,
//        files.get( Constants.ONTOLOGY ).getCanonicalFile() );
//    File donto = new File( defaultsdir, "Default_Custom_Map.prop" );
//    assertEquals( "ontology contents",
//        FileUtils.readFileToString( donto ), FileUtils.readFileToString( onto ) );

    File smss = new File( basedir, "testEngine.temp" );
    assertEquals( "incorrect smss file", smss.getCanonicalFile(),
        files.get( Constants.PROPS ).getCanonicalFile() );
  }

  @Test
  public void testRunWriterAllNulls() throws IOException {
    PropFileWriter pfw = new PropFileWriter( TMPDIR.getAbsolutePath() );
    File dbdir = new File( pfw.getBaseDir().getAbsolutePath(), "db" );
    // File defaultsdir = new File( dbdir, "Default" );
    File basedir = new File( dbdir, "testEngine" );

    files = pfw.runWriter( "testEngine", null, null, null, basedir );

    assertEquals( "incorrect base dir", basedir, files.get(Constants.ENGINE_NAME ) );

    File owl = new File( basedir, "testEngine_OWL.OWL" ).getCanonicalFile();
    assertEquals( "incorrect owl file", owl,
        files.get(Constants.OWLFILE ).getCanonicalFile() );

    File q = new File( basedir, "testEngine_Questions.properties" ).getCanonicalFile();
    assertEquals( "incorrect questions file", q,
        files.get( Constants.DREAMER ).getCanonicalFile() );
    File dq = PropFileWriter.getDefaultFile( "Default_Questions.properties" );
    assertEquals( "questions contents",
        FileUtils.readFileToString( dq ), FileUtils.readFileToString( q ) );

//    File onto = new File( basedir, "testEngine_Custom_Map.prop" ).getCanonicalFile();
//    assertEquals( "incorrect ontology file", onto,
//        files.get( Constants.ONTOLOGY ).getCanonicalFile() );
//    File donto = new File( defaultsdir, "Default_Custom_Map.prop" );
//    assertEquals( "ontology contents",
//        FileUtils.readFileToString( donto ), FileUtils.readFileToString( onto ) );

    File smss = new File( basedir, "testEngine.temp" );
    assertEquals( "incorrect smss file", smss.getCanonicalFile(),
        files.get( Constants.PROPS ).getCanonicalFile() );
  }

  @Test
  public void testRunWriterEmpties() throws IOException {
    PropFileWriter pfw = new PropFileWriter( TMPDIR.getAbsolutePath() );
    File dbdir = new File( pfw.getBaseDir().getAbsolutePath(), "db" );
    File basedir = new File( dbdir, "testEngine" );
    files = pfw.runWriter( "testEngine", "", "", "", null );

    assertEquals( "incorrect base dir", basedir.getCanonicalFile(),
        files.get(Constants.ENGINE_NAME ).getCanonicalFile() );

    File owl = new File( basedir, "testEngine_OWL.OWL" ).getCanonicalFile();
    assertEquals( "incorrect owl file", owl,
        files.get(Constants.OWLFILE ).getCanonicalFile() );

    File q = new File( basedir, "testEngine_Questions.properties" ).getCanonicalFile();
    assertEquals( "incorrect questions file", q,
        files.get( Constants.DREAMER ).getCanonicalFile() );
    File dq = PropFileWriter.getDefaultFile( "Default_Questions.properties" );
    assertEquals( "questions contents",
        FileUtils.readFileToString( dq ), FileUtils.readFileToString( q ) );

//    File onto = new File( basedir, "testEngine_Custom_Map.prop" ).getCanonicalFile();
//    assertEquals( "incorrect ontology file", onto,
//        files.get( Constants.ONTOLOGY ).getCanonicalFile() );
//    File donto = new File( defaultsdir, "Default_Custom_Map.prop" );
//    assertEquals( "ontology contents",
//        FileUtils.readFileToString( donto ), FileUtils.readFileToString( onto ) );

    File smss = new File( basedir, "testEngine.temp" ).getCanonicalFile();
    assertEquals( "incorrect smss file", smss,
        files.get( Constants.PROPS ).getCanonicalFile() );
  }

  @Test
  public void testRunWriterCustom() throws IOException {
    // we'll write a default set, and then use the generated files
    // as custom files for another writing
		PropFileWriter pfw = new PropFileWriter( TMPDIR.getAbsolutePath() );
    File tmpbase = new File( TMPDIR, "testEngine" );
    files2 = pfw.runWriter( "testEngine", null, null, null, tmpbase );
		    
    File basedir = new File( TMPDIR, "testEngine2" );

    files = pfw.runWriter( "testEngine2", null,
        files2.get( Constants.PROPS ).toString(),
        files2.get( Constants.DREAMER ).toString(), basedir );

    assertEquals( "incorrect base dir", basedir.getCanonicalPath(),
        files.get(Constants.ENGINE_NAME ).getCanonicalPath() );

    assertEquals( "questions contents",
        FileUtils.readFileToString( files.get( Constants.DREAMER ) ),
        FileUtils.readFileToString( files2.get( Constants.DREAMER ) ) );
//    assertEquals( "onto contents",
//        FileUtils.readFileToString( files.get( Constants.ONTOLOGY ) ),
//        FileUtils.readFileToString( files2.get( Constants.ONTOLOGY ) ) );
  }
}
