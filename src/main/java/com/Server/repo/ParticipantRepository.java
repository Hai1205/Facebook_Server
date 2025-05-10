package com.Server.repo;

import com.Server.entity.Participant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends MongoRepository<Participant, String> {
        @Query("{'conversation._id': ?0}")
        List<Participant> findByConversationId(String conversationId);

        @Query("{'user._id': ?0}")
        List<Participant> findByUserId(String userId);

        @Query(value = "{'conversation._id': ?0, 'user._id': ?1}", count = true)
        Optional<Participant> existsByConversationIdAndUserId(String conversationId, String userId);

        @Query("{'conversation._id': ?0, 'user._id': ?1}")
        Optional<Participant> findByConversationIdAndUserId(String conversationId, String userId);
}