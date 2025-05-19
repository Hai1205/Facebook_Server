package com.Server.repo;

import com.Server.entity.Noti;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotiRepository extends MongoRepository<Noti, String> {
    boolean deleteByFrom(String userId);

    List<Noti> findByTo(String userId);

    @Query("{'to._id': ?0, 'read': false}")
    @Update("{'$set': {'read': true}}")
    void markAllRead(String userId);
}
