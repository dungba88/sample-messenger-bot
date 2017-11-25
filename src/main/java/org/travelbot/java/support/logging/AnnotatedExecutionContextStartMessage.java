package org.travelbot.java.support.logging;

import org.joo.scorpius.support.message.ExecutionContextStartMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotatedExecutionContextStartMessage extends AnnotatedGelfMessage {

    private static final long serialVersionUID = 3900326418162752886L;

    private static final ObjectMapper mapper = new ObjectMapper();

    private ExecutionContextStartMessage msg;

    public AnnotatedExecutionContextStartMessage(ExecutionContextStartMessage msg) {
        super();
        this.msg = msg;
        putField("executionContextId", msg.getId());
        putField("traceId", msg.getRequest().getTraceId());
        putField("eventName", msg.getEventName());
        try {
            putField("payload", mapper.writeValueAsString(msg.getRequest()));
        } catch (JsonProcessingException e) {
            putField("payloadEncodeException", e);
        }
    }

    @Override
    public String getFormattedMessage() {
        return "Start handling event " + msg.getEventName() + " with id " + msg.getId();
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }
}
