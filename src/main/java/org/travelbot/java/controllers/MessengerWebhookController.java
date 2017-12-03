package org.travelbot.java.controllers;

import java.util.Optional;

import org.joo.scorpius.support.CommonConstants;
import org.joo.scorpius.support.message.CustomMessage;
import org.joo.scorpius.support.vertx.VertxMessageController;
import org.joo.scorpius.trigger.TriggerEvent;
import org.joo.scorpius.trigger.TriggerManager;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.support.exceptions.BadRequestException;
import org.travelbot.java.support.exceptions.UnauthorizedAccessException;
import org.travelbot.java.support.logging.HttpRequestMessage;
import org.travelbot.java.triggers.EventType;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerVerificationException;
import com.github.messenger4j.webhook.Event;
import com.google.gson.JsonSyntaxException;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class MessengerWebhookController extends VertxMessageController {

    private Messenger messenger;

    public MessengerWebhookController(TriggerManager manager, Messenger messenger) {
        super(manager);
        this.messenger = messenger;
    }

    @Override
    public void handle(RoutingContext rc) {
        HttpServerResponse response = rc.response();
        response.putHeader("Content-Type", "application/json");

        Optional<String> traceId = getTraceId(rc, triggerManager.getApplicationContext());
        if (traceId != null && traceId.isPresent()) {
            rc.request().headers().set(CommonConstants.TRACE_ID_HEADER, traceId.get());
        }

        if (triggerManager.isEventEnabled(TriggerEvent.CUSTOM))
            triggerManager.notifyEvent(TriggerEvent.CUSTOM,
                    new CustomMessage("start_request", new HttpRequestMessage(rc)));

        String payload = rc.getBodyAsString();
        if (payload == null || payload.isEmpty())
            throw new BadRequestException("payload is null");

        String signature = rc.request().getHeader("X-Hub-Signature");

        if (signature == null)
            throw new UnauthorizedAccessException("signature cannot be null");

        try {
            messenger.onReceiveEvents(payload, Optional.empty(), event -> handleEvent(rc, event, traceId));
        } catch (MessengerVerificationException ex) {
            rc.fail(ex);
        } catch (JsonSyntaxException ex) {
            throw new BadRequestException(ex);
        }
    }

    private void handleEvent(RoutingContext rc, Event event, Optional<String> traceId) {
        String eventName = getEventNameForMessengerEvent(event);
        if (eventName == null) {
            rc.response().end();
            return;
        }

        MessengerEvent msgEvent = new MessengerEvent(event);
        msgEvent.attachTraceId(traceId);

        triggerManager.fire(eventName, msgEvent, triggerResponse -> rc.next(),
                exception -> onFail(exception, rc.response(), rc));
    }

    private String getEventNameForMessengerEvent(Event event) {
        if (event.isTextMessageEvent())
            return EventType.FB_MSG_RECEIVED.value();
        if (event.isQuickReplyMessageEvent())
            return EventType.FB_REPLY_RECEIVED.value();
        if (event.isAttachmentMessageEvent())
            return EventType.FB_ATTACHMENT_RECEIVED.value();
        return null;
    }
}
