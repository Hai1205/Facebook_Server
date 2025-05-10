package com.Server.repo;

import com.Server.entity.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    List<FriendRequest> findByTo(String userId);

    List<FriendRequest> findAll();

    @Query("{'from.$id': ObjectId(?0), 'to.$id': ObjectId(?1)}")
    FriendRequest findByFromIdAndToId(String fromId, String toId);

    @Query(value = "{$or: [{'from.$id': ObjectId(?0), 'to.$id': ObjectId(?1)}, {'from.$id': ObjectId(?1), 'to.$id': ObjectId(?0)}]}")
    List<FriendRequest> findRequestsBetweenUsers(String user1Id, String user2Id);
}
