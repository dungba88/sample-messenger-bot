package org.travelbot.java;

import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.di.ApplicationModuleInjector;
import org.travelbot.java.support.serializers.ConfigValueSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.messenger4j.Messenger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import lombok.Getter;

public class MessengerApplicationContext extends ApplicationContext {

    public static final String ACCESS_TOKEN = System.getenv("AccessToken");

    public static final String APP_SECRET = System.getenv("AppSecret");

    public static final String VERIFY_TOKEN = System.getenv("VerifyToken");

    public static final String PORT = System.getenv("PORT");

    private @Getter Messenger messenger;

    private @Getter Config config;

    private @Getter ObjectMapper objectMapper;

    public MessengerApplicationContext(ApplicationModuleInjector injector) {
        super(injector);
        this.messenger = Messenger.create(ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN);
        this.config = ConfigFactory.load();
        this.config.checkValid(ConfigFactory.defaultReference());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        final SimpleModule module = new SimpleModule();
        module.addSerializer(ConfigValue.class, new ConfigValueSerializer());
        this.objectMapper.registerModule(module);
    }

    public int getPort() {
        if (PORT != null && !PORT.isEmpty())
            return Integer.parseInt(PORT);
        return 9090;
    }
}
