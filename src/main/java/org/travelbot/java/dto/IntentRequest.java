package org.travelbot.java.dto;

import org.joo.scorpius.support.BaseRequest;
import org.travelbot.java.dto.messenger.MessengerEvent;

import lombok.Getter;

public class IntentRequest extends BaseRequest {

    private static final long serialVersionUID = -1342537136855899736L;

    private final @Getter ParseIntentResponse response;

    private final @Getter MessengerEvent event;

    private final @Getter String senderId;
    
    public IntentRequest(final ParseIntentResponse response, final MessengerEvent event) {
        this.response = response;
        this.event = event;
        this.senderId = event.getOriginalEvent().senderId();
    }
}
