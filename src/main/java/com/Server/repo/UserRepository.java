package com.Server.repo;

import com.Server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findById(String  userId);

    Optional<User> findByEmail(String  email);

    Optional<User> findByUsername(String  username);

    Page<User> findAll(Pageable pageable);

    List<User> findAllByIdNotInAndIdNotIn(String currentUserId, List<String> followingAndFollowers);

    List<User> findByIdNotIn(List<String> ids);

    long countByRole(String role);

    @Query("SELECT u FROM User u ORDER BY u.followers DESC")
    List<User> findTopUsersByFollowers(Pageable pageable);
}
