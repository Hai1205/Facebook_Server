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

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

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

            Conversation conversation = conversationRepository.findConversationBetweenUsers(userId,
                    otherUserId).orElse(null);

            ConversationDTO conversationDTO = null;
            if (conversation != null) {
                conversation.setParticipants(participantRepository.findByConversationId(conversation.getId()));
                conversationDTO = ConservationMapper.mapEntityToDTOFull(conversation);
            } else {
                Conversation newConversation = new Conversation();
                newConversation.setName("Chat between " + user.getFullName() + " and " + otherUser.getFullName());
                conversationRepository.save(newConversation);

                Participant participantUser = createParticipant(user, newConversation);
                Participant participantOtherUser = createParticipant(otherUser, newConversation);
                newConversation.setParticipants(Arrays.asList(participantUser, participantOtherUser));

                conversationRepository.save(newConversation);

                conversationDTO = ConservationMapper.mapEntityToDTOFull(newConversation);
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

    private Participant createParticipant(User user, Conversation conversation) {
        try {
            Participant participant = new Participant(user, conversation);
            return participantRepository.save(participant);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Transactional
    public Response getUserConversations(String userId) {
        Response response = new Response();

        try {
            if (!userRepository.existsById(userId)) {
                throw new OurException("User not found");
            }

            List<Participant> userParticipants = participantRepository.findByUserId(userId);

            List<String> conversationIds = userParticipants.stream()
                    .filter(participant -> participant.getConversation() != null)
                    .map(participant -> participant.getConversation().getId())
                    .distinct()
                    .collect(Collectors.toList());

            List<Conversation> conversations = new ArrayList<>();
            for (String convId : conversationIds) {
                conversationRepository.findById(convId).ifPresent(conversations::add);
            }

            conversations.sort((c1, c2) -> {
                if (c1.getUpdatedAt() == null && c2.getUpdatedAt() == null) {
                    return 0;
                } else if (c1.getUpdatedAt() == null) {
                    return 1;
                } else if (c2.getUpdatedAt() == null) {
                    return -1;
                }
                return c2.getUpdatedAt().compareTo(c1.getUpdatedAt());
            });

            List<ConversationDTO> conversationsDTO = new ArrayList<>();
            for (Conversation conversation : conversations) {
                if (Boolean.FALSE.equals(conversation.getIsGroupChat())) {
                    List<Participant> participants = participantRepository
                            .findByConversationId(conversation.getId());
                    if (participants.size() == 2) {
                        String otherUserId = participants.stream()
                                .filter(cp -> cp.getUser() != null)
                                .map(cp -> cp.getUser().getId())
                                .filter(id -> id != null && !id.equals(userId))
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

                try {
                    List<Participant> participants = participantRepository
                            .findByConversationId(conversation.getId());

                    if (participants != null) {
                        List<ParticipantDTO> participantsDTO = ParticipantMapper
                                .mapListEntityToListDTOFull(participants);
                        conversationDTO.setParticipants(participantsDTO);
                    } else {
                        conversationDTO.setParticipants(new ArrayList<>());
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi lấy participants cho conversation " + conversation.getId() + ": "
                            + e.getMessage());
                    conversationDTO.setParticipants(new ArrayList<>());
                }

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
                            .filter(cp -> cp.getUser() != null)
                            .map(cp -> cp.getUser().getId())
                            .filter(id -> id != null && !id.equals(userId))
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

            if (messages == null) {
                messages = new ArrayList<>();
            }

            // Lọc bỏ tin nhắn null nếu có
            messages = messages.stream()
                    .filter(message -> message != null)
                    .collect(Collectors.toList());

            try {
                messageRepository.markMessagesAsReadInConversation(conversationId, userId);
            } catch (Exception e) {
                System.out.println("Lỗi khi đánh dấu tin nhắn đã đọc: " + e.getMessage());
            }

            List<MessageResponseDTO> messageResponsesDTO = MessageMapper
                    .mapListEntityToListDTOFull(messages);

            response.setStatusCode(200);
            response.setMessage("Get user messages successfully");
            response.setMessageResponses(messageResponsesDTO);
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
            System.out.println("OurException trong getMessages: " + e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Lỗi server: " + e.getMessage());
            System.out.println("Exception trong getMessages: " + e.getMessage());
            e.printStackTrace();
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
                createParticipant(user, savedGroup);
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
            createParticipant(user, group);

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