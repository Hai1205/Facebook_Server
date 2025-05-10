package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDTO {
    private String id;

    private UserDTO sender;

    private String reason;

    private String contentId;

    private String contentType;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}
