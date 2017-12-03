package org.travelbot.java;

import java.util.List;

import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.joo.scorpius.Bootstrap;
import org.joo.scorpius.support.bootstrap.AbstractBootstrap;
import org.joo.scorpius.support.bootstrap.CompositionBootstrap;
import org.joo.scorpius.support.builders.contracts.IdGenerator;
import org.joo.scorpius.support.builders.id.TimeBasedIdGenerator;
import org.joo.scorpius.support.typesafe.TriggerTypeSafeBootstrap;
import org.joo.scorpius.support.typesafe.TypeSafeBootstrap;
import org.travelbot.java.support.logging.AnnotatedGelfJsonAppender;

import io.vertx.core.VertxOptions;

public class MessengerBootstrap extends CompositionBootstrap {

    static {
        PluginManager.addPackage(AnnotatedGelfJsonAppender.class.getPackage().getName());
    }

    protected void configureBootstraps(List<Bootstrap> bootstraps) {
        MessengerApplicationContext msgApplicationContext = (MessengerApplicationContext) applicationContext;

        bootstraps.add(AbstractBootstrap.from(() -> configureOverridens(msgApplicationContext)));
        bootstraps.add(new TypeSafeBootstrap());
        bootstraps.add(new TriggerTypeSafeBootstrap());
        bootstraps.add(new TriggerConfigurator());
        bootstraps.add(new MessengerVertxBootstrap(new VertxOptions().setEventLoopPoolSize(8),
                msgApplicationContext.getPort()));
    }

    private void configureOverridens(MessengerApplicationContext msgApplicationContext) {
        msgApplicationContext.override(IdGenerator.class, new TimeBasedIdGenerator());
    }
}