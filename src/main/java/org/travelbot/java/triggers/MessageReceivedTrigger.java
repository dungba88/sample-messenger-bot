package org.travelbot.java.triggers;

import java.util.Optional;

import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.Promise;
import org.joo.scorpius.support.BaseResponse;
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        executionContext.finish(null);

        final TriggerManager manager = executionContext.getTriggerManager();
        final MessengerEvent event = (MessengerEvent) executionContext.getRequest();

        final String senderId = event.getOriginalEvent().senderId();
        final String text = event.getOriginalEvent().asTextMessageEvent().text();

        Optional<String> traceId = event.fetchRawTraceId();

        // always mark seen and show typing on
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.MARK_SEEN);
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.TYPING_ON);

        // call trigger to parse intent
        manager.fire("parse_intent", new ParseIntentRequest(traceId, text, event)).fail(ex -> {
            executionContext.fail(ex);
        }).pipeDone((PipeDoneCallback<BaseResponse, BaseResponse, Throwable>) response -> {
            if (response == null)
                return (Promise)manager.fire("no_intent", event);
            
            ParseIntentResponse intentResponse = (ParseIntentResponse) response;

            // send user text about the intent
            MessengerUtils.sendText(executionContext, senderId, String.format("Recognize intent '%s' with confidence %d%%",
                    intentResponse.getIntent(), (int)(intentResponse.getConfidence() * 100)));

            // call trigger to handle intent
            IntentRequest intentRequest = new IntentRequest(traceId, intentResponse, event);
            String intent = intentRequest.getIntentResponse().getIntent();
            return (Promise)manager.fire("intent." + intent, intentRequest);
        }).done(response -> {
            // finally show typing off
            markTypingOff(executionContext, senderId);
        }).fail(ex -> {
            if (ex instanceof TriggerExecutionException)
                executionContext.fail((TriggerExecutionException) ex);
            else
                executionContext.fail(new TriggerExecutionException(ex));
            // finally show typing off
            markTypingOff(executionContext, senderId);
        });
    }

    private void markTypingOff(TriggerExecutionContext executionContext, String senderId) {
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.TYPING_OFF);
    }
}
