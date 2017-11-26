package org.travelbot.java.support.logging;

import org.joo.scorpius.support.message.ExecutionContextFinishMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotatedExecutionContextFinishMessage extends AnnotatedGelfMessage {

    private static final long serialVersionUID = 3900326418162752886L;

    private ExecutionContextFinishMessage msg;

    public AnnotatedExecutionContextFinishMessage(ObjectMapper mapper, ExecutionContextFinishMessage msg, Long elapsed) {
        super();
        this.msg = msg;
        putField("executionContextId", msg.getId());
        putField("eventName", msg.getEventName());
        if (elapsed != null)
            putField("exec_elapsed", elapsed / 1000 + "us");
        if (msg.getRequest() != null) {
            putField("traceId", msg.getRequest().getTraceId());
        }
        try {
            putField("response", mapper.writeValueAsString(msg.getResponse()));
        } catch (JsonProcessingException e) {
            putField("responseEncodeException", e);
        }
    }

    @Override
    public String getFormattedMessage() {
        return "Finish handling event " + msg.getEventName() + " with id " + msg.getId();
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
