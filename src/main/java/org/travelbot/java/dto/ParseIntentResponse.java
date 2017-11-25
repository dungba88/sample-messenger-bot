package org.travelbot.java.dto;

import org.joo.scorpius.support.BaseResponse;

import lombok.Getter;

public class ParseIntentResponse extends BaseResponse {

    private static final long serialVersionUID = -2405093486571072528L;

    private final @Getter String intent;
    
    public ParseIntentResponse(final String intent) {
        this.intent = intent;
    }
}
