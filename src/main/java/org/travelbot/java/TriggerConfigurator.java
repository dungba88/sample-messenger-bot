package org.travelbot.java;

import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joo.scorpius.support.BaseRequest;
import org.joo.scorpius.support.message.CustomMessage;
import org.joo.scorpius.support.message.ExecutionContextExceptionMessage;
import org.joo.scorpius.support.message.ExecutionContextFinishMessage;
import org.joo.scorpius.support.message.ExecutionContextStartMessage;
import org.joo.scorpius.trigger.Trigger;
import org.joo.scorpius.trigger.TriggerConfig;
import org.joo.scorpius.trigger.TriggerEvent;
import org.joo.scorpius.trigger.TriggerManager;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.support.logging.AnnotatedExecutionContextExceptionMessage;
import org.travelbot.java.support.logging.AnnotatedExecutionContextFinishMessage;
import org.travelbot.java.support.logging.AnnotatedExecutionContextStartMessage;
import org.travelbot.java.support.logging.HttpRequestMessage;
import org.travelbot.java.support.utils.StopWatchBucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.typesafe.config.Config;

import lombok.Getter;

public class TriggerConfigurator {

    private static final Logger logger = LogManager.getLogger(TriggerConfigurator.class);

    private final TriggerManager triggerManager;

    private final MessengerApplicationContext applicationContext;

    private final ObjectMapper mapper;

    public TriggerConfigurator(TriggerManager triggerManager, MessengerApplicationContext applicationContext) {
        this.triggerManager = triggerManager;
        this.applicationContext = applicationContext;
        this.mapper = applicationContext.getObjectMapper();
    }

    public void configureTriggers() {
        triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, new YieldingWaitStrategy()));

        List<? extends Config> configList = applicationContext.getConfig().getConfigList("triggers");

        configList.stream().map(this::parseTriggerConfig).filter(Objects::nonNull)
                .forEach(cfg -> triggerManager.registerTrigger(cfg.getEvent(), cfg.getConfig()));

        registerEventHandlers();

        warmup();
    }

    private void warmup() {
        for (int i = 0; i < 1000; i++)
            triggerManager.fire("parse_intent", null);
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

    protected void registerEventHandlers() {
        if (applicationContext.getConfig().getBoolean("log.trigger.exception"))
            registerTriggerExceptionHandler(applicationContext);

        if (applicationContext.getConfig().getBoolean("log.trigger.create"))
            registerTriggerCreateHandler();

        if (applicationContext.getConfig().getBoolean("log.trigger.start"))
            registerTriggerStartHandler();

        if (applicationContext.getConfig().getBoolean("log.trigger.finish"))
            registerTriggerFinishHandler();

        if (applicationContext.getConfig().getBoolean("log.trigger.custom"))
            registerTriggerCustomHandler();
    }

    private void registerTriggerExceptionHandler(MessengerApplicationContext msgApplicationContext) {
        final boolean sendExceptionToUser = msgApplicationContext.getConfig().getBoolean("log.trigger.send_exception");
        triggerManager.addEventHandler(TriggerEvent.EXCEPTION, (event, msg) -> {
            ExecutionContextExceptionMessage exceptionMessage = (ExecutionContextExceptionMessage) msg;
            if (logger.isErrorEnabled())
                logger.error(new AnnotatedExecutionContextExceptionMessage(mapper, exceptionMessage));

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
            if (logger.isDebugEnabled())
                logger.debug("Exception occurred while trying to send exception to user", e);
        }
    }

    private void registerTriggerCreateHandler() {
        triggerManager.addEventHandler(TriggerEvent.CREATED, (event, msg) -> {
            ExecutionContextStartMessage startMessage = (ExecutionContextStartMessage) msg;
            StopWatchBucket.getInstance().start(startMessage.getId());
        });
    }

    private void registerTriggerStartHandler() {
        triggerManager.addEventHandler(TriggerEvent.START, (event, msg) -> {
            ExecutionContextStartMessage startMessage = (ExecutionContextStartMessage) msg;
            Long latency = StopWatchBucket.getInstance().stop(startMessage.getId());
            if (logger.isDebugEnabled())
                logger.debug(new AnnotatedExecutionContextStartMessage(mapper, startMessage, latency));
            StopWatchBucket.getInstance().start(startMessage.getId());
        });
    }

    private void registerTriggerFinishHandler() {
        triggerManager.addEventHandler(TriggerEvent.FINISH, (event, msg) -> {
            ExecutionContextFinishMessage finishMessage = (ExecutionContextFinishMessage) msg;
            Long elapsed = StopWatchBucket.getInstance().stop(finishMessage.getId());
            if (logger.isDebugEnabled())
                logger.debug(new AnnotatedExecutionContextFinishMessage(mapper, finishMessage, elapsed));
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

}

class TriggerConfigWrapper {

    private final @Getter String event;

    private final @Getter TriggerConfig config;

    public TriggerConfigWrapper(final String event, final TriggerConfig config) {
        this.event = event;
        this.config = config;
    }
}