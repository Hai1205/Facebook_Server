package com.Server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Server.dto.Response;
import com.Server.exception.OurException;
import com.Server.service.api.ChatApi;
import com.Server.handler.SocketIOHandler;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    @Autowired
    private ChatApi chatApi;

    @Autowired
    private SocketIOHandler socketIOHandler;

    @GetMapping("/get-or-create-conversation/{userId}/{otherUserId}")
    public ResponseEntity<Response> getOrCreateConversation(
            @PathVariable String userId,
            @PathVariable String otherUserId) {

        Response response = chatApi.getOrCreateConversation(userId, otherUserId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-online-users")
    public ResponseEntity<Response> getOnlineUsers() {
        Response response = new Response();

        try {
            List<String> onlineUsers = socketIOHandler.getOnlineUserIds();

            response.setStatusCode(200);
            response.setMessage("Lấy danh sách người dùng đang online thành công");

            // Đặt danh sách người dùng vào một trường mới của response
            Map<String, Object> data = Map.of("onlineUsers", onlineUsers);
            response.setData(data);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Lỗi khi lấy danh sách người dùng đang online: " + e.getMessage());
        }

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/create-conversation/{userId}/{otherUserId}")
    public ResponseEntity<Response> createConversation(
            @PathVariable String userId,
            @PathVariable String otherUserId) {

        if (userId == null || otherUserId == null) {
            throw new OurException("userId and otherUserId are required");
        }

        Response response = chatApi.getOrCreateConversation(userId, otherUserId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-conversations/{userId}")
    public ResponseEntity<Response> getUserConversations(
            @PathVariable String userId) {
        Response response = chatApi.getUserConversations(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-messages/{conversationId}/{userId}")
    public ResponseEntity<Response> getMessages(
            @PathVariable String conversationId,
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Response response = chatApi.getMessages(conversationId, userId, page, size);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-group-conversation")
    public ResponseEntity<Response> getGroupConversation() {
        Response response = chatApi.getGroupConversation();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-conversation/{conversationId}/{userId}")
    public ResponseEntity<Response> getConversation(
            @PathVariable String conversationId,
            @PathVariable String userId) {
        Response response = chatApi.getConversation(conversationId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-with-conversation/{userId}")
    public ResponseEntity<Response> getUsersWithConversation(@PathVariable String userId) {
        Response response = chatApi.getUsersWithConversation(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/create-group")
    public ResponseEntity<Response> createGroupConversation(@RequestBody Map<String, Object> requestBody) {
        String groupName = (String) requestBody.get("name");
        List<?> userIdsRaw = (List<?>) requestBody.get("userIds");
        List<String> userIds = userIdsRaw.stream()
                .map(id -> String.valueOf(id.toString()))
                .toList();

        Response response = chatApi.createGroupConversation(groupName, userIds);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/add-user-to-group/{conversationId}/{userId}")
    public ResponseEntity<Response> addUserToGroup(
            @PathVariable String conversationId,
            @PathVariable String userId) {
        Response response = chatApi.addUserToGroup(conversationId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-group/{conversationId}/{userId}")
    public ResponseEntity<Response> deleteUserFromGroup(@PathVariable String conversationId,
            @PathVariable String userId) {
        Response response = chatApi.deleteUserFromGroup(conversationId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}