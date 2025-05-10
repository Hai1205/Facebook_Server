package com.Server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

@Data
@Document(collection = "stories")
@AllArgsConstructor
@NoArgsConstructor
public class Story {
    @Id
    private String id;

    @DBRef
    private User user;

    private String mediaUrl;

    private int reportCount = 0;

    @Field(targetType = FieldType.STRING)
    private Post.MediaType mediaType;

    @Field(targetType = FieldType.STRING)
    private User.Status status = User.Status.ACTIVE;

    @Field(targetType = FieldType.STRING)
    private Post.Privacy privacy = Post.Privacy.PUBLIC;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "Story{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", mediaType=" + mediaType +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}