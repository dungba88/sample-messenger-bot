package org.travelbot.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.BaseRequest;
import org.joo.scorpius.support.bootstrap.AbstractBootstrap;
import org.joo.scorpius.support.message.CustomMessage;
import org.joo.scorpius.support.message.ExecutionContextExceptionMessage;
import org.joo.scorpius.support.message.ExecutionContextFinishMessage;
import org.joo.scorpius.support.message.ExecutionContextStartMessage;
import org.joo.scorpius.trigger.TriggerEvent;
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

public class TriggerConfigurator extends AbstractBootstrap {

    private static final Logger logger = LogManager.getLogger(TriggerConfigurator.class);

    private ObjectMapper mapper;

    public void run() {
        mapper = applicationContext.getInstance(ObjectMapper.class);
        triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, new YieldingWaitStrategy()));
        registerEventHandlers();
        warmup();
    }

    private void warmup() {
        for (int i = 0; i < 1000; i++)
            triggerManager.fire("parse_intent", null);
    }

    protected void registerEventHandlers() {
        Config config = applicationContext.getInstance(Config.class);

        if (config.getBoolean("log.trigger.exception")) {
            final boolean sendExceptionToUser = config.getBoolean("log.trigger.send_exception");
            registerTriggerExceptionHandler(applicationContext, sendExceptionToUser);
        }

        if (config.getBoolean("log.trigger.create"))
            registerTriggerCreateHandler();

        if (config.getBoolean("log.trigger.start"))
            registerTriggerStartHandler();

        if (config.getBoolean("log.trigger.finish"))
            registerTriggerFinishHandler();

        if (config.getBoolean("log.trigger.custom"))
            registerTriggerCustomHandler();
    }

    private void registerTriggerExceptionHandler(ApplicationContext applicationContext, boolean sendExceptionToUser) {
        triggerManager.addEventHandler(TriggerEvent.EXCEPTION, (event, msg) -> {
            ExecutionContextExceptionMessage exceptionMessage = (ExecutionContextExceptionMessage) msg;
            if (logger.isErrorEnabled())
                logger.error(new AnnotatedExecutionContextExceptionMessage(mapper, exceptionMessage));

            if (sendExceptionToUser)
                sendExceptionToUser((MessengerApplicationContext) applicationContext, exceptionMessage);
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