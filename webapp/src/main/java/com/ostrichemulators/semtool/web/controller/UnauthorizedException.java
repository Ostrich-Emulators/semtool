/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author ryan
 */
@ResponseStatus( HttpStatus.UNAUTHORIZED )
public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException() {
	}

	public UnauthorizedException( String message ) {
		super( message );
	}

	public UnauthorizedException( String message, Throwable cause ) {
		super( message, cause );
	}

	public UnauthorizedException( Throwable cause ) {
		super( cause );
	}

	public UnauthorizedException( String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}
