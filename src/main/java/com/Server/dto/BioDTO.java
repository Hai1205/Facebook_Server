package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BioDTO {
    public BioDTO() {
    }

    private String id;

    private String bioText;

    private String liveIn;

    private String relationship;

    private String workplace;

    private String education;

    private String phone;

    private String hometown;

    private UserDTO user;

    private Instant createdAt;

    private Instant updatedAt;
}
