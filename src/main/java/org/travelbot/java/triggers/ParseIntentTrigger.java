package org.travelbot.java.triggers;

import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.dto.ParseIntentRequest;
import org.travelbot.java.dto.ParseIntentResponse;

public class ParseIntentTrigger extends AbstractTrigger<ParseIntentRequest, ParseIntentResponse> {

    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        executionContext.finish(new ParseIntentResponse("greeting"));
    }
}
