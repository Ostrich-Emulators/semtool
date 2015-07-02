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
public class ImportValidationException extends RuntimeException {

	public static enum ErrorType {

		UNKNOWN, INCONSISTENT_DATA, UNTYPED_DATA, MISSING_DATA, WRONG_TABTYPE,
		INVALID_TYPE, TOO_MUCH_DATA, INVALID_DATA, NOT_A_LOADING_SHEET
	};
	public final ErrorType error;

	public ImportValidationException() {
		error = ErrorType.UNKNOWN;
	}

	public ImportValidationException( String message ) {
		this( ErrorType.UNKNOWN, message );
	}

	public ImportValidationException( ErrorType err, String message ) {
		super( message );
		error = err;
	}

	public ImportValidationException( String message, Throwable cause ) {
		this( ErrorType.UNKNOWN, message, cause );
	}

	public ImportValidationException( ErrorType err, String message, Throwable cause ) {
		super( message, cause );
		error = err;
	}

	public ImportValidationException( Throwable cause ) {
		this( ErrorType.UNKNOWN, cause );
	}

	public ImportValidationException( ErrorType err, Throwable cause ) {
		super( cause );
		error = err;
	}

	public ImportValidationException( String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		this( ErrorType.UNKNOWN, message, cause, enableSuppression, writableStackTrace );
	}

	public ImportValidationException( ErrorType err, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
		error = err;
	}

	@Override
	public String toString() {
		return super.toString() + " type: " + error;
	}
}
