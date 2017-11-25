package org.travelbot.java.utils;

import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.travelbot.java.MessengerApplicationContext;

import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.SenderActionPayload;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.senderaction.SenderAction;

public final class MessengerUtils {

    public static boolean sendAction(TriggerExecutionContext executionContext, String recipientId,
            SenderAction senderAction) {
        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        final SenderActionPayload payload = SenderActionPayload.create(recipientId, senderAction);
        try {
            applicationContext.getMessenger().send(payload);
            return true;
        } catch (MessengerApiException | MessengerIOException e) {
            executionContext.fail(new TriggerExecutionException(e));
        }
        return false;
    }

    public static boolean sendText(TriggerExecutionContext executionContext, String recipientId, String text) {
        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        final Payload payload = MessagePayload.create(recipientId, TextMessage.create(text));
        try {
            applicationContext.getMessenger().send(payload);
            return true;
        } catch (MessengerApiException | MessengerIOException e) {
            executionContext.fail(new TriggerExecutionException(e));
        }
        return false;
    }
}
