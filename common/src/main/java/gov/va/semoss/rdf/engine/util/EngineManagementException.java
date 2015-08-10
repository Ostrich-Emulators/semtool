/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.semoss.rdf.engine.util;

/**
 *
 * @author ryan
 */
public class EngineManagementException extends Exception {

	public enum ErrorCode {

		DUPLICATE_NAME, UNREADABLE_SMSS, UNKNOWN, MISSING_REQUIRED_TUPLE, FILE_ERROR,
		MISSING_BASE_URI
	};

	private ErrorCode code;

	public EngineManagementException() {
		this( ErrorCode.UNKNOWN );
	}

	public EngineManagementException( String message ) {
		this( ErrorCode.UNKNOWN, message );
	}

	public EngineManagementException( String message, Throwable cause ) {
		this( ErrorCode.UNKNOWN, message, cause );
	}

	public EngineManagementException( Throwable cause ) {
		this( ErrorCode.UNKNOWN, cause );
	}

	public EngineManagementException( String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		this( ErrorCode.UNKNOWN, message, cause, enableSuppression, writableStackTrace );
	}

	public EngineManagementException( ErrorCode c ) {
		code = c;
	}

	public EngineManagementException( ErrorCode c, String message ) {
		super( message );
		code = c;
	}

	public EngineManagementException( ErrorCode c, String message, Throwable cause ) {
		super( message, cause );
		code = c;
	}

	public EngineManagementException( ErrorCode c, Throwable cause ) {
		super( cause );
		code = c;
	}

	public EngineManagementException( ErrorCode c, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
		code = c;
	}

	public ErrorCode getCode() {
		return code;
	}

	@Override
	public String toString() {
		return super.toString() + " code: " + code;
	}
}
