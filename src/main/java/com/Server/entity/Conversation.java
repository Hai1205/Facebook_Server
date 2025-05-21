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
    public Conversation() {
    }

    @Id
    private String id;

    private String name;

    private Boolean isGroupChat = false;

    @DBRef
    private List<Participant> participants;

    @DBRef
    private Message lastMessage;

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
                ", participants size=" + (participants != null ? participants.size() : 0) +
                ", lastMessage=" + (lastMessage != null ? lastMessage.getId() : null) +
                ", unreadCount=" + unreadCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}