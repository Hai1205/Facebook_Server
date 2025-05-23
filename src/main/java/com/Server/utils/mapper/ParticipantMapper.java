package com.Server.utils.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.Server.dto.ParticipantDTO;
import com.Server.entity.Participant;

public class ParticipantMapper {
    public static ParticipantDTO mapEntityToDTO(Participant participant) {
        ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setId(participant.getId());

        return participantDTO;
    }

    public static List<ParticipantDTO> mapListEntityToListDTO(List<Participant> participants) {
        return participants.stream()
                .map(ParticipantMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }
   
    public static ParticipantDTO mapEntityToDTOFull(Participant participant) {
        ParticipantDTO participantDTO = mapEntityToDTO(participant);

        if (participant.getUser() != null) {
            participantDTO.setUser(UserMapper.mapEntityToDTO(participant.getUser()));
        } else {
            participantDTO.setUser(null);
        }

        return participantDTO;
    }

    public static List<ParticipantDTO> mapListEntityToListDTOFull(List<Participant> participants) {
        return participants.stream()
                .map(ParticipantMapper::mapEntityToDTOFull)
                .collect(Collectors.toList());
    }
}
