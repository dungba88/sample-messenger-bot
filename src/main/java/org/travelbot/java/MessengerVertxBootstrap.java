package org.travelbot.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.dto.ErrorResponse;
import org.travelbot.java.support.exceptions.BadRequestException;
import org.travelbot.java.support.exceptions.UnauthorizedAccessException;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MessengerVertxBootstrap extends VertxBootstrap {

    private static final Logger logger = LogManager.getLogger(MessengerVertxBootstrap.class);

    public MessengerVertxBootstrap(final VertxOptions vertxOptions, final int port) {
        super(vertxOptions, new HttpServerOptions(), port);
    }

    public MessengerVertxBootstrap(final VertxOptions vertxOptions, final HttpServerOptions httpOptions,
            final int port) {
        super(vertxOptions, httpOptions, port);
    }

    public MessengerVertxBootstrap(final VertxOptions vertxOptions, final HttpServerOptions httpOptions, final int port,
            String endpoint) {
        super(vertxOptions, httpOptions, port, endpoint);
    }

    @Override
    protected Router configureRoutes(Vertx vertx) {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        Router router = super.configureRoutes(vertx);
        router.get("/fb_msg_hook")
                .handler(new MessengerChallengeController(msgApplicationContext.getMessenger())::handle);
        router.post("/fb_msg_hook")
                .handler(new MessengerWebhookController(triggerManager, msgApplicationContext.getMessenger())::handle);
        router.route().handler(this::handleDefault).failureHandler(this::handleFailure);
        return router;
    }

    private void handleDefault(RoutingContext rc) {
        if (!rc.response().ended())
            rc.response().end();
    }

    private void handleFailure(RoutingContext rc) {
        logger.error("Failure on handling request", rc.failure());

        int statusCode = extractStatusCodeFromFailure(rc);
        rc.response().setStatusCode(statusCode);
        String value = serialize(new ErrorResponse(rc.failure()));
        if (value != null)
            rc.response().end(value);
    }

    private int extractStatusCodeFromFailure(RoutingContext rc) {
        if (rc.statusCode() != -1)
            return rc.statusCode();
        if (rc.failure() instanceof UnauthorizedAccessException)
            return 403;
        if (rc.failure() instanceof BadRequestException)
            return 400;
        return 500;
    }

    private String serialize(Object obj) {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;
        try {
            return msgApplicationContext.getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        return null;
    }
}
