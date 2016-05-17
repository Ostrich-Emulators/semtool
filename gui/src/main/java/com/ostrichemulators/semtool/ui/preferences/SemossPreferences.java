/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.ui.preferences;

import java.util.prefs.Preferences;
import org.apache.log4j.Logger;

/**
 * A class to facilitate getting/setting UI preferences and such
 *
 * @author ryan
 */
public class SemossPreferences {

	private static final Logger log = Logger.getLogger( SemossPreferences.class );

	private SemossPreferences() {

	}

	public static Preferences get() {
		return Preferences.userNodeForPackage( SemossPreferences.class );
	}
}
