package com.Server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private String id;

    private String conversationId;

    private UserDTO sender;

    private UserDTO receiver;

    private String content;

    private Instant createdAt;

    private boolean IsRead;

    private List<ParticipantDTO> participants;
}