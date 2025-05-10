package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendRequestDTO {
    private String id;

    private UserDTO from;

    private UserDTO to;

    private String status;

    private boolean read;

    private Instant createdAt;

    private Instant updatedAt;
}
