/**
 * *****************************************************************************
 * Copyright 2013 SEMOSS.ORG
 *
 * This file is part of SEMOSS.
 *
 * SEMOSS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SEMOSS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SEMOSS. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************
 */
package com.ostrichemulators.semtool.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ostrichemulators.semtool.rdf.engine.api.IEngine;

/**
 * Interface that provides a common protocol for objects that wish to execute
 * code while they are active. Used to filter filenames. Opens up a thread and
 * watches the file.
 */
public abstract class AbstractFileWatcher implements Runnable, FilenameFilter {

	// opens up a thread and watches the file
  // when available, it will upload it into the journal
  // may be this is a good time to put this on tomcat
  private static final Logger log = Logger.getLogger( AbstractFileWatcher.class );

	// processes the files with the given extension
  protected String folderToWatch = null;
  protected Set<String> extensions = new HashSet<>();
  protected IEngine engine = null;
  protected Object monitor = null;

  /**
   * Sets folder to watch.
   *
   * @param folderToWatch String	Folder to watch.
   */
  public void setFolderToWatch( String folderToWatch ) {
    this.folderToWatch = folderToWatch;
  }

  /**
   * Sets extension of files.
   *
   * @param extension String	Extension of files.
   */
  public void addExtension( String extension ) {
    this.extensions.add( extension );
  }

  public void setExtensions( Collection<String> exts ) {
    extensions.clear();
    for ( String s : exts ) {
      extensions.add( s.trim() );
    }
  }

  /**
   * Sets engine.
   *
   * @param engine IEngine	Engine to be set.
   */
  public void setEngine( IEngine engine ) {
    this.engine = engine;
  }

  /**
   * Sets monitor.
   *
   * @param monitor Object	Object to be monitored.
   */
  public void setMonitor( Object monitor ) {
    this.monitor = monitor;
  }

  /**
   * Used in the starter class for loading files.
   */
  public abstract void loadFirst();

  /**
   * Starts the thread and processes new files from a given directory.
   */
  @Override
  public void run() {
    try {
      WatchService watcher = FileSystems.getDefault().newWatchService();

      //Path dir2Watch = Paths.get(baseFolder + "/" + folderToWatch);
      Path dir2Watch = Paths.get( folderToWatch );

      dir2Watch.register( watcher, StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY );
      while ( true ) {
        //WatchKey key2 = watcher.poll(1, TimeUnit.MINUTES);
        WatchKey key2 = watcher.take();

        for ( WatchEvent<?> event : key2.pollEvents() ) {
          WatchEvent.Kind kind = event.kind();
          if ( kind == StandardWatchEventKinds.ENTRY_CREATE ) {
            String newFile = event.context() + "";
            if ( this.accept( null, newFile ) ) {
              Thread.sleep( 2000 );
              try {
                process( newFile );
              }
              catch ( Exception ex ) {
                log.error( ex );
              }
            }
            else {
              log.info( "Ignoring File " + newFile );
            }
          }
          key2.reset();
        }
      }
    }
    catch ( IOException | InterruptedException ex ) {
      // do nothing - I will be working it in the process block
    }
  }

  /**
   * Tests if a specified file should be included in a file list.
   *
   * @param arg0 File	Folder in which the file was found.
   * @param arg1 String	Name of the file.
   *
   * @return True if the name should be included in the file list.
   */
  @Override
  public boolean accept( File arg0, String arg1 ) {
    for ( String ext : extensions ) {
      if ( arg1.endsWith( ext ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Processes the file.
   *
   * @param fileName String	Name of the file.
   */
  public abstract void process( String fileName );

}
