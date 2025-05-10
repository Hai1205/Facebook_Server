package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    public UserDTO() {
    }

    private String id;

    private String role;

    private String email;

    private String fullName;

    private String gender;

    private Date dateOfBirth;

    private String avatarPhotoUrl;

    private String coverPhotoUrl;

    private int reportCount;

    private List<UserDTO> followers = new ArrayList<>();

    private List<UserDTO> following = new ArrayList<>();
    
    private List<PostDTO> posts = new ArrayList<>();

    private List<StoryDTO> stories = new ArrayList<>();

    private List<UserDTO> friends = new ArrayList<>();

    private BioDTO bio;

    private String status;

    private boolean isCelebrity;

    private Instant createdAt;

    private Instant updatedAt;
}