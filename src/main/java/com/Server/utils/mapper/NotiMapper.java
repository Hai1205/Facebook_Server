package com.Server.utils.mapper;

import com.Server.dto.NotiDTO;
import com.Server.entity.Noti;

import java.util.List;
import java.util.stream.Collectors;

public class NotiMapper {
    public static NotiDTO mapEntityToDTOFull(Noti noti) {
        NotiDTO notiDTO = mapEntityToDTO(noti);
        if (noti.getFrom() != null) {
            notiDTO.setFrom(UserMapper.mapEntityToDTO(noti.getFrom()));
        }
        if (noti.getTo() != null) {
            notiDTO.setTo(UserMapper.mapEntityToDTO(noti.getTo()));
        }
        if (noti.getPost() != null) {
            notiDTO.setPost(PostMapper.mapEntityToDTO(noti.getPost()));
        }

        return notiDTO;
    }

    public static List<NotiDTO> mapListEntityToListDTOFull(List<Noti> Notifications) {
        return Notifications.stream().map(NotiMapper::mapEntityToDTOFull).collect(Collectors.toList());
    }

    public static NotiDTO mapEntityToDTO(Noti noti) {
        NotiDTO notiDTO = new NotiDTO();
        notiDTO.setId(noti.getId());
        notiDTO.setType(noti.getType().toString());
        notiDTO.setRead(noti.isRead());
        notiDTO.setCreatedAt(noti.getCreatedAt());
        notiDTO.setUpdatedAt(noti.getUpdatedAt());

        return notiDTO;
    }

    public static List<NotiDTO> mapListEntityToListDTO(List<Noti> Notifications) {
        return Notifications.stream().map(NotiMapper::mapEntityToDTO).collect(Collectors.toList());
    }
}
