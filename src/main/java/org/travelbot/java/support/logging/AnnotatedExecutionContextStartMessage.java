package org.travelbot.java.support.logging;

import org.joo.scorpius.support.message.ExecutionContextStartMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotatedExecutionContextStartMessage extends AnnotatedGelfMessage {

    private static final long serialVersionUID = 3900326418162752886L;

    private ExecutionContextStartMessage msg;

    public AnnotatedExecutionContextStartMessage(ObjectMapper mapper, ExecutionContextStartMessage msg, Long latency) {
        super();
        this.msg = msg;
        putField("executionContextId", msg.getId());
        putField("eventName", msg.getEventName());
        if (latency != null)
            putField("exec_latency", latency / 1000 + "us");
        if (msg.getRequest() != null) {
            putField("traceId", msg.getRequest().getTraceId());
            try {
                putField("payload", mapper.writeValueAsString(msg.getRequest()));
            } catch (JsonProcessingException e) {
                putField("payloadEncodeException", e);
            }
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