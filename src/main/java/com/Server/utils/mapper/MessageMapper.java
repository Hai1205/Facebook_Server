package com.Server.utils.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.Server.dto.MessageResponseDTO;
import com.Server.entity.Message;

public class MessageMapper {
    public static MessageResponseDTO mapEntityToResponseDTO(Message message) {
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setId(message.getId());
        messageResponseDTO.setContent(message.getContent());
        messageResponseDTO.setCreatedAt(message.getCreatedAt());
        messageResponseDTO.setIsRead(message.isRead());
        messageResponseDTO.setType(message.getType());
        messageResponseDTO.setStatus(message.getStatus());
        messageResponseDTO.setFileUrl(message.getFileUrl());
        messageResponseDTO.setFileName(message.getFileName());
        messageResponseDTO.setFileSize(message.getFileSize());
        messageResponseDTO.setMimeType(message.getMimeType());

        if (message.getImageUrls() != null) {
            messageResponseDTO.setImageUrls(message.getImageUrls());
        }

        return messageResponseDTO;
    }

    public static List<MessageResponseDTO> mapListEntityToListDTO(List<Message> messages) {
        return messages.stream()
                .map(MessageMapper::mapEntityToResponseDTO)
                .collect(Collectors.toList());
    }

    public static MessageResponseDTO mapEntityToResponseDTOFull(Message message) {
        MessageResponseDTO messageResponseDTO = mapEntityToResponseDTO(message);

        if (message.getSender() != null) {
            messageResponseDTO.setSender(UserMapper.mapEntityToDTO(message.getSender()));
        } else {
            messageResponseDTO.setSender(null);
        }

        if (message.getConversation() != null) {
            messageResponseDTO.setConversation(ConservationMapper.mapEntityToDTO(message.getConversation()));
        } else {
            messageResponseDTO.setConversation(null);
        }

        return messageResponseDTO;
    }

    public static List<MessageResponseDTO> mapListEntityToListDTOFull(List<Message> messages) {
        return messages.stream()
                .map(MessageMapper::mapEntityToResponseDTOFull)
                .collect(Collectors.toList());
    }
}