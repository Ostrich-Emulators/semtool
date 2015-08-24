/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import gov.va.semoss.rdf.engine.util.EngineUtil;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.rdf.engine.util.EngineManagementException;
import gov.va.semoss.rdf.engine.util.EngineOperationListener;
import gov.va.semoss.security.LocalUserImpl;
import gov.va.semoss.ui.main.SemossPreferences;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class PinningEngineListener extends AbstractFileWatcher
		implements FileAlterationListener, EngineOperationListener {

	private static final Logger log = Logger.getLogger(PinningEngineListener.class );
	private final Set<String> pinWhenOpen = new HashSet<>();

	@Override
	public void onStart( FileAlterationObserver fao ) {
	}

	@Override
	public void onDirectoryCreate( File file ) {
	}

	@Override
	public void onDirectoryChange( File file ) {
	}

	@Override
	public void onDirectoryDelete( File file ) {
	}

	@Override
	public void onFileCreate( File file ) {
		// see if we should care, and add the db if we do
		if ( fileHasMyExtension( file ) ) {
			// we care...so load the db

			try {
				EngineUtil.getInstance().mount( file, true, true, LocalUserImpl.admin() );
			}
			catch ( EngineManagementException ioe ) {
				log.error( "could not load db from file: " + file, ioe );
			}
		}
	}

	@Override
	public void onFileChange( File file ) {
	}

	@Override
	public void onFileDelete( File file ) {
		// if we care, remove the engine from the list
		if ( fileHasMyExtension( file ) ) {
			log.debug( "i care about (delete): " + file.getAbsolutePath() );
		}
	}

	@Override
	public void onStop( FileAlterationObserver fao ) {
	}

	@Override
	public synchronized void loadFirst() {
		log.debug( "into loadFirst" );
		SemossPreferences prefs = SemossPreferences.getInstance();

		// PINNED_DBS is a ;-delimited list of smss files
		Collection<String> pinned = prefs.getPinnedSmsses();

		// open everything that has been pinned
		for ( String smss : pinned ) {
			log.debug( "found pinned SMSS: " + smss );
			File smssfile = new File( smss );
			if ( smssfile.exists() ) {
				onFileCreate( smssfile );
			}
			else {
				log.error( "could not find pinned smss file: " + smss );
				SemossPreferences.getInstance().removePin( smss );
			}
		}
	}

	@Override
	public void process( String fileName ) {
	}

	private boolean fileHasMyExtension( File file ) {
		String ext = "." + FilenameUtils.getExtension( file.getName() );
		return extensions.contains( ext );
	}

	@Override
	public void engineOpened( IEngine eng ) {
		// check to see if this database should be pinned
		SemossPreferences prefs = SemossPreferences.getInstance();
		String smss = eng.getProperty( Constants.SMSS_LOCATION );
		if ( pinWhenOpen.contains( smss ) ) {
			prefs.togglePin( eng );
			pinWhenOpen.remove( smss );
		}
		else {
			Set<String> pins = prefs.getPinnedSmsses();
			if ( pins.contains( smss ) ) {
				eng.setProperty( Constants.PIN_KEY, Boolean.toString( true ) );
			}
		}
	}

	@Override
	public void engineClosed( IEngine eng ) {
		// nothing to do here
	}

	@Override
	public void insightsModified( IEngine eng, Collection<URI> perspectives,
			Collection<URI> numinsights ) {
	}
}
