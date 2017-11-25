package org.travelbot.java.triggers;

import java.util.Optional;

import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.TriggerManager;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.dto.IntentRequest;
import org.travelbot.java.dto.ParseIntentRequest;
import org.travelbot.java.dto.ParseIntentResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.dto.messenger.MessengerResponse;
import org.travelbot.java.utils.MessengerUtils;

import com.github.messenger4j.send.senderaction.SenderAction;

public class MessageReceivedTrigger extends AbstractTrigger<MessengerEvent, MessengerResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        executionContext.finish(null);

        final TriggerManager manager = executionContext.getTriggerManager();
        final MessengerEvent event = (MessengerEvent) executionContext.getRequest();

        final String senderId = event.getOriginalEvent().senderId();
        final String text = event.getOriginalEvent().asTextMessageEvent().text();

        // always mark seen and show typing on
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.MARK_SEEN);
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.TYPING_ON);

        Optional<String> traceId = event.fetchRawTraceId();

        // call trigger to parse intent
        manager.fire("parse_intent", new ParseIntentRequest(traceId, text, event)).fail(ex -> {
            executionContext.fail(ex);
        }).pipeDone(response -> {
            IntentRequest intentRequest = new IntentRequest(traceId, (ParseIntentResponse) response, event);

            // call trigger to handle intent
            String intent = intentRequest.getResponse().getIntent();
            return manager.fire("intent." + intent, intentRequest);
        }).done(response -> {
            // finally show typing off
            markTypingOff(executionContext, senderId);
        }).fail(ex -> {
            executionContext.fail(ex);
            // finally show typing off
            markTypingOff(executionContext, senderId);
        });
    }

    private void markTypingOff(TriggerExecutionContext executionContext, String senderId) {
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.TYPING_OFF);
    }
}
