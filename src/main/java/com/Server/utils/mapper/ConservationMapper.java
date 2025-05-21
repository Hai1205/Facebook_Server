package com.Server.utils.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.Server.dto.ConversationDTO;
import com.Server.entity.Conversation;

public class ConservationMapper {
    public static ConversationDTO mapEntityToDTOFull(Conversation conversation) {
        ConversationDTO participantDTO = mapEntityToDTO(conversation);

        if (conversation.getLastMessage() != null) {
            participantDTO.setLastMessage(MessageMapper.mapEntityToResponseDTOFull(conversation.getLastMessage()));
        } else {
            participantDTO.setLastMessage(null);
        }

        if (conversation.getParticipants() != null) {
            participantDTO
                    .setParticipants(ParticipantMapper.mapListEntityToListDTOFull(conversation.getParticipants()));
        } else {
            participantDTO.setParticipants(null);
        }

        return participantDTO;
    }

    public static List<ConversationDTO> mapListEntityToListDTOFull(List<Conversation> participants) {
        return participants.stream()
                .map(ConservationMapper::mapEntityToDTOFull)
                .collect(Collectors.toList());
    }

    public static ConversationDTO mapEntityToDTO(Conversation conversation) {
        ConversationDTO participantDTO = new ConversationDTO();
        participantDTO.setId(conversation.getId());
        participantDTO.setName(conversation.getName());
        participantDTO.setIsGroupChat(conversation.getIsGroupChat());
        participantDTO.setCreatedAt(conversation.getCreatedAt());
        participantDTO.setUpdatedAt(conversation.getUpdatedAt());
        participantDTO.setUnreadCount(conversation.getUnreadCount());

        return participantDTO;
    }

    public static List<ConversationDTO> mapListEntityToListDTO(List<Conversation> participants) {
        return participants.stream()
                .map(ConservationMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }
}
