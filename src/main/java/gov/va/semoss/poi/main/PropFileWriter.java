/*******************************************************************************
 * Copyright 2013 SEMOSS.ORG
 * 
 * This file is part of SEMOSS.
 * 
 * SEMOSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SEMOSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SEMOSS.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package gov.va.semoss.poi.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.impl.AbstractEngine;

import gov.va.semoss.util.Constants;

/**
 * Creates a folder in user.dir/db that contains the files required for the engine
 * The custom map, smss, and question sheet are all named based on the name of the engine
 * This class required user.dir/db/Default folder to contain a custom map, smss and question sheet
 */
public class PropFileWriter {

	private static final Logger logger = Logger.getLogger(PropFileWriter.class);

  private String engineClass = "gov.va.semoss.rdf.engine.impl.BigDataEngine";
  private boolean hasMap = false;
  private File baseDirectory;
  private final String defaultDBPropName;
  private final String defaultOntologyProp;
  private String defaultQuestionProp;

	public PropFileWriter ( String basedir ){
		defaultDBPropName = "Default.properties";
		defaultQuestionProp = "Default_Questions.properties";
		defaultOntologyProp = "Default_Custom_Map.prop";
    setBaseDir( basedir );
	}
	
	public void setDefaultQuestionSheet(String defaultQuestionSheet){
		this.defaultQuestionProp = defaultQuestionSheet;
	}
	
	public final void setBaseDir(String baseDir){
		setBaseDir( new File( baseDir ) );
	}
  
	public final void setBaseDir(File baseDir){
		baseDirectory = baseDir.getAbsoluteFile();
	}

  public File getBaseDir(){
    return baseDirectory;
  }
  
  public void setHasMap( boolean b ){
    hasMap = b;
  }
  
  public boolean getHasMap(){
    return hasMap;
  }
  
  public void setEngineClassName( String e ){
    engineClass = e;
  }
  
  public String getEngineClassName(){
    return engineClass;
  }
	
  //TODO Change variable names, should we change default.properties to default.smss?	
  /**
   * Uses the name of a new database to create the custom map, smss, and
   * question sheet files for the engine. If user does not specify specific
   * files, the default files in db/Default will be used.
   *
   *
   * @param engineName      String that contains the name of the engine
   * @param ontologyName    String that contains the path to a user specified
   *                        custom map file
   * @param dbPropFile      String that contains the path to a user specified
   *                        smss file
   * @param questionFileLoc String that contains the path to a user specified
   *                        question sheet
   * @param newEngineDir    the directory where all the db files should be
   *                        placed. If null, the new directory will be in
   *                        {@link #baseDirectory}/db
   *
   * @return a mapping of file locations. The keys in the map will be in the set
   *         {{@link Constants#OWLFILE}, {@link Constants#ENGINE_NAME} (the engine
   *         directory), {@link Constants#ONTOLOGY},
   * {@link Constants#DREAMER}, {@link Constants#PROPS} (the smss file)}
   */
   public Map<String, File> runWriter(String engineName, String ontologyName, 
        String dbPropFile, String questionFileLoc, File newEngineDir) {

        Map<String, File> files = new HashMap<>();
      
        File dbdir = new File(baseDirectory, "db");
        File engineDirectory
            = ( null == newEngineDir ? new File( dbdir, engineName ) : newEngineDir );
        files.put(Constants.ENGINE_NAME, engineDirectory );

        if (null == questionFileLoc) {
            questionFileLoc = "";
        }
        if (null == ontologyName) {
            ontologyName = "";
        }
        if (null == dbPropFile) {
            dbPropFile = "";
        }
        
        try {
            // make the new folder to store everything in
            engineDirectory.mkdirs();
            // define the owlFile location
            File owlFile = new File( engineDirectory,
                    AbstractEngine.getDefaultName(Constants.OWLFILE, engineName) );
            files.put(Constants.OWLFILE, owlFile );
            
            // if question sheet was not specified, we need to make a copy of the default questions
            File questionmodel = ("".equals(questionFileLoc)
                    ? getDefaultFile(defaultQuestionProp)
                    : new File(questionFileLoc));
            File questionFile = new File(engineDirectory,
                    AbstractEngine.getDefaultName(Constants.DREAMER, engineName));
            FileUtils.copyFile(questionmodel, questionFile );
            files.put( Constants.DREAMER, questionFile );

//            File ontomodel = ("".equals(ontologyName)
//                    ? getDefaultFile( defaultOntologyProp )
//                    : new File(ontologyName));
//            File ontologyFile = new File(engineDirectory,
//                    AbstractEngine.getDefaultName(Constants.ONTOLOGY, engineName));
//            FileUtils.copyFile(ontomodel, ontologyFile );
//            files.put( Constants.ONTOLOGY, ontologyFile );

            File smssmodel = ("".equals(dbPropFile)
                    ? getDefaultFile( defaultDBPropName )
                    : new File(dbPropFile));
            // NOTE: we're NOT setting the suffix to ".smss"
            // because we don't want the file watcher to open it before we're ready
            File propFile = new File(engineDirectory, engineName + ".temp");
            writeCustomDBProp( engineDirectory, smssmodel, propFile, engineName,
                this.engineClass );
            files.put( Constants.PROPS, propFile );

        } catch (Exception e) {
            logger.error(e, e);
        }
        
        return files;
    }
   
   public static File getDefaultFile( String name ) throws IOException {
     File outfile = File.createTempFile( "semoss-propwriter-", ".os" );
     FileUtils.copyInputStreamToFile( 
         PropFileWriter.class.getResourceAsStream( "/defaultdb/" + name ), 
         outfile );
     outfile.deleteOnExit();
     return outfile;
   }

    /**
     * Creates the contents of the SMSS file in a temp file for the engine Adds
     * file locations of the database, custom map, questions, and OWl files for
     * the engine Adds the file locations to the contents of the default SMSS
     * file which contains constant information about the database
     *
     * @param defaultName String containing the path to the Default SMSS file
     * @param dbname String containing the name of the new database
     */
    private void writeCustomDBProp(File engineDirectory, File smssmodel, 
        File smssfile, String dbname, String engineclass) throws IOException {
        // File jnlfile = new File(smssfile.getParent(), dbname + ".jnl" );
        
        Properties smssprops = new Properties();
        Properties modelprops = new Properties();
        try (FileReader rdr = new FileReader(smssmodel)) {
            modelprops.load(rdr);
        }
        
        Set<String> toremove = new HashSet<>(Arrays.asList(Constants.ONTOLOGY,
                Constants.OWLFILE, Constants.DREAMER, Constants.ENGINE_IMPL));
        
        for (String key : modelprops.stringPropertyNames()) {
            if (!toremove.contains(key)) {
                String val = modelprops.getProperty(key);
                // skip the filename part (rely on the convention)
                if (!val.contains("@FileName@")) {
                  smssprops.setProperty(key, val);
                }
            }
        }
        
        smssprops.setProperty(Constants.ENGINE_IMPL, engineclass );
        
        if (this.hasMap) {
            smssprops.setProperty("MAP", "db" + File.separator + dbname
                    + File.separator + dbname + "_Mapping.ttl" );
        }
        
        try (FileWriter fw = new FileWriter(smssfile)) {
            smssprops.store(fw, dbname);
        }
    }
}
