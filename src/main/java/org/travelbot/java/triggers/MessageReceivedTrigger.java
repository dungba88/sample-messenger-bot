package org.travelbot.java.triggers;

import java.util.Optional;

import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.Promise;
import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.TriggerManager;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.dto.IntentRequest;
import org.travelbot.java.dto.ParseIntentRequest;
import org.travelbot.java.dto.ParseIntentResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.dto.messenger.MessengerResponse;
import org.travelbot.java.support.utils.MessengerUtils;

import com.github.messenger4j.send.senderaction.SenderAction;

public class MessageReceivedTrigger extends AbstractTrigger<MessengerEvent, MessengerResponse> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        executionContext.finish(null);

        final MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        final TriggerManager manager = executionContext.getTriggerManager();
        final MessengerEvent event = (MessengerEvent) executionContext.getRequest();

        final String senderId = event.getOriginalEvent().senderId();
        final String text = event.getOriginalEvent().asTextMessageEvent().text();

        Optional<String> traceId = event.fetchRawTraceId();

        // always mark seen and show typing on
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.MARK_SEEN);
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.TYPING_ON);

        // call trigger to parse intent
        manager.fire(EventType.PARSE_INTENT.value(), new ParseIntentRequest(traceId, text, event))
                .fail(executionContext::fail)
                .pipeDone((PipeDoneCallback<BaseResponse, BaseResponse, Exception>) response -> {
                    if (response == null)
                        return (Promise) manager.fire(EventType.NO_INTENT.value(), event);

                    ParseIntentResponse intentResponse = (ParseIntentResponse) response;

                    // send user text about the intent
                    if (applicationContext.getConfig().getBoolean("log.send_intent"))
                        sendIntentToUser(executionContext, senderId, intentResponse);

                    // call trigger to handle intent
                    IntentRequest intentRequest = new IntentRequest(traceId, intentResponse, event);
                    String intent = intentRequest.getIntentResponse().getIntent();
                    return (Promise) manager.fire("intent." + intent, intentRequest);
                }).done(response -> markTypingOff(executionContext, senderId)).fail(ex -> {
                    if (ex instanceof TriggerExecutionException)
                        executionContext.fail((TriggerExecutionException) ex);
                    else
                        executionContext.fail(new TriggerExecutionException(ex));
                    // finally show typing off
                    markTypingOff(executionContext, senderId);
                });
    }

    private void sendIntentToUser(TriggerExecutionContext executionContext, final String senderId,
            ParseIntentResponse intentResponse) {
        String intentText = String.format("Recognize intent '%s' with confidence %d%%", intentResponse.getIntent(),
                (int) (intentResponse.getConfidence() * 100));
        MessengerUtils.sendText(executionContext, senderId, intentText);
    }

    private void markTypingOff(TriggerExecutionContext executionContext, String senderId) {
        MessengerUtils.sendAction(executionContext, senderId, SenderAction.TYPING_OFF);
    }
}
