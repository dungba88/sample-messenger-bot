package org.travelbot.java.dto.messenger;

import com.github.messenger4j.webhook.event.QuickReplyMessageEvent;

public class QuickReplyMessageEventWrapper extends BaseMessageEvent {

	private static final long serialVersionUID = -3676060746696584293L;

	public QuickReplyMessageEventWrapper(QuickReplyMessageEvent event) {
		super(event);
	}
	
	public String getMessageId() {
		return ((QuickReplyMessageEvent)event).messageId();
	}

	public String getText() {
		return ((QuickReplyMessageEvent)event).text();
	}
	
	public String getPayload() {
		return ((QuickReplyMessageEvent)event).payload();
	}
}
