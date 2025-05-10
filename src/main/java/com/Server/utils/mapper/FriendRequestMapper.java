package com.Server.utils.mapper;

import com.Server.dto.FriendRequestDTO;
import com.Server.entity.FriendRequest;

import java.util.List;
import java.util.stream.Collectors;

public class FriendRequestMapper {
    public static FriendRequestDTO mapEntityToDTOFull(FriendRequest friendRequest) {
        FriendRequestDTO friendRequestDTO = mapEntityToDTO(friendRequest);
        if (friendRequest.getFrom() != null) {
            friendRequestDTO.setFrom(UserMapper.mapEntityToDTO(friendRequest.getFrom()));
        }
        if (friendRequest.getTo() != null) {
            friendRequestDTO.setTo(UserMapper.mapEntityToDTO(friendRequest.getTo()));
        }

        return friendRequestDTO;
    }

    public static List<FriendRequestDTO> mapListEntityToListDTOFull(List<FriendRequest> friendRequests) {
        return friendRequests.stream().map(FriendRequestMapper::mapEntityToDTOFull).collect(Collectors.toList());
    }

    public static FriendRequestDTO mapEntityToDTO(FriendRequest friendRequest) {
        FriendRequestDTO friendRequestDTO = new FriendRequestDTO();
        friendRequestDTO.setId(friendRequest.getId());
        friendRequestDTO.setStatus(friendRequest.getStatus().toString());
        friendRequestDTO.setCreatedAt(friendRequest.getCreatedAt());
        friendRequestDTO.setUpdatedAt(friendRequest.getUpdatedAt());

        return friendRequestDTO;
    }

    public static List<FriendRequestDTO> mapListEntityToListDTO(List<FriendRequest> friendRequests) {
        return friendRequests.stream().map(FriendRequestMapper::mapEntityToDTO).collect(Collectors.toList());
    }
}
