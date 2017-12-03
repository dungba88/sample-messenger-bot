package org.travelbot.java;

import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.di.ApplicationModuleInjector;
import org.travelbot.java.support.utils.AsyncTaskRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.messenger4j.Messenger;
import com.typesafe.config.Config;

import lombok.Getter;

public class MessengerApplicationContext extends ApplicationContext {

    public static final String ACCESS_TOKEN = System.getenv("AccessToken");

    public static final String APP_SECRET = System.getenv("AppSecret");

    public static final String VERIFY_TOKEN = System.getenv("VerifyToken");

    public static final String PORT = System.getenv("PORT");

    private @Getter Messenger messenger;

    private @Getter Config config;

    private @Getter ObjectMapper objectMapper;

    private @Getter AsyncTaskRunner taskRunner;

    public MessengerApplicationContext(ApplicationModuleInjector injector) {
        super(injector);
    }

    @Override
    protected void refreshCachedProperties() {
        super.refreshCachedProperties();
        this.messenger = getInstance(Messenger.class);
        this.config = getInstance(Config.class);
        this.objectMapper = getInstance(ObjectMapper.class);
        this.taskRunner = getInstance(AsyncTaskRunner.class);
    }

    public int getPort() {
        if (PORT != null && !PORT.isEmpty())
            return Integer.parseInt(PORT);
        return 9090;
    }
}
