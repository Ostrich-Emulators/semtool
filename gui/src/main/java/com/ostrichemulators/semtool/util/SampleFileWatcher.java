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
package com.ostrichemulators.semtool.util;

import org.apache.log4j.Logger;

public class SampleFileWatcher extends AbstractFileWatcher {
  private static final Logger log = Logger.getLogger( SampleFileWatcher.class );
  
	/**
	 * Processes sample files.
	 * @param 	fileName
	 */
	@Override
	public void process(String fileName) {
		try {
			//loadExistingDB();
			// for the sample this will never get called
		} catch(Exception ex) {
			log.error( ex );
		}
	}
	
	/**
	 * Runs the file watcher.
	 */
	@Override
	public void run()
	{
		log.debug("Engine Name is " + engine);
	}

	/**
	 * Used in the starter class for loading files.
	 */
	@Override
	public void loadFirst() {
		
	}
	
}
