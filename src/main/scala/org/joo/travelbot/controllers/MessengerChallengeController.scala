package org.joo.travelbot.controllers

import io.vertx.ext.web.RoutingContext

class MessengerChallengeController {

  def handle(rc: RoutingContext) {
    val response = rc.response()
    response.putHeader("Content-Type", "text/plain")

    val verifyToken = rc.request().getParam("hub.verify_token")
    val challenge = rc.request().getParam("hub.challenge")

    response.end(challenge)
  }
}