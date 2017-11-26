package org.travelbot.java.dto;

import java.util.Map.Entry;
import java.util.Set;

import org.joo.scorpius.support.BaseResponse;

import com.typesafe.config.ConfigValue;

import lombok.Getter;

public class IntentResponse extends BaseResponse {

    private static final long serialVersionUID = 2395351462910462066L;

    private final transient @Getter Set<Entry<String, ConfigValue>> response;

    public IntentResponse(Set<Entry<String, ConfigValue>> configSet) {
        this.response = configSet;
    }
}
