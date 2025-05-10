package com.Server.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "participants")
public class Participant {
    @Id
    private String id;

    @DBRef
    private Conversation conversation;

    @DBRef
    private User user;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant joinedAt;

    private Instant leftAt;

    @Override
    public String toString() {
        return "ConversationParticipant{" +
                "id='" + id + '\'' +
                ", conversation=" + conversation +
                ", user=" + user +
                ", joinedAt=" + joinedAt +
                ", leftAt=" + leftAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}