package org.travelbot.java;

public class UnauthorizedAccessException extends RuntimeException {

	private static final long serialVersionUID = 5097521208822815131L;

	public UnauthorizedAccessException(String msg) {
		super(msg);
	}

	public UnauthorizedAccessException(Throwable cause) {
		super(cause);
	}

	public UnauthorizedAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
