package org.travelbot.java.triggers;

public enum EventType {

    FB_MSG_RECEIVED("fb_msg_received"),
    FB_REPLY_RECEIVED("fb_reply_received"),
    FB_ATTACHMENT_RECEIVED("fb_attachment_received"),
    PARSE_INTENT("parse_intent"),
    NO_INTENT("no_intent");
    
    private String value;

    private EventType(String value) {
        this.value = value;
    }
    
    public String value() {
        return value;
    }
    
    public String toString() {
        return value;
    }
}
