package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {
    public PostDTO() {
    }

    private String id;

    private UserDTO user;

    private String content;

    private String mediaUrl;

    private String status;
    
    private String mediaType;

    private int reportCount;

    private List<UserDTO> likes = new ArrayList<>();

    private List<CommentDTO> comments = new ArrayList<>();

    private int likeCount;

    private int commentCount;

    private List<UserDTO> share = new ArrayList<>();

    private int shareCount;

    private String privacy;

    private Instant createdAt;
    
    private Instant updatedAt;
}
