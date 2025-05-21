package com.Server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Server.dto.MessageDTO;
import com.Server.dto.Response;
import com.Server.service.api.MessageApi;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageApi messageApi;

    @PostMapping
    public ResponseEntity<Response> sendMessage(@RequestBody MessageDTO messageDTO) {
        Response response = messageApi.sendMessage(messageDTO);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/send-with-files")
    public ResponseEntity<Response> sendMessageWithFiles(
            @RequestParam("senderId") String senderId,
            @RequestParam("conversationId") String conversationId,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        Response response = messageApi.sendMessageWithFiles(senderId, conversationId, content, files);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/send-with-images")
    public ResponseEntity<Response> sendMessageWithImages(
            @RequestParam("senderId") String senderId,
            @RequestParam("conversationId") String conversationId,
            @RequestParam("content") String content,
            @RequestParam("images") MultipartFile[] images) {

        Response response = messageApi.sendMessageWithImages(senderId, conversationId, content, images);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-conversation/{user1Id}/{user2Id}")
    public ResponseEntity<Response> getConversation(
            @PathVariable String user1Id,
            @PathVariable String user2Id) {
        Response response = messageApi.getConversation(user1Id, user2Id);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-contacts/{userId}")
    public ResponseEntity<Response> getContacts(@PathVariable String userId) {
        Response response = messageApi.getContacts(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-latest-messages/{userId}")
    public ResponseEntity<Response> getLatestMessages(@PathVariable String userId) {
        Response response = messageApi.getLatestMessages(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-unread-messages/count/{userId}")
    public ResponseEntity<Response> countUnreadMessages(@PathVariable String userId) {
        Response response = messageApi.countUnreadMessages(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/{messageId}/mark-as-deleted")
    public ResponseEntity<Response> markMessageAsDeleted(
            @PathVariable String messageId,
            @RequestParam("userId") String userId) {

        Response response = messageApi.markMessageAsDeleted(messageId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}