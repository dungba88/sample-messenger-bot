package org.travelbot.java.support.logging;

import org.joo.scorpius.support.CommonConstants;
import org.joo.scorpius.support.graylog.msg.AnnotatedGelfMessage;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class HttpRequestMessage extends AnnotatedGelfMessage {

    private static final long serialVersionUID = -8893470932483746453L;

    private transient HttpServerRequest request;

    public HttpRequestMessage(RoutingContext rc) {
        super();
        this.request = rc.request();
        putField("uri", rc.request().absoluteURI());
        putField("method", rc.request().method().toString());
        putField("traceId", rc.request().getHeader(CommonConstants.TRACE_ID_HEADER));
        putField("params", rc.request().params().entries());
        putField("headers", rc.request().headers().entries());
        putField("host", rc.request().host());
        putField("localAddress", rc.request().localAddress());
        putField("payload", rc.getBodyAsString());
    }

    @Override
    public String getFormattedMessage() {
        return "Starting request " + request.absoluteURI();
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

}
