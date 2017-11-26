package org.travelbot.java.support.utils.payload;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.quickreply.QuickReply;
import com.typesafe.config.Config;

public class ConfigurableQuickRepliesPayloadFactory extends AbstractConfigurablePayloadFactory {

    public ConfigurableQuickRepliesPayloadFactory(Config cfg) {
        super(cfg);
    }

    @Override
    public Payload create(final String recipientId) {
        Optional<List<QuickReply>> quickReplies = Optional.empty();

        String text = cfg.getString("text");
        if (cfg.hasPath("quick_replies")) {
            quickReplies = createQuickRepliesFromConfig(cfg.getConfigList("quick_replies"));
        }
        return MessagePayload.create(recipientId, TextMessage.create(text, quickReplies, Optional.empty()));
    }

    private Optional<List<QuickReply>> createQuickRepliesFromConfig(List<? extends Config> configList) {
        if (configList.isEmpty())
            return Optional.empty();
        List<QuickReply> quickReplies = configList.stream().map(mapper::mapConfigToQuickReply)
                .collect(Collectors.toList());
        return Optional.of(quickReplies);
    }
}
