package org.travelbot.java.dto;

import org.joo.scorpius.support.BaseResponse;

import com.github.messenger4j.nlp.NlpEntity;

import lombok.Getter;

public class ParseIntentResponse extends BaseResponse {

    private static final long serialVersionUID = -2405093486571072528L;

    private final @Getter String intent;

    private final @Getter double confidence;

    private final @Getter NlpEntity[] intentEntities;

    public ParseIntentResponse(final String intent, final double confidence, final NlpEntity[] intentEntities) {
        this.intent = intent;
        this.confidence = confidence;
        this.intentEntities = intentEntities;
    }
}
