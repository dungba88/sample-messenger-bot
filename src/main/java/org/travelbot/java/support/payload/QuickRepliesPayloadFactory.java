package org.travelbot.java.support.payload;

import java.util.List;
import java.util.Optional;

import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.quickreply.QuickReply;

public class QuickRepliesPayloadFactory implements PayloadFactory {

    private String text;

    private Optional<List<QuickReply>> quickReplies;

    public QuickRepliesPayloadFactory(final String text, final Optional<List<QuickReply>> quickReplies) {
        this.text = text;
        this.quickReplies = quickReplies;
    }

    @Override
    public Payload create(final String recipientId) {
        return MessagePayload.create(recipientId, TextMessage.create(text, quickReplies, Optional.empty()));
    }
}
