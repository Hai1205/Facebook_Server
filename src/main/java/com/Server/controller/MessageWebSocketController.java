package com.Server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.Server.dto.*;
import com.Server.repo.*;
import com.Server.service.api.MessageApi;
import com.Server.exception.OurException;
import com.Server.utils.OnlineUserTracker;
import com.Server.utils.mapper.UserMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MessageWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(MessageWebSocketController.class);

    @Autowired
    private MessageApi messageApi;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private OnlineUserTracker onlineUserTracker;

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String ping() {
        log.info("Received ping request");
        return "pong";
    }

    @MessageMapping("/user.connect")
    public void handleUserConnect(SimpMessageHeaderAccessor headerAccessor, Map<String, Object> userData) {
        String userId = (String) userData.get("userId");
        if (userId != null) {
            log.info("User connected via explicit message: {}", userId);

            // Lưu userId vào session để sử dụng trong các event listener
            if (headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("userId", userId);
            }

            // Thêm user vào danh sách online
            onlineUserTracker.addUser(userId);

            // Gửi thông báo cho tất cả người dùng
            Map<String, Object> statusUpdate = Map.of(
                    "userId", userId,
                    "online", true,
                    "timestamp", System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/user.status", statusUpdate);

            // Gửi danh sách người dùng đang online về cho client vừa kết nối
            Set<String> onlineUsers = onlineUserTracker.getOnlineUsers();
            messagingTemplate.convertAndSendToUser(headerAccessor.getSessionId(),
                    "/queue/online-users", onlineUsers);
        }
    }

    @MessageMapping("/user.disconnect")
    public void handleUserDisconnect(Map<String, Object> userData) {
        String userId = (String) userData.get("userId");
        if (userId != null) {
            processUserDisconnect(userId);
        }
    }

    private void processUserDisconnect(String userId) {
        log.info("User disconnected: {}", userId);

        // Xóa user khỏi danh sách online
        onlineUserTracker.removeUser(userId);

        // Gửi thông báo cho tất cả người dùng
        Map<String, Object> statusUpdate = Map.of(
                "userId", userId,
                "online", false,
                "timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/user.status", statusUpdate);
    }

    @MessageMapping("/chat.typing")
    public void handleTypingEvent(Map<String, Object> typingData) {
        try {
            String userId = (String) typingData.get("userId");
            String conversationId = (String) typingData.get("conversationId");
            Boolean isTypingObj = (Boolean) typingData.get("isTyping");

            if (userId == null || conversationId == null || isTypingObj == null) {
                log.error("Invalid typing data: userId={}, conversationId={}, isTyping={}",
                        userId, conversationId, isTypingObj);
                return;
            }

            boolean isTyping = isTypingObj.booleanValue();

            Map<String, Object> typingUpdate = Map.of(
                    "userId", userId,
                    "conversationId", conversationId,
                    "isTyping", isTyping);
            messagingTemplate.convertAndSend("/topic/conversation." + conversationId + ".typing", typingUpdate);

            log.debug("Typing status sent: user={}, conversation={}, isTyping={}",
                    userId, conversationId, isTyping);
        } catch (Exception e) {
            log.error("Error processing typing event: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversation.{conversationId}")
    public MessageResponseDTO forwardMessage(@DestinationVariable String conversationId, MessageDTO messageDTO) {
        try {
            if (messageDTO == null || messageDTO.getSender() == null ||
                    messageDTO.getSender().getId() == null ||
                    messageDTO.getConversation() == null) {
                log.error("Invalid message data for conversation: {}", conversationId);
                throw new OurException("Invalid message data");
            }

            messageDTO.getConversation().setId(conversationId);

            participantRepository
                    .findByConversationIdAndUserId(messageDTO.getConversation().getId(),
                            messageDTO.getSender().getId())
                    .orElseThrow(() -> new OurException("User is not in conversation"));

            // Lưu tin nhắn vào cơ sở dữ liệu
            Response response = messageApi.sendMessage(messageDTO);
            MessageResponseDTO storedMessage = response.getMessageResponse();

            if (storedMessage != null) {
                log.debug("Message stored successfully: id={}, conversation={}",
                        storedMessage.getId(), conversationId);
                return storedMessage;
            }

            // Nếu không lưu được, vẫn trả về message để hiển thị
            MessageResponseDTO responseDTO = new MessageResponseDTO();
            responseDTO.setConversationId(conversationId);
            responseDTO.setSender(messageDTO.getSender());
            responseDTO.setContent(messageDTO.getContent());
            responseDTO.setIsRead(false);

            List<ParticipantDTO> participants = participantRepository.findByConversationId(conversationId)
                    .stream()
                    .map(p -> {
                        ParticipantDTO participantDTO = new ParticipantDTO();
                        participantDTO.setId(p.getId());
                        participantDTO.setUser(UserMapper.mapEntityToDTO(p.getUser()));
                        return participantDTO;
                    })
                    .collect(Collectors.toList());

            responseDTO.setParticipants(participants);

            log.debug("Returning temporary message for conversation: {}", conversationId);
            return responseDTO;
        } catch (Exception e) {
            log.error("Error forwarding message: {}", e.getMessage(), e);
            throw e;
        }
    }

    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId, String userId) {
        try {
            if (conversationId == null || userId == null || conversationId.trim().isEmpty()
                    || userId.trim().isEmpty()) {
                log.error("Invalid parameters for marking messages as read: conversationId={}, userId={}",
                        conversationId, userId);
                return;
            }

            log.debug("Marking messages as read: conversation={}, user={}", conversationId, userId);
            messageApi.markMessagesAsRead(conversationId, userId);

            // Gửi thông báo đã đọc đến tất cả người dùng trong cuộc trò chuyện
            Map<String, Object> readUpdate = Map.of(
                    "conversationId", conversationId,
                    "userId", userId,
                    "timestamp", System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/conversation." + conversationId + ".read", readUpdate);
        } catch (OurException e) {
            log.error("Error marking messages as read: {}", e.getMessage());
            messagingTemplate.convertAndSend(
                    "/topic/errors." + userId,
                    e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error marking messages as read: {}", e.getMessage(), e);
            messagingTemplate.convertAndSend(
                    "/topic/errors." + userId,
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    @MessageMapping("/get-online-users")
    public void getOnlineUsers(SimpMessageHeaderAccessor headerAccessor) {
        Set<String> onlineUsers = onlineUserTracker.getOnlineUsers();
        messagingTemplate.convertAndSendToUser(headerAccessor.getSessionId(),
                "/queue/online-users", onlineUsers);
    }
}
