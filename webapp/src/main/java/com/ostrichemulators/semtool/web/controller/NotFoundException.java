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
@ResponseStatus( HttpStatus.NOT_FOUND )
public class NotFoundException extends RuntimeException {

	public NotFoundException() {
	}

	public NotFoundException( String message ) {
		super( message );
	}

	public NotFoundException( String message, Throwable cause ) {
		super( message, cause );
	}

	public NotFoundException( Throwable cause ) {
		super( cause );
	}

	public NotFoundException( String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}
