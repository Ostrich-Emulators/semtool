/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.security;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 *
 * @author ryan
 */
public class SemossCsrfMatcher implements RequestMatcher {

	private final Pattern allowedMethods = Pattern.compile( "^(GET|HEAD|TRACE|OPTIONS)$" );
	private final RegexRequestMatcher unprotectedMatcher
			= new RegexRequestMatcher( ".*/databases/([^/]+)(/repositories/(data|insights).*)?",
					null, true );

	@Override
	public boolean matches( HttpServletRequest request ) {
		if ( allowedMethods.matcher( request.getMethod() ).matches() ) {
			return false;
		}

		return !unprotectedMatcher.matches( request );
	}
}
