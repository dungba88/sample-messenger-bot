package org.travelbot.java;

import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.di.ApplicationModuleInjector;

import com.github.messenger4j.Messenger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.Getter;

public class MessengerApplicationContext extends ApplicationContext {

    public final static String ACCESS_TOKEN = System.getenv("AccessToken");

    public final static String APP_SECRET = System.getenv("AppSecret");

    public final static String VERIFY_TOKEN = System.getenv("VerifyToken");

    public final static String PORT = System.getenv("PORT");

    private @Getter Messenger messenger;
    
    private @Getter Config config;

    public MessengerApplicationContext(ApplicationModuleInjector injector) {
        super(injector);
        messenger = Messenger.create(ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN);
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference());
    }

    public int getPort() {
        if (PORT != null && !PORT.isEmpty()) {
            return Integer.parseInt(PORT);
        }
        return 9090;
    }
}
