/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.util;

import org.apache.commons.codec.language.Soundex;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.StringDistance;

/**
 *
 * @author ryan
 */
public class SoundexDistance implements StringDistance {

	private final Soundex metap = new Soundex();
	private final NGramDistance levy = new NGramDistance();

	@Override
	public float getDistance( String s1, String s2 ) {
		String str1 = tokenizeAndEncode( s1 );
		String str2 = tokenizeAndEncode( s2 );
		float dist = levy.getDistance( str1, str2 );
		return dist;
	}

	private String tokenizeAndEncode( String string ) {
		StringBuilder sb = new StringBuilder();
		for ( String s : string.split( "\\s+" ) ) {
			sb.append( metap.soundex( s ) );
		}

		return sb.toString();
	}
}
