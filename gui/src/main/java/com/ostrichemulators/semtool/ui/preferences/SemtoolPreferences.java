/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.preferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;
import com.ostrichemulators.semtool.rdf.engine.api.IEngine;
import com.ostrichemulators.semtool.util.Constants;

/**
 * A helper class for working with preferences. It has some special
 * encoding/decoding for some preferences
 *
 * @author ryan
 */
public class SemtoolPreferences {

	private static final Logger log = Logger.getLogger(SemtoolPreferences.class );

	private SemtoolPreferences() {

	}

	public static Preferences get() {
		return Preferences.userNodeForPackage(SemtoolPreferences.class );
	}

	public static Set<String> getPinnedLocations() {
		String smsses = get().get( Constants.PIN_KEY, "" );
		Set<String> pins = new HashSet<>();
		if ( !smsses.isEmpty() ) {
			pins.addAll( Arrays.asList( smsses.split( ";" ) ) );
		}

		return pins;
	}

	/**
	 * Removes an SMSS file location that no longer exists, or can't be opened
	 *
	 * @param smssloc the location to forget
	 */
	public static void removePin( String smssloc ) {
		Set<String> names = getPinnedLocations();
		names.remove( smssloc );
		writePins( names );
	}

	/**
	 * Toggles the pinning of the given repository
	 *
	 * @param eng the repository to toggle
	 */
	public static void togglePin( IEngine eng ) {
		Set<String> names = getPinnedLocations();
		String smssloc = eng.getProperty( Constants.SMSS_LOCATION );
		if ( Boolean.parseBoolean( eng.getProperty( Constants.PIN_KEY ) ) ) {
			eng.setProperty( Constants.PIN_KEY, null );
			names.remove( smssloc );
		}
		else {
			eng.setProperty( Constants.PIN_KEY, Boolean.TRUE.toString() );
			names.add( smssloc );
		}

		writePins( names );
	}

	private static void writePins( Set<String> pins ) {
		StringBuilder sb = new StringBuilder();
		for ( String smss : pins ) {
			if ( 0 != sb.length() ) {
				sb.append( ";" );
			}
			sb.append( smss );
		}
		get().put( Constants.PIN_KEY, sb.toString() );
	}
}
