package com.Server.dto;

import com.Server.entity.Post;
import com.Server.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private String id;
    private UserDTO user;
    private String content;
    private List<String> mediaUrls = new ArrayList<>();
    private List<Post.MediaType> mediaTypes = new ArrayList<>();
    private Post.Privacy privacy;
    private User.Status status;
    private List<UserDTO> likes;
    private List<CommentDTO> comments;
    private List<UserDTO> share;
    private int reportCount;
    private int likeCount;
    private int commentCount;
    private int shareCount;
    private Instant createdAt;
    private Instant updatedAt;
}
