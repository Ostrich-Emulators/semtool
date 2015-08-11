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
package gov.va.semoss.util;

import java.io.File;
import javax.swing.JFrame;
import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.ui.components.RepositoryList;

/**
 * This class opens a thread and watches a specific SMSS file.
 */
public class SMSSFileWatcher extends AbstractFileWatcher {
  private static final Logger log = Logger.getLogger( SMSSFileWatcher.class );
  
	/**
	 * Processes SMSS files.
	 * @param	fileName of the file.
	 */
	@Override
	public void process(String fileName) {
		try {
			//loadExistingDB();
			loadNewDB(fileName);							
		} catch(Exception ex) {
			// TODO: Specify exception
			log.error( ex );
		}
	}
	
	/**
	 * Returns an array of strings naming the files in the directory.
	 * Goes through list and loads an existing database.
   * @throws java.lang.Exception
	 */
	public void loadExistingDB() throws Exception {
		File dir = new File(folderToWatch);
		String [] fileNames = dir.list(this);
    for ( String fileName : fileNames ) {
      try {
        loadNewDB( fileName );
        //Utility.loadEngine(fileName, prop);
      }
      catch ( Exception ex ) {
        log.error( ex );
        log.fatal( "Engine Failed " + folderToWatch+ "/" + fileName );
      }
    }	

	}
	
	/**
	 * Loads a new database by setting a specific engine with associated properties.
	 * @param 	newFile
   * @throws java.lang.Exception
	 */
	public void loadNewDB(String newFile) throws Exception {
    File smssfile = new File( folderToWatch, newFile );
  
    IEngine eng = GuiUtility.loadEngine(smssfile);
		RepositoryList list = DIHelper.getInstance().getRepoList();
		list.getRepositoryModel().add( eng );
		list.setSelectedIndex( 0 );

		JFrame frame2
				= (JFrame) DIHelper.getInstance().getLocalProp( Constants.MAIN_FRAME );
		frame2.repaint();
	}
	
	
	/**
	 * Used in the starter class for processing SMSS files.
	 */
	@Override
	public void loadFirst() {
    File dir = new File( folderToWatch );
    String[] fileNames = dir.list( this );
    for ( String fname : fileNames ) {
      try {
        process( fname );
      }
      catch ( Exception ex ) {
        log.fatal( "Engine Failed " + folderToWatch + "/" + fname, ex );
      }
    }
	}

	
	/**
	 * Processes new SMSS files.
	 */
	@Override
	public void run()
	{
		log.info("Starting thread");
		synchronized(monitor) {
			super.run();
		}
	}

}
