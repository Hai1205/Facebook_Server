package com.Server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.Server.dto.*;
import com.Server.repo.*;
import com.Server.service.api.MessageApi;
import com.Server.exception.OurException;
import com.Server.utils.mapper.UserMapper;

import java.util.List;
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

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String ping() {
        log.info("Received ping request");
        return "pong";
    }

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversation.{conversationId}")
    public MessageResponseDTO forwardMessage(@DestinationVariable String conversationId, MessageDTO messageDTO) {
        try {
            messageDTO.getConversation().setId(conversationId);

            participantRepository
                    .findByConversationIdAndUserId(messageDTO.getConversation().getId(),
                            messageDTO.getSender().getId())
                    .orElseThrow(() -> new OurException("User is not in conversation"));

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

            return responseDTO;
        } catch (Exception e) {
            log.error("Error forwarding message: {}", e.getMessage(), e);
            throw e;
        }
    }

    @MessageMapping("/chat/{conversationId}/read")
    public void markAsRead(@DestinationVariable String conversationId, String userId) {
        try {
            messageApi.markMessagesAsRead(conversationId, userId);
        } catch (OurException e) {
            messagingTemplate.convertAndSend(
                    "/topic/errors." + userId,
                    e.getMessage());
        }
    }
}
