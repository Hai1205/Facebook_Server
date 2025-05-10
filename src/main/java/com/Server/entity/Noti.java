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

@Data
@Document(collection = "notifications")
public class Noti {
    public Noti(TYPE type, User from, User to) {
        this.type = type;
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
    private TYPE type;

    private boolean read = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "Noti{" +
                "id='" + id + '\'' +
                ", from=" + from + '\'' +
                ", to=" + to + '\'' +
                ", type=" + type + '\'' +
                ", read=" + read + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public enum TYPE {
        LIKE,
        FOLLOW,
        COMMENT,
    }
}
