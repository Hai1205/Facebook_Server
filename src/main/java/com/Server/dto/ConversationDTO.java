package com.Server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String id;

    private String name;

    private Boolean isGroupChat;

    private Instant createdAt;

    private Instant updatedAt;

    private List<ParticipantDTO> participants;

    private MessageResponseDTO lastMessage;

    private Long unreadCount;
}