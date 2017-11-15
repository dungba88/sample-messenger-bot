package org.travelbot.java;

import java.util.concurrent.Executors;

import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.triggers.MessageReceivedTrigger;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;

public class MessengerVertxBootstrap extends VertxBootstrap {
	
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
		return router;
	}

	public void configureTriggers() {
		triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, Executors.newFixedThreadPool(3),
				ProducerType.MULTI, new YieldingWaitStrategy()));
		triggerManager.registerTrigger("fb_msg_received").withAction(MessageReceivedTrigger::new);
	}
}