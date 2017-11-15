package org.travelbot.java.controllers;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class MessengerChallengeController {

	public void handle(RoutingContext rc) {
		HttpServerResponse response = rc.response();
		response.putHeader("Content-Type", "text/plain");

		String verifyToken = rc.request().getParam("hub.verify_token");
		String challenge = rc.request().getParam("hub.challenge");

		response.end(challenge);
	}
}