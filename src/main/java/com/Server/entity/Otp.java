package com.Server.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "otps")
public class Otp {
    @Id
    private String id;

    @DBRef
    private User user;

    private String code;

    private Instant timeExpired;

    @CreatedDate
    private Instant createdAt;

    @Override
    public String toString() {
        return "Otp{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", code='" + code + '\'' +
                ", timeExpired=" + timeExpired + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}