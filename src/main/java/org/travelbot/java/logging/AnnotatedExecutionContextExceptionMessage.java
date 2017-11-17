package org.travelbot.java.logging;

import org.joo.scorpius.support.message.ExecutionContextExceptionMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotatedExecutionContextExceptionMessage extends AnnotatedGelfMessage {

	private static final long serialVersionUID = 3900326418162752886L;
	
	private static final ObjectMapper mapper = new ObjectMapper();

	private ExecutionContextExceptionMessage msg;

	public AnnotatedExecutionContextExceptionMessage(ExecutionContextExceptionMessage msg) {
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
		return "Exception occurred when handling event " + msg.getEventName() + " with id " + msg.getId();
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
		return msg.getCause();
	}
}
