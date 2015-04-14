/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.util;

import static gov.va.semoss.util.Utility.isValidUriChars;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.lang.RandomStringUtils;

/**
 * A Sanitizer that creates valid URIs in a deterministic fashion
 *
 * @author ryan
 */
public class UniqueSanitizer implements UriSanitizer {


	@Override
	public String sanitize( String raw ) {
		if ( Utility.isValidUriChars( raw ) ) {
			return raw;
		}

		String rawWithUnderscores = raw.trim().replaceAll( " ", "_" );
		return ( isValidUriChars( rawWithUnderscores )
				? rawWithUnderscores
				: RandomStringUtils.randomAlphabetic( 1 ) + UUID.randomUUID().toString() );
	}
}
