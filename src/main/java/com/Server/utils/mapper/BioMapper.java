package com.Server.utils.mapper;

import com.Server.dto.BioDTO;
import com.Server.entity.Bio;

import java.util.List;
import java.util.stream.Collectors;

public class BioMapper {
    public static BioDTO mapEntityToDTOFull(Bio bio) {
        BioDTO bioDTO = mapEntityToDTO(bio);

        if (bio.getUser() != null) {
            bioDTO.setUser(UserMapper.mapEntityToDTO(bio.getUser()));
        }

        return bioDTO;
    }

    public static BioDTO mapEntityToDTO(Bio bio) {
        BioDTO bioDTO = new BioDTO();

        bioDTO.setId(bio.getId());
        bioDTO.setBioText(bio.getBioText());
        bioDTO.setLiveIn(bio.getLiveIn());
        bioDTO.setRelationship(bio.getRelationship());
        bioDTO.setWorkplace(bio.getWorkplace());
        bioDTO.setEducation(bio.getEducation());
        bioDTO.setPhone(bio.getPhone());
        bioDTO.setHometown(bio.getHometown());
        bioDTO.setCreatedAt(bio.getCreatedAt());
        bioDTO.setUpdatedAt(bio.getUpdatedAt());

        return bioDTO;
    }

    public static List<BioDTO> mapListEntityToListDTO(List<Bio> biographies) {
        return biographies.stream()
                .map(BioMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public static List<BioDTO> mapListEntityToListDTOFull(List<Bio> biographies) {
        return biographies.stream()
                .map(BioMapper::mapEntityToDTOFull)
                .collect(Collectors.toList());
    }
}
