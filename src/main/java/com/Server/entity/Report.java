package com.Server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

@Data
@Document(collection = "reports")
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Id
    private String id;

    private User sender;

    private String reason;

    @Field(targetType = FieldType.STRING)
    private ContentType contentType;

    @Field(targetType = FieldType.STRING)
    private Status status = Status.PENDING;

    private String contentId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", sender=" + sender +
                ", reason='" + reason + '\'' +
                ", contentType=" + contentType + '\'' +
                ", contentId=" + contentId + '\'' +
                ", status=" + status + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public enum ContentType {
        USER,
        POST,
        COMMENT,
        STORY
    }
    
    public enum Status {
        PENDING,
        ACCEPT,
        REJECT
    }
}
