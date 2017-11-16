package org.travelbot.java.controllers;

import java.util.Optional;

import org.joo.scorpius.support.vertx.VertxMessageController;
import org.joo.scorpius.trigger.TriggerManager;
import org.travelbot.java.dto.MessengerEvent;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerVerificationException;
import com.github.messenger4j.webhook.Event;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class MessengerWebhookController extends VertxMessageController {

	private Messenger messenger;
	
	public MessengerWebhookController(TriggerManager manager, Messenger messenger) {
		super(manager);
		this.messenger = messenger;
	}

	public void handle(RoutingContext rc) {
		HttpServerResponse response = rc.response();
		response.putHeader("Content-Type", "application/json");
		
		String payload = rc.getBodyAsString();
//		String signature = rc.request().getHeader("X-Hub-Signature");
		
//		if (signature == null)
//			throw new UnauthorizedAccessException("signature cannot be null");
		
		try {
			messenger.onReceiveEvents(payload, Optional.empty(), event -> {
				handleEvent(rc, event);
			});
		} catch (MessengerVerificationException ex) {
			rc.fail(ex);
		}
	}

	private void handleEvent(RoutingContext rc, Event event) {
		String eventName = getEventNameForMessengerEvent(event);
		if (eventName == null) {
			rc.response().end();
			return;
		}
		
		MessengerEvent msgEvent = new MessengerEvent(event);
		msgEvent.attachTraceId(getTraceId(rc, triggerManager.getApplicationContext()));
		
		triggerManager.fire(eventName, msgEvent, triggerResponse -> {
			onDone(triggerResponse, rc.response(), rc);
		}, exception -> {
			onFail(exception, rc.response(), rc);
		});
	}
	
	private String getEventNameForMessengerEvent(Event event) {
		if (event.isTextMessageEvent()) return "fb_msg_received";
		if (event.isQuickReplyMessageEvent()) return "fb_reply_received";
		return null;
	}
}
