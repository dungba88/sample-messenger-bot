package org.travelbot.java.dto;

import org.joo.scorpius.support.BaseResponse;

public class ErrorResponse extends BaseResponse {

    private static final long serialVersionUID = 2535606332240968175L;

    private static final int MAX_RECURSIVE_CAUSES = 5;

    private final String errorMsg;

    private final String cause;

    public ErrorResponse(Throwable cause) {
        this.cause = cause.getClass().getName();
        Throwable current = cause;
        int counter = 0;
        while (counter++ < MAX_RECURSIVE_CAUSES && current.getCause() != null) {
            current = current.getCause();
        }
        this.errorMsg = current.getMessage();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getCause() {
        return cause;
    }
}
