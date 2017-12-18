package org.travelbot.java.support.payload;

import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.SenderActionPayload;
import com.github.messenger4j.send.senderaction.SenderAction;

public class SenderActionPayloadFactory implements PayloadFactory {

    private final SenderAction senderAction;

    public SenderActionPayloadFactory(SenderAction senderAction) {
        this.senderAction = senderAction;
    }

    @Override
    public Payload create(final String recipientId) {
        return SenderActionPayload.create(recipientId, senderAction);
    }
}
