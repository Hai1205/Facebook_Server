package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotiDTO {
    private String id;

    private UserDTO from;

    private UserDTO to;

    private PostDTO post;

    private String type;

    private boolean read;

    private Instant createdAt;

    private Instant updatedAt;
}
