package com.Server.utils.mapper;

import com.Server.dto.StoryDTO;
import com.Server.entity.Story;

import java.util.List;
import java.util.stream.Collectors;

public class StoryMapper {
    public static StoryDTO mapEntityToDTO(Story story) {
        StoryDTO storyDTO = new StoryDTO();

        storyDTO.setId(story.getId());
        storyDTO.setMediaUrl(story.getMediaUrl());
        storyDTO.setMediaType(story.getMediaType().toString());
        storyDTO.setStatus(story.getStatus().toString());
        storyDTO.setReportCount(story.getReportCount());
        storyDTO.setPrivacy(story.getPrivacy().toString());
        storyDTO.setCreatedAt(story.getCreatedAt());
        storyDTO.setUpdatedAt(story.getUpdatedAt());

        return storyDTO;
    }

    public static List<StoryDTO> mapListEntityToListDTO(List<Story> stories) {
        return stories.stream()
                .map(StoryMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    public static StoryDTO mapEntityToDTOFull(Story story) {
        StoryDTO storyDTO = mapEntityToDTO(story);

        if (story.getUser() != null) {
            storyDTO.setUser(UserMapper.mapEntityToDTO(story.getUser()));
        }

        return storyDTO;
    }

    public static List<StoryDTO> mapListEntityToListDTOFull(List<Story> stories) {
        return stories.stream()
                .map(StoryMapper::mapEntityToDTOFull)
                .collect(Collectors.toList());
    }
}
