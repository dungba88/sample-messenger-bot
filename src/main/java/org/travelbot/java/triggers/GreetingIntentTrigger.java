package org.travelbot.java.triggers;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.dto.IntentRequest;
import org.travelbot.java.utils.MessengerUtils;

public class GreetingIntentTrigger extends AbstractTrigger<IntentRequest, BaseResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        IntentRequest request = (IntentRequest) executionContext.getRequest();
        MessengerUtils.sendText(executionContext, request.getSenderId(), "Hi, how can I help you?");
    }
}
