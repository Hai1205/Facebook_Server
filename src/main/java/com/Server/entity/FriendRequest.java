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
@Document(collection = "friend_requests")
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequest {
    public FriendRequest(User from, User to) {
        this.from = from;
        this.to = to;
    }

    @Id
    private String id;

    @DBRef
    private User from;

    @DBRef
    private User to;

    @Field(targetType = FieldType.STRING)
    private FriendRequest.STATUS status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "FriendRequest{" +
                "id='" + id + '\'' +
                ", from=" + from + '\'' +
                ", to=" + to + '\'' +
                ", status=" + status + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public enum STATUS {
        PENDING,
        REJECT,
        ACCEPT,
    }
}
