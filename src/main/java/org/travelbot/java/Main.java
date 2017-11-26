package org.travelbot.java;

import org.joo.scorpius.Application;
import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.builders.ApplicationContextBuilder;
import org.travelbot.java.MessengerVertxBootstrap;

public class Main {

    public static void main(String[] args) {
        Application app = new Application(new ApplicationContextBuilder() {

            @Override
            public ApplicationContext build() {
                return new MessengerApplicationContext(getInjector());
            }
        });
        app.run(new MessengerVertxBootstrap());
    }
}
