package org.travelbot.java.dto.messenger;

import com.github.messenger4j.webhook.event.TextMessageEvent;

public class TextMessageEventWrapper extends BaseMessageEvent {

    private static final long serialVersionUID = -6323285586290685714L;

    public TextMessageEventWrapper(TextMessageEvent event) {
        super(event);
    }

    public String getMessageId() {
        return ((TextMessageEvent) event).messageId();
    }

    public String getText() {
        return ((TextMessageEvent) event).text();
    }
}
