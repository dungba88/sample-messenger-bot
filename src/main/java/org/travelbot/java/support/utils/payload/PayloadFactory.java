package org.travelbot.java.support.utils.payload;

import com.github.messenger4j.send.Payload;

public interface PayloadFactory {

    public Payload create(final String recipientId);
}
