package org.travelbot.java.triggers;

import org.joo.scorpius.support.exception.TriggerExecutionException;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.joo.scorpius.trigger.impl.AbstractTrigger;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.dto.messenger.MessengerResponse;

import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.SenderActionPayload;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.senderaction.SenderAction;

public class MessageReceivedTrigger extends AbstractTrigger<MessengerEvent, MessengerResponse> {

	@Override
	public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
		executionContext.finish(null);
		MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext.getApplicationContext();
		MessengerEvent event = (MessengerEvent) executionContext.getRequest();
		
		final String recipientId = event.getOriginalEvent().senderId();
		final String text = event.getOriginalEvent().asTextMessageEvent().text();
		
		long start = System.currentTimeMillis();
		try {
			sendAction(applicationContext, recipientId, SenderAction.MARK_SEEN);
			sendAction(applicationContext, recipientId, SenderAction.TYPING_ON);
			sendText(applicationContext, recipientId, text);
		} catch (MessengerApiException | MessengerIOException e) {
			throw new TriggerExecutionException(e);
		}
		System.out.println("Inner :" + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void sendAction(MessengerApplicationContext applicationContext, String recipientId, SenderAction senderAction) throws MessengerApiException, MessengerIOException {
		final SenderActionPayload payload = SenderActionPayload.create(recipientId, senderAction);
		applicationContext.getMessenger().send(payload);
	}

	private void sendText(MessengerApplicationContext applicationContext, String recipientId, String text) throws MessengerApiException, MessengerIOException {
		final Payload payload = MessagePayload.create(recipientId, TextMessage.create(text));
		applicationContext.getMessenger().send(payload);
	}
}
