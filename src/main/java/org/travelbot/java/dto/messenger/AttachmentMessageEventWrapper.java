package org.travelbot.java.dto.messenger;

import com.github.messenger4j.webhook.event.AttachmentMessageEvent;

public class AttachmentMessageEventWrapper extends BaseMessageEvent {

	private static final long serialVersionUID = -3676060746696584293L;

	public AttachmentMessageEventWrapper(AttachmentMessageEvent event) {
		super(event);
	}
	
	public String getMessageId() {
		return ((AttachmentMessageEvent)event).messageId();
	}
}
