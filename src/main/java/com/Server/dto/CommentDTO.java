package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDTO {
    public CommentDTO() {
    }

    private String id;

    private UserDTO user;
    
    private String text;

    private String status;

    private int reportCount;

    private Instant createdAt;

    private Instant updatedAt;
}
