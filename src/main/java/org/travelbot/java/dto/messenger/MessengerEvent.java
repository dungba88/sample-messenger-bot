package org.travelbot.java.dto.messenger;

import org.joo.scorpius.support.BaseRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.messenger4j.webhook.Event;

@JsonInclude(Include.NON_NULL)
public class MessengerEvent extends BaseRequest {

    private static final long serialVersionUID = 4086780811845076530L;

    private final long createdTime;

    private final Event originalEvent;

    public MessengerEvent(Event originalEvent) {
        this.originalEvent = originalEvent;
        this.createdTime = System.currentTimeMillis();
    }

    @JsonIgnore
    public Event getOriginalEvent() {
        return originalEvent;
    }

    public TextMessageEventWrapper getTextMessageEvent() {
        if (originalEvent.isTextMessageEvent())
            return new TextMessageEventWrapper(originalEvent.asTextMessageEvent());
        return null;
    }

    public QuickReplyMessageEventWrapper getQuickReplyMessageEvent() {
        if (originalEvent.isQuickReplyMessageEvent())
            return new QuickReplyMessageEventWrapper(originalEvent.asQuickReplyMessageEvent());
        return null;
    }

    public AttachmentMessageEventWrapper getAttachmentMessageEvent() {
        if (originalEvent.isAttachmentMessageEvent())
            return new AttachmentMessageEventWrapper(originalEvent.asAttachmentMessageEvent());
        return null;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
