/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.ui.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;
import gov.va.semoss.rdf.engine.api.IEngine;
import gov.va.semoss.util.Constants;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper class for working with preferences. The goal is to provide a FULLY
 * backward-compatible interface (so you don't have to use this class to get/set
 * preferences; you can use a plain old {@link Preferences} if you want), with
 * some special encoding/decoding for some preferences
 *
 * @author ryan
 */
public class SemossPreferences {

	private static final Logger log = Logger.getLogger( SemossPreferences.class );
	private final Preferences prefs
			= Preferences.userNodeForPackage( SemossPreferences.class );
	private static SemossPreferences instance;

	public static SemossPreferences getInstance() {
		if ( null == instance ) {
			instance = new SemossPreferences();
		}
		return instance;
	}

	private SemossPreferences() {
	}

	public Set<String> getPinnedSmsses() {
		String smsses = prefs.get( Constants.PIN_KEY, "" );
		Set<String> pins = new HashSet<>();
		if ( !smsses.isEmpty() ) {
			pins.addAll( Arrays.asList( smsses.split( ";" ) ) );
		}

		return pins;
	}

	public Map<String, String> getNamespaces() {
		Map<String, String> namespaces = new LinkedHashMap<>();

		String ns = get( Constants.USERPREF_NAMESPACES, "" );
		for ( String s : ns.split( ";" ) ) {
			int idx = s.indexOf( ":" );
			if( idx > 0 ){
				namespaces.put( s.substring( 0, idx ), s.substring( idx + 1 ) );
			}
		}

		return namespaces;
	}

	public void setNamespaces( Map<String, String> ns ) {
		StringBuilder sb = new StringBuilder();
		for ( Map.Entry<String, String> en : ns.entrySet() ) {
			if ( sb.length() > 0 ) {
				sb.append( ";" );
			}

			sb.append( en.getKey() ).append( ":" ).append( en.getValue() );
		}
		
		put( Constants.USERPREF_NAMESPACES, sb.toString() );		
	}

	/**
	 * Removes an SMSS file location that no longer exists, or can't be opened
	 *
	 * @param smssloc the location to forget
	 */
	public void removePin( String smssloc ) {
		Set<String> names = getPinnedSmsses();
		names.remove( smssloc );
		writePins( names );
	}

	/**
	 * Toggles the pinning of the given repository
	 *
	 * @param eng the repository to toggle
	 */
	public void togglePin( IEngine eng ) {
		Set<String> names = getPinnedSmsses();
		String smssloc = eng.getProperty( Constants.SMSS_LOCATION );
		if ( Boolean.parseBoolean( eng.getProperty( Constants.PIN_KEY ) ) ) {
			eng.setProperty( Constants.PIN_KEY, null );
			names.remove( smssloc );
		}
		else {
			eng.setProperty( Constants.PIN_KEY, Boolean.toString( true ) );
			names.add( smssloc );
		}

		writePins( names );
	}

	public boolean getBoolean( String key, boolean def ) {
		return prefs.getBoolean( key, def );
	}

	public void putBoolean( String key, boolean val ) {
		prefs.putBoolean( key, val );
	}

	public String get( String key, String def ) {
		return prefs.get( key, def );
	}

	public void put( String key, String val ) {
		prefs.put( key, val );
	}

	public void set( Class<?> k, String key, String value ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		spref.put( key, value );
	}

	public String get( Class<?> k, String key, String def ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		return spref.get( key, def );
	}

	public void set( Class<?> k, String key, int value ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		spref.putInt( key, value );
	}

	public int get( Class<?> k, String key, int def ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		return spref.getInt( key, def );
	}

	public void set( Class<?> k, String key, double value ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		spref.putDouble( key, value );
	}

	public double get( Class<?> k, String key, double def ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		return spref.getDouble( key, def );
	}

	public void set( Class<?> k, String key, boolean value ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		spref.putBoolean( key, value );
	}

	public boolean get( Class<?> k, String key, boolean def ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		return spref.getBoolean( key, def );
	}

	public void set( Class<?> k, String key, byte[] value ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		spref.putByteArray( key, value );
	}

	public byte[] get( Class<?> k, String key, byte[] def ) {
		Preferences spref = Preferences.userNodeForPackage( k );
		return spref.getByteArray( key, def );
	}

	private void writePins( Set<String> pins ) {
		StringBuilder sb = new StringBuilder();
		for ( String smss : pins ) {
			if ( 0 != sb.length() ) {
				sb.append( ";" );
			}
			sb.append( smss );
		}
		prefs.put( Constants.PIN_KEY, sb.toString() );
	}
}
