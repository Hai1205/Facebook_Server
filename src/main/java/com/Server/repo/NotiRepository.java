package com.Server.repo;

import com.Server.entity.Noti;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotiRepository extends MongoRepository<Noti, String> {
    boolean deleteByFrom(String userId);

    List<Noti> findByTo(String userId);
}
