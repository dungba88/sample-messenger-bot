package org.travelbot.java.controllers;

import java.util.Optional;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.trigger.TriggerManager;
import org.travelbot.java.dto.MessengerEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerVerificationException;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class MessengerWebhookController {

	private TriggerManager manager;

	private Messenger messenger;
	
	public MessengerWebhookController(TriggerManager manager, Messenger messenger) {
		this.manager = manager;
		this.messenger = messenger;
	}

	public void handle(RoutingContext rc) {
		long start = System.currentTimeMillis();
		HttpServerResponse response = rc.response();
		response.putHeader("Content-Type", "application/json");
		
		String payload = rc.getBodyAsString();
		String signature = rc.request().getHeader("X-Hub-Signature");
		
		try {
			messenger.onReceiveEvents(payload, Optional.empty(), event -> {
				System.out.println("Decode event: " + (System.currentTimeMillis() - start) + "ms");
				if (!event.isTextMessageEvent()) {
					response.end();
					return;
				}
				manager.fire("fb_msg_received", new MessengerEvent(event), triggerResponse -> {
					onDone(triggerResponse, response, rc);
					System.out.println((System.currentTimeMillis() - start) + "ms");
				}, exception -> {
					onFail(exception, response, rc);
				});
			});
		} catch (MessengerVerificationException ex) {
			rc.fail(ex);
		}
	}
	
	private void onFail(Throwable exception, HttpServerResponse response, RoutingContext rc) {
		rc.fail(exception);
	}

	private void onDone(BaseResponse triggerResponse, HttpServerResponse response, RoutingContext rc) {
		if (triggerResponse == null) {
			response.end();
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String strResponse = mapper.writeValueAsString(triggerResponse);
			response.end(strResponse);
		} catch (JsonProcessingException e) {
			rc.fail(e);
		}
	}
}
