package org.travelbot.java.utils;

import java.util.List;
import java.util.Optional;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.support.payload.PayloadAbstractFactory;
import org.travelbot.java.support.payload.PayloadFactory;
import org.travelbot.java.support.payload.QuickRepliesPayloadFactory;
import org.travelbot.java.support.payload.SenderActionPayloadFactory;

import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.quickreply.QuickReply;
import com.github.messenger4j.send.senderaction.SenderAction;
import com.typesafe.config.Config;

public final class MessengerUtils {

    private MessengerUtils() {

    }

    public static Promise<Boolean, Throwable> sendAction(TriggerExecutionContext executionContext, String recipientId,
            SenderAction senderAction) {
        if (recipientId == null || recipientId.isEmpty())
            return new SimpleDonePromise<>(false);
        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        AsyncTaskRunner runner = applicationContext.getTaskRunner();
        return runner.supplyTask(() -> {
            final PayloadFactory payloadFactory = new SenderActionPayloadFactory(senderAction);
            return sendWithPayloadFactory(executionContext, payloadFactory, recipientId);
        });
    }

    public static Promise<Boolean, Throwable> sendText(TriggerExecutionContext executionContext, String recipientId,
            String text) {
        return sendQuickReply(executionContext, recipientId, text, Optional.empty());
    }

    public static Promise<Boolean, Throwable> sendQuickReply(TriggerExecutionContext executionContext,
            String recipientId, String text, Optional<List<QuickReply>> quickReplies) {
        if (recipientId == null || recipientId.isEmpty())
            return new SimpleDonePromise<>(false);
        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        AsyncTaskRunner runner = applicationContext.getTaskRunner();
        return runner.supplyTask(() -> {
            final PayloadFactory payloadFactory = new QuickRepliesPayloadFactory(text, quickReplies);
            return sendWithPayloadFactory(executionContext, payloadFactory, recipientId);
        });
    }

    public static Promise<Boolean, Throwable> sendWithConfig(TriggerExecutionContext executionContext,
            String recipientId, Config cfg) {
        if (recipientId == null || recipientId.isEmpty())
            return new SimpleDonePromise<>(false);
        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        AsyncTaskRunner runner = applicationContext.getTaskRunner();
        return runner.supplyTask(() -> {
            final PayloadFactory payloadFactory = PayloadAbstractFactory.createFactory(cfg);
            if (payloadFactory == null)
                return false;
            return sendWithPayloadFactory(executionContext, payloadFactory, recipientId);
        });
    }

    private static boolean sendWithPayloadFactory(TriggerExecutionContext executionContext,
            PayloadFactory payloadFactory, String recipientId) {
        if (recipientId == null || recipientId.isEmpty())
            return false;

        final Payload payload = payloadFactory.create(recipientId);

        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        try {
            applicationContext.getMessenger().send(payload);
            return true;
        } catch (MessengerApiException | MessengerIOException e) {
            executionContext.fail(new TriggerExecutionException(e));
        }
        return false;
    }
}
