package org.travelbot.java.dto;

import org.joo.scorpius.support.BaseResponse;

import lombok.Getter;

public class IntentResponse extends BaseResponse {

    private static final long serialVersionUID = 2395351462910462066L;

    private final @Getter String response;

    public IntentResponse(String response) {
        this.response = response;
    }
}
