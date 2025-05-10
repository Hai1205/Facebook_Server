package com.Server.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallDataDTO {
    public CallDataDTO() {
    }

    public CallDataDTO(String userToCall, JsonNode signalData, String from, String name) {
        this.userToCall = userToCall;
        this.signalData = signalData;
        this.from = from;
        this.name = name;
    }

    public CallDataDTO(String userId, String callId) {
        this.userId = userId;
        this.callId = callId;
    }

    private String userToCall;

    private JsonNode signalData;

    private String from;

    private String name;

    private String userId;

    private String callId;
}
