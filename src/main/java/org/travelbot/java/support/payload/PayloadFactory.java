package org.travelbot.java.support.payload;

import com.github.messenger4j.send.Payload;

public interface PayloadFactory {

    public Payload create(final String recipientId);
}
