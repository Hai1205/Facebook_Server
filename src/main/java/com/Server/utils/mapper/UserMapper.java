package com.Server.utils.mapper;

import com.Server.dto.UserDTO;
import com.Server.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
        public static UserDTO mapEntityToDTOFull(User user) {
                UserDTO userDTO = mapEntityToDTO(user);

                if (user.getBio() != null) {
                        userDTO.setBio(BioMapper.mapEntityToDTO(user.getBio()));
                }

                if (user.getPosts() != null) {
                        userDTO.setPosts(PostMapper.mapListEntityToListDTOFull(user.getPosts()));
                }

                if (user.getStories() != null) {
                        userDTO.setStories(StoryMapper.mapListEntityToListDTO(user.getStories()));
                }

                if (user.getFriends() != null) {
                        userDTO.setFriends(mapListEntityToListDTO(user.getFriends()));
                }
               
                if (user.getFollowers() != null) {
                        userDTO.setFollowers(mapListEntityToListDTO(user.getFollowers()));
                }
               
                if (user.getFollowing() != null) {
                        userDTO.setFollowing(mapListEntityToListDTO(user.getFollowing()));
                }

                return userDTO;
        }

        public static UserDTO mapEntityToDTO(User user) {
                UserDTO userDTO = new UserDTO();

                userDTO.setId(user.getId());
                userDTO.setEmail(user.getEmail());
                userDTO.setFullName(user.getFullName());
                userDTO.setGender(user.getGender().toString());
                userDTO.setDateOfBirth(user.getDateOfBirth());
                userDTO.setAvatarPhotoUrl(user.getAvatarPhotoUrl());
                userDTO.setCoverPhotoUrl(user.getCoverPhotoUrl());
                userDTO.setRole(user.getRole().toString());
                userDTO.setStatus(user.getStatus().toString());
                userDTO.setReportCount(user.getReportCount());
                userDTO.setCelebrity(user.isCelebrity());
                userDTO.setUpdatedAt(user.getUpdatedAt());
                userDTO.setCreatedAt(user.getCreatedAt());

                return userDTO;
        }

        public static List<UserDTO> mapListEntityToListDTO(List<User> users) {
                return users.stream()
                                .map(UserMapper::mapEntityToDTO)
                                .collect(Collectors.toList());
        }

        public static List<UserDTO> mapListEntityToListDTOFull(List<User> users) {
                return users.stream()
                                .map(UserMapper::mapEntityToDTOFull)
                                .collect(Collectors.toList());
        }

        public static List<UserDTO> mapListEntityToListDTOLimit(List<User> users, int limit) {
                return users.stream()
                                .limit(limit)
                                .map(UserMapper::mapEntityToDTO)
                                .collect(Collectors.toList());
        }
}
