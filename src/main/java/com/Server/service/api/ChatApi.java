package com.Server.service.api;

import com.Server.dto.ConversationDTO;
import com.Server.dto.MessageResponseDTO;
import com.Server.dto.ParticipantDTO;
import com.Server.dto.Response;
import com.Server.dto.UserDTO;
import com.Server.entity.Conversation;
import com.Server.entity.Message;
import com.Server.entity.Participant;
import com.Server.entity.User;
import com.Server.exception.OurException;
import com.Server.repo.*;
import com.Server.utils.mapper.ConservationMapper;
import com.Server.utils.mapper.MessageMapper;
import com.Server.utils.mapper.ParticipantMapper;
import com.Server.utils.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatApi {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Transactional
    public Response getOrCreateConversation(String userId, String otherUserId) {
        Response response = new Response();

        try {
            if (userId.equals(otherUserId)) {
                throw new OurException("You can`t chat yourself");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found"));
            User otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new OurException("Other user not found"));

            Optional<Conversation> existingConversation = conversationRepository.findConversationBetweenUsers(userId,
                    otherUserId);

            ConversationDTO conversationDTO = null;
            if (existingConversation.isPresent()) {
                Conversation conversation = existingConversation.get();
                conversation.setParticipants(participantRepository.findByConversationId(conversation.getId()));
                conversationDTO = ConservationMapper.mapEntityToDTOFull(conversation);
            } else {
                Conversation newConversation = new Conversation();
                newConversation.setName("Chat between " + user.getFullName() + " and " + otherUser.getFullName());
                newConversation.setIsGroupChat(false);
                Conversation savedConversation = conversationRepository.save(newConversation);
                addUserToConversation(savedConversation, user);
                addUserToConversation(savedConversation, otherUser);
                savedConversation
                        .setParticipants(participantRepository.findByConversationId(savedConversation.getId()));
                conversationDTO = ConservationMapper.mapEntityToDTOFull(savedConversation);
            }

            response.setStatusCode(200);
            response.setMessage("get conversations for user successfully");
            response.setConversation(conversationDTO);
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

    private Response addUserToConversation(Conversation conversation, User user) {
        Response response = new Response();

        try {
            participantRepository.existsByConversationIdAndUserId(conversation.getId(), user.getId())
                    .orElseThrow(() -> new OurException("Participant not found"));

            Participant participant = new Participant();
            participant.setConversation(conversation);
            participant.setUser(user);

            participantRepository.save(participant);

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

    @Transactional
    public Response getUserConversations(String userId) {
        Response response = new Response();

        try {
            if (!userRepository.existsById(userId)) {
                throw new OurException("User not found");
            }

            List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);
            List<ConversationDTO> conversationsDTO = new ArrayList<>();
            for (Conversation conversation : conversations) {
                if (Boolean.FALSE.equals(conversation.getIsGroupChat())) {
                    List<Participant> participants = participantRepository
                            .findByConversationId(conversation.getId());
                    if (participants.size() == 2) {
                        String otherUserId = participants.stream()
                                .map(cp -> cp.getUser().getId())
                                .filter(id -> !id.equals(userId))
                                .findFirst().orElse(null);

                        if (otherUserId != null) {
                            User user = userRepository.findById(userId).orElse(null);
                            User otherUser = userRepository.findById(otherUserId).orElse(null);
                            if (user == null || otherUser == null)
                                continue;
                        }
                    }
                }

                ConversationDTO conversationDTO = ConservationMapper.mapEntityToDTOFull(conversation);
                List<Participant> participants = participantRepository
                        .findByConversationId(conversation.getId());
                List<ParticipantDTO> participantsDTO = ParticipantMapper.mapListEntityToListDTOFull(participants);
                conversationDTO.setParticipants(participantsDTO);

                conversationsDTO.add(conversationDTO);
            }

            response.setStatusCode(200);
            response.setMessage("get conversations for user successfully");
            response.setConversations(conversationsDTO);
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

    public Response getMessages(String conversationId, String userId, int page, int size) {
        Response response = new Response();

        try {
            Conversation conversation = conversationRepository
                    .findById(conversationId)
                    .orElseThrow(() -> new OurException("Conversation not found"));

            userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found"));

            participantRepository
                    .findByConversationIdAndUserId(conversationId,
                            userId)
                    .orElseThrow(() -> new OurException("User is not in conversation"));

            if (Boolean.FALSE.equals(conversation.getIsGroupChat())) {
                List<Participant> participants = participantRepository.findByConversationId(conversationId);
                if (participants.size() == 2) {
                    String otherUserId = participants.stream()
                            .map(cp -> cp.getUser().getId())
                            .filter(id -> !id.equals(userId))
                            .findFirst().orElse(null);

                    if (otherUserId != null) {
                        userRepository.findById(userId)
                                .orElseThrow(() -> new OurException("User not found"));

                        userRepository.findById(otherUserId)
                                .orElseThrow(() -> new OurException("Other user not found"));
                    }
                }
            }

            Pageable pageable = PageRequest.of(page, size);
            List<Message> messages = messageRepository.findByConversationIdWithPagination(conversationId, pageable);

            messageRepository.markMessagesAsReadInConversation(conversationId, userId);

            List<MessageResponseDTO> messageResponsesDTO = MessageMapper
                    .mapListEntityToListDTOFull(messages);

            response.setStatusCode(200);
            response.setMessage("Get user messages successfully");
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

    public Response getGroupConversation() {
        Response response = new Response();

        try {
            Conversation conversation = conversationRepository.findFirstGroupConversation()
                    .orElseGet(() -> {
                        Conversation newConversation = new Conversation();
                        newConversation.setName("Group Chat");
                        newConversation.setIsGroupChat(true);

                        return conversationRepository.save(newConversation);
                    });

            ConversationDTO conversationDTO = ConservationMapper
                    .mapEntityToDTO(conversation);

            response.setStatusCode(200);
            response.setMessage("get conversation for user successfully");
            response.setConversation(conversationDTO);
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
    public Response getConversation(String conversationId, String userId) {
        Response response = new Response();

        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new OurException("Conversation not found"));

            ConversationDTO conversationDTO = ConservationMapper.mapEntityToDTOFull(conversation);
            if (conversationDTO.getParticipants() == null) {
                conversationDTO.setParticipants(new ArrayList<>());
            }
            List<Participant> participants = participantRepository.findByConversationId(conversationId);
            List<ParticipantDTO> participantsDTO = new ArrayList<>();

            for (Participant participant : participants) {
                User user = participant.getUser();
                ParticipantDTO participantDTO = new ParticipantDTO();

                participantDTO.setId(participant.getId());
                participantDTO.setUser(UserMapper.mapEntityToDTOFull(user));
                participantsDTO.add(participantDTO);
            }

            conversationDTO.setParticipants(participantsDTO);

            response.setStatusCode(200);
            response.setMessage("get conversation for user successfully");
            response.setConversation(conversationDTO);
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

    public Response getUsersWithConversation(String userId) {
        Response response = new Response();

        try {
            List<Participant> participants = participantRepository.findByUserId(userId);

            List<String> conversationIds = participants.stream()
                    .map(p -> p.getConversation().getId())
                    .collect(Collectors.toList());

            List<User> users = new ArrayList<>();
            for (String conversationId : conversationIds) {
                Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
                if (conversation == null || conversation.getIsGroupChat())
                    continue;

                List<Participant> others = participantRepository.findByConversationId(conversationId)
                        .stream()
                        .filter(p -> !p.getUser().getId().equals(userId))
                        .collect(Collectors.toList());

                for (Participant other : others) {
                    users.add(other.getUser());
                }
            }

            users.stream().distinct().collect(Collectors.toList());

            List<UserDTO> usersDTO = UserMapper.mapListEntityToListDTOFull(users);

            response.setStatusCode(200);
            response.setMessage("get conversation for user successfully");
            response.setUsers(usersDTO);
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

    public Response createGroupConversation(String groupName, List<String> userIds) {
        Response response = new Response();

        try {
            if (userIds == null || userIds.size() < 2) {
                throw new OurException("at least 2 members");
            }

            Conversation group = new Conversation();
            group.setName(groupName);
            group.setIsGroupChat(true);
            Conversation savedGroup = conversationRepository.save(group);

            for (String userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new OurException("not found: " + userId));
                addUserToConversation(savedGroup, user);
            }
            savedGroup.setParticipants(participantRepository.findByConversationId(savedGroup.getId()));

            ConversationDTO conversationDTO = ConservationMapper
                    .mapEntityToDTO(savedGroup);

            response.setStatusCode(200);
            response.setMessage("get conversation for user successfully");
            response.setConversation(conversationDTO);
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

    public Response addUserToGroup(String conversationId, String userId) {
        Response response = new Response();

        try {
            Conversation group = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new OurException("Conversation not found"));
            if (!group.getIsGroupChat())
                throw new OurException("Not a group chat");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found"));
            addUserToConversation(group, user);

            response.setStatusCode(200);
            response.setMessage("get conversation for user successfully");
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

    public Response deleteUserFromGroup(String conversationId, String userId) {
        Response response = new Response();

        try {
            Participant participant = participantRepository
                    .findByConversationIdAndUserId(conversationId, userId)
                    .orElseThrow(() -> new OurException("User not in group"));
            participantRepository.delete(participant);

            response.setStatusCode(200);
            response.setMessage("get conversation for user successfully");
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