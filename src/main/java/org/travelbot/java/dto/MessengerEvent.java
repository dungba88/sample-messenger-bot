package org.travelbot.java.dto;
import org.joo.scorpius.support.BaseRequest;

import com.github.messenger4j.webhook.Event;

public class MessengerEvent extends BaseRequest {

	private static final long serialVersionUID = 4086780811845076530L;
	
	private final long createdTime;

	private final Event originalEvent;
	
	public MessengerEvent(Event originalEvent) {
		this.originalEvent = originalEvent;
		this.createdTime = System.currentTimeMillis();
	}

	public Event getOriginalEvent() {
		return originalEvent;
	}

	public long getCreatedTime() {
		return createdTime;
	}
}
