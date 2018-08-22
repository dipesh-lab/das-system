package org.das.exception;

public class InvalidRequestException extends RuntimeException {
	
	private static final long serialVersionUID = 51L;

	public InvalidRequestException() {
		super("Invalid incoming request");
	}
	
}
