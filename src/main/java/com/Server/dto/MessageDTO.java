package com.Server.dto;

import com.Server.entity.Message.MessageType;
import com.Server.entity.Message.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id;

    private ConversationDTO conversation;

    private UserDTO sender;

    private String content;

    // Loại tin nhắn
    private MessageType type = MessageType.TEXT;

    // Danh sách URL hình ảnh
    private List<String> imageUrls = new ArrayList<>();

    // Thông tin file
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String mimeType;

    // Trạng thái tin nhắn
    private MessageStatus status = MessageStatus.SENT;
}