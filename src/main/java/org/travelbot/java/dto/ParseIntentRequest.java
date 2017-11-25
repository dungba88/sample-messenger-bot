package org.travelbot.java.dto;

import org.joo.scorpius.support.BaseRequest;
import org.travelbot.java.dto.messenger.MessengerEvent;

import lombok.Getter;

public class ParseIntentRequest extends BaseRequest {

    private static final long serialVersionUID = 1364140780792924936L;

    private final @Getter String text;
    
    private final @Getter MessengerEvent event;
    
    public ParseIntentRequest(final String text, final MessengerEvent event) {
        this.text = text;
        this.event = event;
    }
}
