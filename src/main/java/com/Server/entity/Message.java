package com.Server.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    @DBRef
    private Conversation conversation;

    @DBRef
    private User sender;

    private String content;

    // Thêm các trường cho file và hình ảnh
    private MessageType type = MessageType.TEXT; // Loại tin nhắn mặc định là TEXT

    // Danh sách URL hình ảnh nếu là tin nhắn hình ảnh
    private List<String> imageUrls = new ArrayList<>();

    // URL file nếu là tin nhắn file
    private String fileUrl;

    // Tên file gốc nếu là tin nhắn file
    private String fileName;

    // Kích thước file tính bằng byte
    private Long fileSize;

    // Loại MIME của file
    private String mimeType;

    // Trạng thái tin nhắn - có thể hữu ích để đánh dấu tin nhắn đã xóa, v.v.
    private MessageStatus status = MessageStatus.SENT;

    private boolean isRead = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Enum cho loại tin nhắn
    public enum MessageType {
        TEXT, // Tin nhắn văn bản thông thường
        IMAGE, // Tin nhắn hình ảnh
        FILE, // Tin nhắn file
        AUDIO, // Tin nhắn âm thanh
        VIDEO, // Tin nhắn video
        LOCATION, // Tin nhắn vị trí
        STICKER // Tin nhắn sticker
    }

    // Enum cho trạng thái tin nhắn
    public enum MessageStatus {
        SENDING, // Đang gửi
        SENT, // Đã gửi
        DELIVERED, // Đã chuyển đến người nhận
        READ, // Đã đọc
        FAILED, // Gửi thất bại
        DELETED // Đã xóa
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", conversation=" + conversation +
                ", sender=" + sender +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}