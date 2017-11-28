package org.travelbot.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.joo.scorpius.support.builders.contracts.IdGenerator;
import org.joo.scorpius.support.builders.id.TimeBasedIdGenerator;
import org.joo.scorpius.support.vertx.VertxBootstrap;
import org.travelbot.java.controllers.MessengerChallengeController;
import org.travelbot.java.controllers.MessengerWebhookController;
import org.travelbot.java.dto.ErrorResponse;
import org.travelbot.java.support.exceptions.BadRequestException;
import org.travelbot.java.support.exceptions.UnauthorizedAccessException;
import org.travelbot.java.support.logging.AnnotatedGelfJsonAppender;
import org.travelbot.java.support.serializers.ConfigValueSerializer;
import org.travelbot.java.support.utils.AsyncTaskRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.messenger4j.Messenger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MessengerVertxBootstrap extends VertxBootstrap {

    static {
        PluginManager.addPackage(AnnotatedGelfJsonAppender.class.getPackage().getName());
    }

    private static final Logger logger = LogManager.getLogger(MessengerVertxBootstrap.class);

    public void run() {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        configureOverridens(msgApplicationContext);

        configureTriggers(msgApplicationContext);

        configureServer(msgApplicationContext);
    }

    private void configureServer(MessengerApplicationContext msgApplicationContext) {
        int eventLoopSize = msgApplicationContext.getConfig().getInt("executors.vertx_event_loop_size");
        super.configureServer(new VertxOptions().setEventLoopPoolSize(eventLoopSize), msgApplicationContext.getPort());
    }

    private void configureTriggers(MessengerApplicationContext msgApplicationContext) {
        new TriggerConfigurator(triggerManager, msgApplicationContext).configureTriggers();
    }

    private void configureOverridens(MessengerApplicationContext msgApplicationContext) {
        msgApplicationContext.override(IdGenerator.class, new TimeBasedIdGenerator());
        msgApplicationContext.override(Messenger.class, configureMessenger());
        msgApplicationContext.override(Config.class, configureConfiguration());
        msgApplicationContext.override(ObjectMapper.class, configureObjectMapper());
        msgApplicationContext.override(AsyncTaskRunner.class,
                new AsyncTaskRunner(msgApplicationContext.getConfig().getInt("executors.task_runner_threads")));
    }

    private Messenger configureMessenger() {
        return Messenger.create(MessengerApplicationContext.ACCESS_TOKEN, MessengerApplicationContext.APP_SECRET,
                MessengerApplicationContext.VERIFY_TOKEN);
    }

    private Config configureConfiguration() {
        Config config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference());
        return config;
    }

    private ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        final SimpleModule module = new SimpleModule();
        module.addSerializer(ConfigValue.class, new ConfigValueSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
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