package org.travelbot.java.support.payload;

import org.travelbot.java.utils.MessengerMapper;

import com.typesafe.config.Config;

public abstract class AbstractConfigurablePayloadFactory implements PayloadFactory {

    protected final Config cfg;

    protected final MessengerMapper mapper;

    public AbstractConfigurablePayloadFactory(Config cfg) {
        this.cfg = cfg;
        this.mapper = new MessengerMapper();
    }
}
