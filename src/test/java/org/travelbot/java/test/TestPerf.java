package org.travelbot.java.test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.builders.ApplicationContextBuilder;
import org.joo.scorpius.support.builders.contracts.IdGenerator;
import org.joo.scorpius.support.builders.id.TimeBasedIdGenerator;
import org.joo.scorpius.trigger.TriggerManager;
import org.joo.scorpius.trigger.impl.DefaultTriggerManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.TriggerConfigurator;
import org.travelbot.java.dto.messenger.MessengerEvent;
import org.travelbot.java.support.logging.AnnotatedGelfJsonAppender;

import com.github.messenger4j.nlp.NlpEntity;
import com.github.messenger4j.webhook.Event;
import com.github.messenger4j.webhook.event.BaseEvent;
import com.github.messenger4j.webhook.event.TextMessageEvent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@RunWith(Parameterized.class)
public class TestPerf {

    static {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        PluginManager.addPackage(AnnotatedGelfJsonAppender.class.getPackage().getName());
    }

    private int iterations = 10000;

    private BaseEvent baseEvent;

    public TestPerf(BaseEvent baseEvent) {
        this.baseEvent = baseEvent;
    }

    @Test
    public void test() {
        MessengerApplicationContext applicationContext = (MessengerApplicationContext) new ApplicationContextBuilder() {
            
            public ApplicationContext build() {
                return new MessengerApplicationContext(getInjector());
            }
        }.build();

        applicationContext.override(IdGenerator.class, new TimeBasedIdGenerator());
        applicationContext.override(Config.class, ConfigFactory.load());
        
        TriggerManager manager = new DefaultTriggerManager(applicationContext);

        new TriggerConfigurator(manager, applicationContext).configureTriggers();

        MessengerEvent event = new MessengerEvent(new Event(baseEvent));
        event.attachTraceId(Optional.empty());

        for (int i = 0; i < iterations; i++) {
            manager.fire("fb_msg_received", event);
        }

        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger counter = new AtomicInteger(0);

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.fire("fb_msg_received", event, response -> {
                if (counter.incrementAndGet() == iterations)
                    latch.countDown();
            }, ex -> {
                ex.printStackTrace();
                if (counter.incrementAndGet() == iterations)
                    latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long elapsed = System.nanoTime() - start;
        long pace = iterations * 1000000000L / elapsed;

        System.out.println("Total (ms): " + (elapsed / 1000000) + "ms");
        System.out.println("Average (us): " + (elapsed / iterations / 1000) + "us");
        System.out.println("Pace: " + pace + " ops/sec");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manager.shutdown();
    }

    private static TextMessageEvent mockMessageEventNoNlp() {
        TextMessageEvent event = new TextMessageEvent("", "", Instant.now(), "", "Hello");
        return event;
    }

    private static TextMessageEvent mockMessageEventWithNlp() {
        TextMessageEvent event = mockMessageEventNoNlp();
        event.extendedProperties().put("entities", mockEntities());
        return event;
    }

    private static Map<String, List<NlpEntity>> mockEntities() {
        Map<String, List<NlpEntity>> map = new HashMap<>();
        map.put("intent", new ArrayList<>(Arrays.asList(new NlpEntity[] { new NlpEntity("intent", "greeting", 1) })));
        return map;
    }

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<>();
        list.add(new Object[] { mockMessageEventNoNlp() });
        list.add(new Object[] { mockMessageEventWithNlp() });
        return list;
    }
}
