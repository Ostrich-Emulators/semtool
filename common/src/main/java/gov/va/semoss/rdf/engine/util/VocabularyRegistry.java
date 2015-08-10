/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ryan
 */
public class VocabularyRegistry {

	private static final Map<String, URL> vocabs = new HashMap<>();
	private static final Map<String, Boolean> vocabCheck = new HashMap<>();

	private VocabularyRegistry() {
	}

	/**
	 * Registers the given vocabulary with the given resource in a jar
	 *
	 * @param label
	 * @param jarpath
	 * @param enabled
	 */
	public static void registerVocabulary( String label, URL jarpath, boolean enabled ) {
		vocabs.put( label, jarpath );
		vocabCheck.put( label, enabled );
	}

	public static Map<String, URL> getVocabularies() {
		return new HashMap<>( vocabs );
	}

	public static Map<String, URL> getVocabularies( boolean enabled ) {
		Map<String, URL> ret = new HashMap<>();
		for ( Map.Entry<String, Boolean> en : vocabCheck.entrySet() ) {
			if ( en.getValue() ) {
				ret.put( en.getKey(), vocabs.get( en.getKey() ) );
			}
		}

		return ret;
	}

	public static Map<String, Boolean> getVocabularies2() {
		return new HashMap<>( vocabCheck );
	}

	public static boolean isEnabled( String label ) {
		return ( vocabCheck.containsKey( label ) ? vocabCheck.get( label ) : false );
	}

	public static URL getURL( String label ) {
		return ( vocabs.containsKey( label ) ? vocabs.get( label ) : null );
	}
}
