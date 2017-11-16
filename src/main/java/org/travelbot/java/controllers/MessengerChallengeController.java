package org.travelbot.java.controllers;

import org.travelbot.java.UnauthorizedAccessException;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerVerificationException;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class MessengerChallengeController {
	
	private Messenger messenger;

	public MessengerChallengeController(Messenger messenger) {
		this.messenger = messenger;
	}

	public void handle(RoutingContext rc) {
		HttpServerResponse response = rc.response();
		response.putHeader("Content-Type", "text/plain");

		String mode = rc.request().getParam("hub.mode");
		String verifyToken = rc.request().getParam("hub.verify_token");
		String challenge = rc.request().getParam("hub.challenge");
		
		try {
			messenger.verifyWebhook(mode, verifyToken);
			response.end(challenge);
		} catch (MessengerVerificationException | IllegalArgumentException e) {
			rc.fail(new UnauthorizedAccessException(e));
		}
	}
}