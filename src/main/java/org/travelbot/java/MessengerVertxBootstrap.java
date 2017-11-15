package org.travelbot.java;

import java.util.concurrent.Executors;

import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.triggers.MessageReceivedTrigger;

import com.github.messenger4j.Messenger;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;

public class MessengerVertxBootstrap extends VertxBootstrap {
	
	private final static String ACCESS_TOKEN = "EAABzrMo98iQBAJpePVHI5PtyxTm0jg4SZCJORFWDH6WKeCjAhWLCMA1VeuXRzk50yXxWxGWbZBNe9NnpPX4T2G7d19oZAVj0bHbJMZBO0z5IUEHxyxft1oZASAq7ZBoQsN5ddeIhJ12pEqsMLhpV51beNOrVWWuuuYNPLudeyYrwZDZD";
	
	private final static String APP_SECRET = "bc1cb829a28c5df32bc298433b2a32d2";
	
	private final static String VERIFY_TOKEN = "MeoHeoCho";
	
	private Messenger messenger;

	public void run() {
		messenger = Messenger.create(ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN);
		
		configureTriggers();

		VertxOptions options = new VertxOptions().setEventLoopPoolSize(8);
		configureServer(options, 9090);
	}

	public Router configureRoutes(Vertx vertx) {
		Router router = super.configureRoutes(vertx);
		router.get("/fb_msg_hook").handler(new MessengerChallengeController(messenger)::handle);
		router.post("/fb_msg_hook").handler(new MessengerWebhookController(triggerManager, messenger)::handle);
		return router;
	}

	public void configureTriggers() {
		triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, Executors.newFixedThreadPool(3),
				ProducerType.MULTI, new YieldingWaitStrategy()));
		triggerManager.registerTrigger("fb_msg_received").withAction(MessageReceivedTrigger::new);
	}
}