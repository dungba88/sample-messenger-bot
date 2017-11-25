package org.travelbot.java.triggers;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.dto.IntentResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.utils.MessengerUtils;

public class NoIntentTrigger extends AbstractTrigger<MessengerEvent, BaseResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        MessengerEvent request = (MessengerEvent) executionContext.getRequest();
        String response = applicationContext.getConfig().getString("reply.no_intent");
        executionContext.finish(new IntentResponse(response));
        MessengerUtils.sendText(executionContext, request.getBaseEvent().senderId(), response);
    }
}
