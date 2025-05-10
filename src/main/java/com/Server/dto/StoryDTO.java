package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoryDTO {
    public StoryDTO() {
    }

    private String id;

    private UserDTO user;

    private String mediaUrl;

    private String mediaType;

    private String status;

    private int reportCount;

    private String privacy;
    
    private Instant createdAt;

    private Instant updatedAt;
}
