package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantDTO {
    public ParticipantDTO() {
    }

    public ParticipantDTO(UserDTO user) {
        this.user = user;
    }

    private String id;

    private UserDTO user;
}