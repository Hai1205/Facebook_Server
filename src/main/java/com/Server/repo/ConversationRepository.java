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
       @Query(value = "{'isGroupChat': false, 'participants.userId': {$all: [?0, ?1]}}")
       Optional<Conversation> findConversationBetweenUsers(String userId, String otherUserId);

       Optional<Conversation> findById(String conversationId);

       @Query(value = "{'participants.userId': ?0}", sort = "{'updatedAt': -1}")
       List<Conversation> findConversationsByUserId(String userId);

       @Query(value = "{'isGroupChat': true}", sort = "{'_id': 1}")
       List<Conversation> findFirstGroupConversation(Pageable pageable);

       default Optional<Conversation> findFirstGroupConversation() {
              List<Conversation> conversations = findFirstGroupConversation(PageRequest.of(0, 1));
              return conversations.isEmpty() ? Optional.empty() : Optional.of(conversations.get(0));
       }
}