package org.travelbot.java;

import java.util.List;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.joo.scorpius.support.BaseRequest;
import org.joo.scorpius.support.builders.contracts.IdGenerator;
import org.joo.scorpius.support.builders.id.TimeBasedIdGenerator;
import org.joo.scorpius.support.message.CustomMessage;
import org.joo.scorpius.support.message.ExecutionContextExceptionMessage;
import org.joo.scorpius.support.message.ExecutionContextFinishMessage;
import org.joo.scorpius.support.message.ExecutionContextStartMessage;
import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.joo.scorpius.trigger.Trigger;
import org.joo.scorpius.trigger.TriggerConfig;
import org.joo.scorpius.trigger.TriggerEvent;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.dto.ErrorResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.exceptions.BadRequestException;
import org.travelbot.java.exceptions.UnauthorizedAccessException;
import org.travelbot.java.logging.AnnotatedExecutionContextExceptionMessage;
import org.travelbot.java.logging.AnnotatedExecutionContextFinishMessage;
import org.travelbot.java.logging.AnnotatedExecutionContextStartMessage;
import org.travelbot.java.logging.AnnotatedGelfJsonAppender;
import org.travelbot.java.logging.HttpRequestMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import com.typesafe.config.Config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;

public class MessengerVertxBootstrap extends VertxBootstrap {

    static {
        PluginManager.addPackage(AnnotatedGelfJsonAppender.class.getPackage().getName());
    }

    private final static Logger logger = LogManager.getLogger(MessengerVertxBootstrap.class);

    private ObjectMapper mapper = new ObjectMapper();

    public void run() {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        msgApplicationContext.override(IdGenerator.class, new TimeBasedIdGenerator());

        configureTriggers();

        configureServer(new VertxOptions().setEventLoopPoolSize(8), msgApplicationContext.getPort());
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

        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;
        List<? extends Config> configList = msgApplicationContext.getConfig().getConfigList("triggers");

        configList.stream().map(this::parseTriggerConfig).filter(cfg -> cfg != null).forEach(cfg -> {
            triggerManager.registerTrigger(cfg.getEvent(), cfg.getConfig());
        });

        registerEventHandlers();
    }

    private TriggerConfigWrapper parseTriggerConfig(Config cfg) {
        String condition = cfg.hasPath("condition") ? cfg.getString("condition") : null;
        String action = cfg.getString("action");

        TriggerConfig config;
        try {
            config = parseTriggerConfig(condition, action);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            if (logger.isErrorEnabled())
                logger.error("Exception occurred while trying to load triggers", e);
            return null;
        }

        return new TriggerConfigWrapper(cfg.getString("event"), config);
    }

    @SuppressWarnings("unchecked")
    private TriggerConfig parseTriggerConfig(String condition, String action)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        TriggerConfig config = new TriggerConfig();
        if (condition != null)
            config.withCondition(condition);
        Class<Trigger<?, ?>> clazz = (Class<Trigger<?, ?>>) Class.forName(action);
        config.withAction(clazz.newInstance());
        return config;
    }

    private void registerEventHandlers() {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        if (msgApplicationContext.getConfig().getBoolean("log.trigger.exception"))
            registerTriggerExceptionHandler(msgApplicationContext);

        if (msgApplicationContext.getConfig().getBoolean("log.trigger.start"))
            registerTriggerStartHandler();

        if (msgApplicationContext.getConfig().getBoolean("log.trigger.finish"))
            registerTriggerFinishHandler();

        if (msgApplicationContext.getConfig().getBoolean("log.trigger.custom"))
            registerTriggerCustomHandler();
    }

    private void registerTriggerExceptionHandler(MessengerApplicationContext msgApplicationContext) {
        final boolean sendExceptionToUser = msgApplicationContext.getConfig().getBoolean("log.trigger.send_exception");
        triggerManager.addEventHandler(TriggerEvent.EXCEPTION, (event, msg) -> {
            ExecutionContextExceptionMessage exceptionMessage = (ExecutionContextExceptionMessage) msg;
            if (logger.isErrorEnabled())
                logger.error(new AnnotatedExecutionContextExceptionMessage(exceptionMessage));

            if (sendExceptionToUser)
                sendExceptionToUser(msgApplicationContext, exceptionMessage);
        });
    }

    private void sendExceptionToUser(MessengerApplicationContext msgApplicationContext,
            ExecutionContextExceptionMessage exceptionMessage) {
        BaseRequest request = exceptionMessage.getRequest();
        if (!(request instanceof MessengerEvent))
            return;
        String recipientId = ((MessengerEvent) request).getBaseEvent().senderId();
        final Payload payload = MessagePayload.create(recipientId,
                TextMessage.create(exceptionMessage.getCause().getMessage()));
        try {
            msgApplicationContext.getMessenger().send(payload);
        } catch (MessengerApiException | MessengerIOException e) {
        }
    }

    private void registerTriggerStartHandler() {
        triggerManager.addEventHandler(TriggerEvent.START, (event, msg) -> {
            ExecutionContextStartMessage startMessage = (ExecutionContextStartMessage) msg;
            if (logger.isDebugEnabled())
                logger.debug(new AnnotatedExecutionContextStartMessage(startMessage));
        });
    }

    private void registerTriggerFinishHandler() {
        triggerManager.addEventHandler(TriggerEvent.FINISH, (event, msg) -> {
            ExecutionContextFinishMessage finishMessage = (ExecutionContextFinishMessage) msg;
            if (logger.isDebugEnabled())
                logger.debug(new AnnotatedExecutionContextFinishMessage(finishMessage));
        });
    }

    private void registerTriggerCustomHandler() {
        triggerManager.addEventHandler(TriggerEvent.CUSTOM, (event, msg) -> {
            CustomMessage customMsg = (CustomMessage) msg;
            if (!(customMsg.getCustomObject() instanceof HttpRequestMessage))
                return;
            HttpRequestMessage httpMsg = (HttpRequestMessage) customMsg.getCustomObject();
            httpMsg.putField("eventName", customMsg.getName());
            if (logger.isDebugEnabled())
                logger.debug(httpMsg);
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

class TriggerConfigWrapper {

    private final @Getter String event;

    private final @Getter TriggerConfig config;

    public TriggerConfigWrapper(final String event, final TriggerConfig config) {
        this.event = event;
        this.config = config;
    }
}