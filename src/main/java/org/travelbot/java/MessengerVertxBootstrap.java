package org.travelbot.java;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.dto.ErrorResponse;
import org.travelbot.java.triggers.MessageReceivedTrigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MessengerVertxBootstrap extends VertxBootstrap {
	
	private Logger logger = LogManager.getLogger(MessengerVertxBootstrap.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public void run() {
		configureTriggers();

		VertxOptions options = new VertxOptions().setEventLoopPoolSize(8);
		configureServer(options, 9090);
	}

	public Router configureRoutes(Vertx vertx) {
		MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext)applicationContext;
		
		Router router = super.configureRoutes(vertx);
		router.get("/fb_msg_hook").handler(new MessengerChallengeController(msgApplicationContext.getMessenger())::handle);
		router.post("/fb_msg_hook").handler(new MessengerWebhookController(triggerManager, msgApplicationContext.getMessenger())::handle);
		router.route().failureHandler(this::handleFailure);
		return router;
	}

	public void configureTriggers() {
		triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, Executors.newFixedThreadPool(3),
				ProducerType.MULTI, new YieldingWaitStrategy()));
		triggerManager.registerTrigger("fb_msg_received").withAction(MessageReceivedTrigger::new);
	}
	
	private void handleFailure(RoutingContext rc) {
		int statusCode = extractStatusCodeFromFailure(rc);
		rc.response().setStatusCode(statusCode);
		try {
			String value = mapper.writeValueAsString(new ErrorResponse(rc.failure()));
			rc.response().end(value);
		} catch (JsonProcessingException e) {
			logger.error(e);
		}
		logger.error("Failure on handling request", rc.failure());
	}

	private int extractStatusCodeFromFailure(RoutingContext rc) {
		if (rc.statusCode() != -1) return rc.statusCode();
		if (rc.failure() instanceof UnauthorizedAccessException) return 403;
		return 500;
	}
}