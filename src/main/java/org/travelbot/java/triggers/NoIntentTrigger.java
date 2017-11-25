package org.travelbot.java.triggers;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.dto.IntentResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.utils.MessengerUtils;

public class NoIntentTrigger extends AbstractTrigger<MessengerEvent, BaseResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        MessengerEvent request = (MessengerEvent) executionContext.getRequest();
        String response = "Sorry, I can't understand what you're saying";
        executionContext.finish(new IntentResponse(response));
        MessengerUtils.sendText(executionContext, request.getBaseEvent().senderId(), response);
    }
}
