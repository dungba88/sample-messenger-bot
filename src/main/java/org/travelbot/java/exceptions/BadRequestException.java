package org.travelbot.java.exceptions;

public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 5097521208822815131L;

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
