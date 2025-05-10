package com.Server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "bios")
@AllArgsConstructor
@NoArgsConstructor
public class Bio {
    @Id
    private String id;

    private String bioText;

    private String liveIn;

    private String relationship;

    private String workplace;

    private String education;

    private String phone;

    private String hometown;

    @DBRef
    private User user;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "Bio{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", bioText='" + bioText + '\'' +
                ", liveIn='" + liveIn + '\'' +
                ", relationship='" + relationship + '\'' +
                ", workplace='" + workplace + '\'' +
                ", education='" + education + '\'' +
                ", phone='" + phone + '\'' +
                ", hometown='" + hometown + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
