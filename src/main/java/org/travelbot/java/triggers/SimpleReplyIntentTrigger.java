package org.travelbot.java.triggers;

import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.dto.IntentRequest;
import org.travelbot.java.dto.IntentResponse;
import org.travelbot.java.utils.MessengerUtils;

import com.typesafe.config.Config;

public class SimpleReplyIntentTrigger extends AbstractTrigger<IntentRequest, BaseResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext
                .getApplicationContext();
        IntentRequest request = (IntentRequest) executionContext.getRequest();
        String intent = request.getIntentResponse().getIntent();

        String path = getPath(applicationContext, intent);
        Config cfg = applicationContext.getConfig().getConfig(path);

        executionContext.finish(new IntentResponse(cfg.entrySet()));
        MessengerUtils.sendWithConfig(executionContext, request.getSenderId(), cfg);
    }

    private String getPath(MessengerApplicationContext applicationContext, String intent) {
        String path = "reply." + intent;
        if (applicationContext.getConfig().hasPath(path))
            return path;
        return "reply.notavailable";
    }
}
