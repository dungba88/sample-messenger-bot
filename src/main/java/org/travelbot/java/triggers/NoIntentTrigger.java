package org.travelbot.java.triggers;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.dto.IntentResponse;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.utils.MessengerUtils;

import com.typesafe.config.Config;

public class NoIntentTrigger extends AbstractTrigger<MessengerEvent, BaseResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        MessengerEvent request = (MessengerEvent) executionContext.getRequest();
        Config cfg = applicationContext.getConfig().getConfig("reply.no_intent");
        executionContext.finish(new IntentResponse(cfg.entrySet()));
        MessengerUtils.sendWithConfig(executionContext, request.getBaseEvent().senderId(), cfg);
    }
}
