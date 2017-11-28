package org.travelbot.java.triggers;

import java.util.List;
import java.util.Map;

import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.dto.ParseIntentRequest;
import org.travelbot.java.dto.ParseIntentResponse;

import com.github.messenger4j.nlp.NlpEntity;
import com.github.messenger4j.webhook.event.BaseEvent;

public class ParseIntentTrigger extends AbstractTrigger<ParseIntentRequest, ParseIntentResponse> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
        ParseIntentRequest request = (ParseIntentRequest) executionContext.getRequest();
        if (request == null) {
            executionContext.finish(null);
            return;
        }
        BaseEvent event = request.getEvent().getBaseEvent();
        Map<String, List<NlpEntity>> entitiesMap = (Map) event.extendedProperties().get("entities");
        if (entitiesMap == null) {
            executionContext.finish(null);
            return;
        }
        List<NlpEntity> intentEntities = entitiesMap.get("intent");
        ParseIntentResponse response = parse(intentEntities);
        executionContext.finish(response);
    }

    private ParseIntentResponse parse(List<NlpEntity> intentEntities) {
        if (intentEntities == null || intentEntities.isEmpty())
            return null;
        NlpEntity[] intents = intentEntities.toArray(new NlpEntity[0]);

        NlpEntity mainIntent = null;
        for (NlpEntity intent : intents) {
            if (mainIntent == null || mainIntent.getConfidence() < intent.getConfidence())
                mainIntent = intent;
        }

        if (mainIntent == null)
            return null;

        return new ParseIntentResponse(mainIntent.getValue(), mainIntent.getConfidence(), intents);
    }
}
