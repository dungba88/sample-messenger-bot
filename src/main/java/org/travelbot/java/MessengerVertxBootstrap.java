package org.travelbot.java;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.joo.scorpius.support.BaseRequest;
import org.joo.scorpius.support.message.CustomMessage;
import org.joo.scorpius.support.message.ExecutionContextExceptionMessage;
import org.joo.scorpius.support.message.ExecutionContextStartMessage;
import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.joo.scorpius.trigger.TriggerEvent;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.dto.ErrorResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.exceptions.BadRequestException;
import org.travelbot.java.exceptions.UnauthorizedAccessException;
import org.travelbot.java.logging.AnnotatedExecutionContextExceptionMessage;
import org.travelbot.java.logging.AnnotatedExecutionContextStartMessage;
import org.travelbot.java.logging.AnnotatedGelfJsonAppender;
import org.travelbot.java.logging.HttpRequestMessage;
import org.travelbot.java.triggers.MessageReceivedTrigger;
import org.travelbot.java.triggers.NoIntentTrigger;
import org.travelbot.java.triggers.ParseIntentTrigger;
import org.travelbot.java.triggers.SimpleReplyIntentTrigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MessengerVertxBootstrap extends VertxBootstrap {

    static {
        PluginManager.addPackage(AnnotatedGelfJsonAppender.class.getPackage().getName());
    }

    private final static Logger logger = LogManager.getLogger(MessengerVertxBootstrap.class);

    private ObjectMapper mapper = new ObjectMapper();

    public void run() {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        configureTriggers();

        VertxOptions options = new VertxOptions().setEventLoopPoolSize(8);
        configureServer(options, msgApplicationContext.getPort());
    }

    protected Router configureRoutes(Vertx vertx) {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        Router router = super.configureRoutes(vertx);
        router.get("/fb_msg_hook")
                .handler(new MessengerChallengeController(msgApplicationContext.getMessenger())::handle);
        router.post("/fb_msg_hook")
                .handler(new MessengerWebhookController(triggerManager, msgApplicationContext.getMessenger())::handle);
        router.route().failureHandler(this::handleFailure);
        return router;
    }

    private void configureTriggers() {
        triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, Executors.newFixedThreadPool(3),
                ProducerType.MULTI, new YieldingWaitStrategy()));

        triggerManager.registerTrigger("fb_msg_received").withAction(MessageReceivedTrigger::new);
        triggerManager.registerTrigger("parse_intent").withAction(ParseIntentTrigger::new);
        triggerManager.registerTrigger("intent.greeting").withAction(SimpleReplyIntentTrigger::new);
        triggerManager.registerTrigger("intent.inquire_name").withAction(SimpleReplyIntentTrigger::new);
        triggerManager.registerTrigger("no_intent").withAction(NoIntentTrigger::new);

        registerEventHandlers();
    }

    private void registerEventHandlers() {
        triggerManager.addEventHandler(TriggerEvent.EXCEPTION, (event, msg) -> {
            ExecutionContextExceptionMessage exceptionMessage = (ExecutionContextExceptionMessage) msg;
            if (logger.isErrorEnabled())
                logger.error(new AnnotatedExecutionContextExceptionMessage(exceptionMessage));
            
            BaseRequest request = exceptionMessage.getRequest();
            if (request instanceof MessengerEvent) {
                MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;
                String recipientId = ((MessengerEvent)request).getBaseEvent().senderId();
                final Payload payload = MessagePayload.create(recipientId, TextMessage.create(exceptionMessage.getCause().getMessage()));
                try {
                    msgApplicationContext.getMessenger().send(payload);
                } catch (MessengerApiException | MessengerIOException e) {
                }
            }
        });

        triggerManager.addEventHandler(TriggerEvent.START, (event, msg) -> {
            ExecutionContextStartMessage startMessage = (ExecutionContextStartMessage) msg;
            if (logger.isDebugEnabled())
                logger.debug(new AnnotatedExecutionContextStartMessage(startMessage));
        });

        triggerManager.addEventHandler(TriggerEvent.CUSTOM, (event, msg) -> {
            CustomMessage customMsg = (CustomMessage) msg;
            if (customMsg.getCustomObject() instanceof HttpRequestMessage) {
                HttpRequestMessage httpMsg = (HttpRequestMessage) customMsg.getCustomObject();
                httpMsg.putField("eventName", customMsg.getName());
                if (logger.isDebugEnabled())
                    logger.debug(httpMsg);
            }
        });
    }

    private void handleFailure(RoutingContext rc) {
        logger.error("Failure on handling request", rc.failure());

        int statusCode = extractStatusCodeFromFailure(rc);
        rc.response().setStatusCode(statusCode);
        String value = serialize(new ErrorResponse(rc.failure()));
        if (value != null)
            rc.response().end(value);
    }

    private int extractStatusCodeFromFailure(RoutingContext rc) {
        if (rc.statusCode() != -1)
            return rc.statusCode();
        if (rc.failure() instanceof UnauthorizedAccessException)
            return 403;
        if (rc.failure() instanceof BadRequestException)
            return 400;
        return 500;
    }

    private String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        return null;
    }
}