package com.Server.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "posts")
public class Post {
    @Id
    private String id;

    @DBRef
    private User user;

    private String content;

    private List<String> mediaUrls = new ArrayList<>();

    private int reportCount = 0;

    @Field(targetType = FieldType.STRING)
    private List<MediaType> mediaTypes = new ArrayList<>();

    @Field(targetType = FieldType.STRING)
    private Privacy privacy = Privacy.PUBLIC;

    @Field(targetType = FieldType.STRING)
    private User.Status status = User.Status.ACTIVE;

    @DBRef
    private List<User> likes = new ArrayList<>();

    private List<Comment> comments = new ArrayList<>();

    @DBRef
    private List<User> shares = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", content='" + content + '\'' +
                ", mediaUrls=" + mediaUrls + '\'' +
                ", mediaTypes=" + mediaTypes + '\'' +
                ", status='" + status + '\'' +
                ", likes=" + likes + '\'' +
                ", comments=" + comments + '\'' +
                ", shares=" + shares + '\'' +
                ", privacy=" + privacy + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public enum MediaType {
        IMAGE,
        VIDEO
    }

    public enum Privacy {
        PUBLIC,
        PRIVATE
    }
}