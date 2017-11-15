package org.travelbot.java.triggers;

import org.joo.scorpius.support.TriggerExecutionException;
import org.joo.scorpius.trigger.AbstractTrigger;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.travelbot.java.dto.MessengerEvent;
import org.travelbot.java.dto.MessengerResponse;

public class MessageReceivedTrigger extends AbstractTrigger<MessengerEvent, MessengerResponse> {

	@Override
	public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
		executionContext.finish(new MessengerResponse());
	}
}
