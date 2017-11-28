package org.travelbot.java.dto.messenger;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import com.github.messenger4j.webhook.event.BaseEvent;

public class BaseMessageEvent implements Serializable {

    private static final long serialVersionUID = -6037621467747809537L;

    protected final transient BaseEvent event;

    public BaseMessageEvent(BaseEvent event) {
        this.event = event;
    }

    public String getSenderId() {
        return event.senderId();
    }

    public String getRecipientId() {
        return event.recipientId();
    }

    public Instant getTimestamp() {
        return event.timestamp();
    }

    public Map<String, Object> getExtendedProperties() {
        return event.extendedProperties();
    }
}
