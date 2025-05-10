package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpDTO {
    public OtpDTO() {
    }

    private UserDTO user;

    private String id;

    private String code;

    private Instant timeExpired;

    private Instant createdAt;

    private Instant updatedAt;
}
