package org.travelbot.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.BaseRequest;
import org.joo.scorpius.support.bootstrap.AbstractBootstrap;
import org.joo.scorpius.support.message.ExecutionContextExceptionMessage;
import org.joo.scorpius.trigger.TriggerEvent;
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy;
import org.travelbot.java.dto.messenger.MessengerEvent;

import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.typesafe.config.Config;

public class TriggerConfigurator extends AbstractBootstrap {

    private static final Logger logger = LogManager.getLogger(TriggerConfigurator.class);

    public void run() {
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

        if (config.getBoolean("log.trigger.exception") && config.getBoolean("log.trigger.send_exception"))
            registerTriggerExceptionHandler(applicationContext);
    }

    private void registerTriggerExceptionHandler(ApplicationContext applicationContext) {
        triggerManager.addEventHandler(TriggerEvent.EXCEPTION, (event, msg) -> {
            ExecutionContextExceptionMessage exceptionMessage = (ExecutionContextExceptionMessage) msg;
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
}