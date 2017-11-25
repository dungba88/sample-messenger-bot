package org.travelbot.java.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.message.Message;

public abstract class AnnotatedGelfMessage implements Message {

    private static final long serialVersionUID = 7300513597474905162L;

    private final Map<String, Object> additionalFields = new HashMap<>();

    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public AnnotatedGelfMessage putField(String key, Object value) {
        additionalFields.put(key, value);
        return this;
    }
}
