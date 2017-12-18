package org.travelbot.java.support.payload;

import com.typesafe.config.Config;

public final class PayloadAbstractFactory {

    private PayloadAbstractFactory() {

    }

    public static PayloadFactory createFactory(Config cfg) {
        if (cfg.hasPath("text"))
            return new ConfigurableQuickRepliesPayloadFactory(cfg);
        return null;
    }
}
