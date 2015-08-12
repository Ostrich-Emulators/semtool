/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * A Sanitizer that creates valid URIs in a deterministic fashion
 *
 * @author ryan
 */
public class DeterministicSanitizer implements UriSanitizer {

	private final Pattern PAT = Pattern.compile( "([a-z])" );

	@Override
	public String sanitize( String raw ) {
		if ( RDFDatatypeTools.isValidUriChars( raw ) ) {
			return raw;
		}

		// Attempt a simple sanitizing:
		String rawWithUnderscores = raw.trim().replaceAll( " ", "_" );
		if ( RDFDatatypeTools.isValidUriChars( rawWithUnderscores ) ) {
			return rawWithUnderscores;
		}

		String md5 = DigestUtils.md5Hex( raw );
    // md5 might start with a number, so determinisitically decide on an
		// reasonably-random leading character. Here, we just add up the indexes of
		// the alpha characters, then use that to generate a new character
		Matcher m = PAT.matcher( md5 );
		int counter = 17;
		final int length = md5.length();
		final int limit = length / 2;
		while ( m.find() ) {
			long end = m.end( 1 );
			if ( end > limit ) {
				// don't want letters at the end to be too significant, so shrink them
				end -= limit;
			}

			counter += end;
		}

		counter = counter % 26;
		char leading = (char) ( 65 + counter ); // get ascii char A-Z
		return leading + md5;
	}
}
