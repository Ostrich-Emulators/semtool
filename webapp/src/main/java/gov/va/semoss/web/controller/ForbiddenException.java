/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author ryan
 */
@ResponseStatus( HttpStatus.FORBIDDEN )
public class ForbiddenException extends RuntimeException {

	public ForbiddenException() {
	}

	public ForbiddenException( String message ) {
		super( message );
	}

	public ForbiddenException( String message, Throwable cause ) {
		super( message, cause );
	}

	public ForbiddenException( Throwable cause ) {
		super( cause );
	}

	public ForbiddenException( String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}
