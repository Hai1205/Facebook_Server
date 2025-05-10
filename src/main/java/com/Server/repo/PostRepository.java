package com.Server.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;

import com.Server.entity.Post;

import java.util.List;
import java.util.Set;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findAllByOrderByCreatedAtDesc();

    @Query(value = "{}", sort = "{'likes': -1}")
    List<Post> findTopPostsByLikes(Pageable pageable);

    List<Post> findByUserIdInOrderByCreatedAtDesc(Set<String> userIds);
}
