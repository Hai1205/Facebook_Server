package com.Server.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;

    private String name;

    private Boolean isGroupChat = false;

    @DBRef
    private List<Participant> participants;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Long unreadCount;

    @Override
    public String toString() {
        return "Conversation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isGroupChat=" + isGroupChat +
                ", participants=" + participants +
                ", unreadCount=" + unreadCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}