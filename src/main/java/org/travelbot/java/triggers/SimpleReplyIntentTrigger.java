package org.travelbot.java.triggers;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.dto.IntentRequest;
import org.travelbot.java.dto.IntentResponse;
import org.travelbot.java.utils.MessengerUtils;

public class SimpleReplyIntentTrigger extends AbstractTrigger<IntentRequest, BaseResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        IntentRequest request = (IntentRequest) executionContext.getRequest();
        String intent = request.getIntentResponse().getIntent();
        String response = "";
        
        if (intent.equals("greeting"))
            response = "Hi, how can I help you?";
        else if (intent.equals("inquire_name"))
            response = "My name is Sophie";
        else if (intent.equals("inquire_personal"))
            response = "I'm your travel buddy";
        executionContext.finish(new IntentResponse(response));
        MessengerUtils.sendText(executionContext, request.getSenderId(), response);
    }
}
