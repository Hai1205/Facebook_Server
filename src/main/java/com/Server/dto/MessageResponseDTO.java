package com.Server.dto;

import com.Server.entity.Message.MessageType;
import com.Server.entity.Message.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private String id;

    private ConversationDTO conversation;

    private UserDTO sender;

    private UserDTO receiver;

    private String content;

    private MessageType type = MessageType.TEXT;

    private List<String> imageUrls = new ArrayList<>();

    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String mimeType;

    private MessageStatus status = MessageStatus.SENT;

    private Instant createdAt;

    private boolean IsRead;

    private List<ParticipantDTO> participants;
}