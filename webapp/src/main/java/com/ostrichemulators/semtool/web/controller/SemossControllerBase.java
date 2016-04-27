/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author ryan
 */
public class SemossControllerBase {

	@ExceptionHandler( IllegalArgumentException.class )
	@ResponseStatus( value = HttpStatus.NOT_FOUND )
	public String handleMissingIdError( IllegalArgumentException ex,
			HttpServletRequest req ) {
		req.setAttribute( "message", ex.getMessage() );
		return "exception";
	}

	@ExceptionHandler( ValidationException.class )
	@ResponseStatus( value = HttpStatus.EXPECTATION_FAILED )
	public String handleValidationError( ValidationException ex,
			HttpServletRequest req ) {
		req.setAttribute( "message", ex.getMessage() );
		return "exception";
	}

	@ExceptionHandler( ConstraintViolationException.class )
	@ResponseStatus( value = HttpStatus.EXPECTATION_FAILED )
	public String handleValidationError( ConstraintViolationException ex,
			HttpServletRequest req ) {
		req.setAttribute( "message", ex.getMessage() );
		return "exception";
	}

	public static String urlencode( String raw ) {
		try {
			return URLEncoder.encode( raw, "UTF-8" );
		}
		catch ( Exception e ) {
			Logger.getLogger( SemossControllerBase.class ).error( e, e );
		}
		return raw;
	}

	public static String urldencode( String encoded ) {
		try {
			return URLDecoder.decode( encoded, "UTF-8" );
		}
		catch ( Exception e ) {
			Logger.getLogger( SemossControllerBase.class ).error( e, e );
		}
		return encoded;
	}

	protected boolean hasRole( Authentication auth, String role ) {
		for ( GrantedAuthority ga : auth.getAuthorities() ) {
			if ( ga.getAuthority().equals( role ) ) {
				return true;
			}
		}
		return false;
	}

}
