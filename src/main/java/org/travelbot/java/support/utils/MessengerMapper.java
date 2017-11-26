package org.travelbot.java.support.utils;

import java.util.Optional;

import com.github.messenger4j.send.message.quickreply.QuickReply;
import com.github.messenger4j.send.message.quickreply.TextQuickReply;
import com.typesafe.config.Config;

public final class MessengerMapper {

    public QuickReply mapConfigToQuickReply(Config cfg) {
        if (cfg.hasPath("text"))
            return mapConfigToTextQuickReply(cfg);
        return null;

    }

    private QuickReply mapConfigToTextQuickReply(Config cfg) {

        return TextQuickReply.create(cfg.getString("text"), cfg.getString("payload"), Optional.empty());
    }
}
