package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.repo.*;
import com.Server.utils.mapper.MessageMapper;
import com.Server.utils.mapper.UserMapper;
import com.Server.exception.OurException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class MessageApi {

        private static final Logger log = LoggerFactory.getLogger(MessageApi.class);

        @Autowired
        private MessageRepository messageRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ConversationRepository conversationRepository;

        @Autowired
        private ParticipantRepository participantRepository;

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Transactional(rollbackFor = Exception.class)
        public Response sendMessage(MessageDTO messageDTO) {
                Response response = new Response();

                try {
                        log.info("Sending message: {}", messageDTO);

                        Participant participant = participantRepository
                                        .findByConversationIdAndUserId(messageDTO.getConversation().getId(),
                                                        messageDTO.getSender().getId())
                                        .orElseThrow(() -> new OurException("User is not in conversation"));

                        User sender = participant.getUser();

                        Conversation conversation = conversationRepository
                                        .findById(messageDTO.getConversation().getId())
                                        .orElseThrow(() -> new OurException("Conversation not found"));

                        if (Boolean.FALSE.equals(conversation.getIsGroupChat())) {
                                List<Participant> participants = participantRepository
                                                .findByConversationId(conversation.getId());
                                if (participants.size() == 2) {
                                        String otherUserId = participants.stream()
                                                        .map(cp -> cp.getUser().getId())
                                                        .filter(id -> !id.equals(messageDTO.getSender().getId()))
                                                        .findFirst().orElse(null);

                                        if (otherUserId != null) {
                                                User otherUser = participants.stream()
                                                                .map(Participant::getUser)
                                                                .filter(u -> u.getId().equals(otherUserId))
                                                                .findFirst().orElse(null);

                                                if (otherUser == null)
                                                        throw new OurException("User not found");
                                        }
                                }
                        }

                        Message message = new Message();
                        message.setSender(sender);
                        message.setConversation(conversation);
                        message.setContent(messageDTO.getContent());
                        message.setRead(false);

                        Message savedMessage = messageRepository.save(message);
                        log.info("Message saved: {}", savedMessage);

                        conversation.setUpdatedAt(savedMessage.getCreatedAt());
                        conversationRepository.save(conversation);

                        MessageResponseDTO messageResponseDTO = MessageMapper.mapEntityToResponseDTOFull(savedMessage);
                        messagingTemplate.convertAndSend(
                                        "/topic/conversation." + conversation.getId(),
                                        messageResponseDTO);

                        log.info("Message sent to WebSocket");

                        response.setStatusCode(200);
                        response.setMessage("get conversations for user successfully");
                        response.setMessageResponse(messageResponseDTO);
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }

        @Transactional
        public Response getConversation(String user1Id, String user2Id) {
                Response response = new Response();

                try {
                        userRepository.findById(user1Id)
                                        .orElseThrow(() -> new OurException("User 1 not found"));

                        userRepository.findById(user2Id)
                                        .orElseThrow(() -> new OurException("User 2 not found"));

                        Conversation conversation = conversationRepository
                                        .findConversationBetweenUsers(user1Id, user2Id)
                                        .orElseThrow(() -> new OurException("Conversation not found"));

                        List<Message> messages = messageRepository.findByConversationId(conversation.getId());

                        messageRepository.markMessagesAsReadInConversation(conversation.getId(), user1Id);

                        List<MessageResponseDTO> messageResponsesDTO = MessageMapper
                                        .mapListEntityToListDTOFull(messages);

                        response.setStatusCode(200);
                        response.setMessage("get conversations for user successfully");
                        response.setMessageResponses(messageResponsesDTO);
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }

        @Transactional
        public Response markMessagesAsRead(String conversationId, String userId) {
                Response response = new Response();

                try {
                        conversationRepository.findById(conversationId)
                                        .orElseThrow(() -> new OurException("Conversation not found"));

                        userRepository.findById(userId)
                                        .orElseThrow(() -> new OurException("User not found"));

                        messageRepository.markMessagesAsReadInConversation(conversationId, userId);

                        messagingTemplate.convertAndSend(
                                        "/topic/conversation." + conversationId + ".read",
                                        userId);

                        response.setStatusCode(200);
                        response.setMessage("get conversations for user successfully");
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }

        public Response getContacts(String userId) {
                Response response = new Response();

                try {
                        userRepository.findById(userId)
                                        .orElseThrow(() -> new OurException("User not found"));

                        List<String> conversationIds = participantRepository.findByUserId(userId)
                                        .stream()
                                        .map(cp -> cp.getConversation().getId())
                                        .collect(Collectors.toList());

                        if (conversationIds.isEmpty()) {
                                response.setStatusCode(200);
                                response.setMessage("User has no conversations");
                                response.setUsers(new ArrayList<>());

                                return response;
                        }

                        Set<User> contacts = new HashSet<>();
                        for (String conversationId : conversationIds) {
                                participantRepository.findByConversationId(conversationId)
                                                .stream()
                                                .map(Participant::getUser)
                                                .filter(user -> !user.getId().equals(userId))
                                                .forEach(contacts::add);
                        }

                        List<UserDTO> users = contacts.stream()
                                        .map(UserMapper::mapEntityToDTO)
                                        .collect(Collectors.toList());

                        response.setStatusCode(200);
                        response.setMessage("get contacts for user successfully");
                        response.setUsers(users);
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }

        public Response getLatestMessages(String userId) {
                Response response = new Response();

                try {
                        userRepository.findById(userId)
                                        .orElseThrow(() -> new OurException("User not found"));

                        Pageable pageable = PageRequest.of(0, 10);
                        List<Message> latestMessages = messageRepository.findLatestMessagesByUserId(userId, pageable);

                        List<MessageResponseDTO> latestMessagesDTO = MessageMapper
                                        .mapListEntityToListDTOFull(latestMessages);

                        response.setStatusCode(200);
                        response.setMessage("get contacts for user successfully");
                        response.setMessageResponses(latestMessagesDTO);
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }

        public Response countUnreadMessages(String userId) {
                Response response = new Response();

                try {
                        userRepository.findById(userId)
                                        .orElseThrow(() -> new OurException("User not found"));

                        Long count = messageRepository.countUnreadMessages(userId);

                        response.setStatusCode(200);
                        response.setMessage("get contacts for user successfully");
                        response.setUnRead(count);
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }

        @Transactional
        public Response markMessagesAsReadBySender(String senderId, String receiverId) {
                Response response = new Response();

                try {
                        userRepository.findById(senderId)
                                        .orElseThrow(() -> new OurException("Sender not found"));

                        userRepository.findById(receiverId)
                                        .orElseThrow(() -> new OurException("Receiver not found"));

                        Conversation conversation = conversationRepository
                                        .findConversationBetweenUsers(senderId, receiverId)
                                        .orElseThrow(() -> new OurException("Conversation not found"));

                        messageRepository.markMessagesAsReadInConversation(conversation.getId(), receiverId);

                        response.setStatusCode(200);
                        response.setMessage("get contacts for user successfully");
                } catch (OurException e) {
                        response.setStatusCode(400);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                } catch (Exception e) {
                        response.setStatusCode(500);
                        response.setMessage(e.getMessage());
                        System.out.println(e.getMessage());
                }

                return response;
        }
}