/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.poi.main;

/**
 *
 * @author ryan
 */
public class FileLoadingException extends Exception {

	public static enum ErrorType {

		UNKNOWN, INCONSISTENT_DATA, UNTYPED_DATA
	};
	private final ErrorType error;

	public FileLoadingException() {
		error = ErrorType.UNKNOWN;
	}

	public FileLoadingException( String message ) {
		this(ErrorType.UNKNOWN, message );
	}

	public FileLoadingException( ErrorType err, String message ) {
		super( message );
		error = err;
	}

	public FileLoadingException( String message, Throwable cause ) {
		this(ErrorType.UNKNOWN, message, cause );
	}

	public FileLoadingException( ErrorType err, String message, Throwable cause ) {
		super( message, cause );
		error = err;
	}

	public FileLoadingException( Throwable cause ) {
		this(ErrorType.UNKNOWN, cause );
	}

	public FileLoadingException( ErrorType err, Throwable cause ) {
		super( cause );
		error = err;
	}

	public FileLoadingException( String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		this(ErrorType.UNKNOWN, message, cause, enableSuppression, writableStackTrace );
	}

	public FileLoadingException( ErrorType err, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
		error = err;
	}
}
