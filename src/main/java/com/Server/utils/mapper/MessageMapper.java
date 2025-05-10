package com.Server.utils.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.Server.dto.MessageResponseDTO;
import com.Server.entity.Message;

public class MessageMapper {
    public static MessageResponseDTO mapEntityToResponseDTOFull(Message message) {
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setId(message.getId());
        messageResponseDTO.setConversationId(message.getConversation().getId());

        if (message.getSender() != null) {
            messageResponseDTO.setSender(UserMapper.mapEntityToDTO(message.getSender()));
        } else {
            messageResponseDTO.setSender(null);
        }

        messageResponseDTO.setContent(message.getContent());
        messageResponseDTO.setCreatedAt(message.getCreatedAt());
        messageResponseDTO.setIsRead(message.isRead());

        return messageResponseDTO;
    }

    public static List<MessageResponseDTO> mapListEntityToListDTOFull(List<Message> messages) {
        return messages.stream()
                .map(MessageMapper::mapEntityToResponseDTOFull)
                .collect(Collectors.toList());
    }
}