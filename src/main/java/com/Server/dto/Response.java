package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    public Response(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    private int statusCode;
    private String message;
    private Pagination pagination;

    @JsonIgnore
    private String token;

    private String role;
    private String expirationTime;
    private Long unRead;
    private String friendStatus;

    private UserDTO user;
    private PostDTO post;
    private BioDTO bio;
    private CommentDTO comment;
    private StoryDTO story;
    private ReportDTO report;
    private FriendRequestDTO friendRequest;
    private ConversationDTO conversation;
    private MessageResponseDTO messageResponse;

    private List<UserDTO> users;
    private List<PostDTO> posts;
    private List<StoryDTO> stories;
    private List<BioDTO> biographies;
    private List<CommentDTO> comments;
    private List<NotiDTO> notifications;
    private List<FriendRequestDTO> friendRequests;
    private List<ReportDTO> reports;
    private List<ConversationDTO> conversations;
    private List<MessageResponseDTO> messageResponses;

    private Map<String, Long> generalStat;

    private Map<String, Object> data;
}
