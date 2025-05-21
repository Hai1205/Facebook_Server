package com.Server.service.instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private OnlineUserTracker onlineUserTracker;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        if (event == null || event.getMessage() == null) {
            log.warn("Received null SessionConnectedEvent or message");
            return;
        }

        try {
            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
            String userId = getUserIdFromHeaders(headers);

            if (userId != null) {
                log.info("User connected: {}", userId);
                onlineUserTracker.addUser(userId);
                broadcastUserStatus(userId, true);

                // updateUserOnlineStatus(userId, true);
            } else {
                log.debug("Connected user without userId in session attributes");
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket connection event", e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event == null || event.getMessage() == null) {
            log.warn("Received null SessionDisconnectEvent or message");
            return;
        }

        try {
            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
            String userId = getUserIdFromHeaders(headers);

            if (userId != null) {
                log.info("User disconnected: {}", userId);
                onlineUserTracker.removeUser(userId);
                broadcastUserStatus(userId, false);

                // updateUserOnlineStatus(userId, false);
            } else {
                log.debug("Disconnected user without userId in session attributes");
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnection event", e);
        }
    }

    private String getUserIdFromHeaders(SimpMessageHeaderAccessor headers) {
        if (headers != null && headers.getSessionAttributes() != null) {
            Object userIdObj = headers.getSessionAttributes().get("userId");
            if (userIdObj != null) {
                return userIdObj.toString();
            }
        }
        return null;
    }

    private void broadcastUserStatus(String userId, boolean online) {
        try {
            Map<String, Object> statusUpdate = Map.of(
                    "userId", userId,
                    "online", online,
                    "timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/user.status", statusUpdate);
            log.debug("Broadcasted status for user {}: online={}", userId, online);
        } catch (Exception e) {
            log.error("Error broadcasting user status", e);
        }
    }

    // private void updateUserOnlineStatus(String userId, boolean online) {
    //     try {
    //         Optional<User> userOpt = userRepository.findById(userId);
    //         if (userOpt.isPresent()) {
    //         }
    //     } catch (Exception e) {
    //         log.error("Error updating user online status in database", e);
    //     }
    // }
}