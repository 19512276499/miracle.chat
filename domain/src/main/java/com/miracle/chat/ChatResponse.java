package com.miracle.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatResponse {
    /**
     * 问题消耗tokens
     */
    @JsonProperty("question_tokens")
    private long questionTokens = 0;
}
