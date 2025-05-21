package com.Server.repo;

import com.Server.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
       @Query(value = "{ 'isGroupChat': false }")
       List<Conversation> findNonGroupConversations();

       Optional<Conversation> findById(String conversationId);

       @Query(value = "{'participants': {$elemMatch: {'user.$id': ObjectId(?0)}}}", sort = "{'updatedAt': -1}")
       List<Conversation> findConversationsByUserId(String userId);

       @Query(value = "{'isGroupChat': true}", sort = "{'_id': 1}")
       List<Conversation> findFirstGroupConversation(Pageable pageable);

       default Optional<Conversation> findFirstGroupConversation() {
              List<Conversation> conversations = findFirstGroupConversation(PageRequest.of(0, 1));
              return conversations.isEmpty() ? Optional.empty() : Optional.of(conversations.get(0));
       }

       default Optional<Conversation> findConversationBetweenUsers(String userId1, String userId2) {
              List<Conversation> nonGroupChats = findNonGroupConversations();

              for (Conversation conversation : nonGroupChats) {
                     if (conversation.getParticipants() != null && conversation.getParticipants().size() == 2) {
                            boolean hasUser1 = false;
                            boolean hasUser2 = false;

                            for (var participant : conversation.getParticipants()) {
                                   if (participant.getUser() != null) {
                                          String participantUserId = participant.getUser().getId();
                                          if (userId1.equals(participantUserId)) {
                                                 hasUser1 = true;
                                          } else if (userId2.equals(participantUserId)) {
                                                 hasUser2 = true;
                                          }
                                   }
                            }

                            if (hasUser1 && hasUser2) {
                                   return Optional.of(conversation);
                            }
                     }
              }

              return Optional.empty();
       }
}