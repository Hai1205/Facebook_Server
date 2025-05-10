package com.Server.dto;

import lombok.Data;

@Data
public class Request {
    // Auth
    private String email;

    private String username;

    private String password;

    private String otp;

    private String oldPassword;

    private String newPassword;

    private String rePassword;

    // Post
    private String postId;

    private String userId;

    private String text;

    // Bio
    private String bioText;

    private String liveIn;

    private String relationship;

    private String workplace;

    private String education;

    private String phone;

    private String hometown;

    // User
    private String gender;

    private String avatarUrl;

    private String fullName;

    private String dateOfBirth;

    private String bio;

    private String link;

    // Chat
    private String chatId;

    private String senderId;

    private String receiverId;

    private String content;
}
