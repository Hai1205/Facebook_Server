package com.Server.repo;

import com.Server.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
       // Lấy tất cả tin nhắn giữa 2 người dùng, sắp xếp theo thời gian
       @Query("{'conversation._id': {$in: ?0}}")
       List<Message> findMessagesByConversationIds(List<String> conversationIds);

       // Phương thức tìm cuộc trò chuyện giữa hai người
       List<Message> findByConversation_IdOrderByCreatedAtAsc(String conversationId);

       // Lấy danh sách người dùng đã trao đổi tin nhắn với user
       @Query(value = "{'conversation.participants.userId': ?0}", fields = "{'conversation.participants': 1}")
       List<Message> findConversationParticipantsByUserId(String userId);

       // Lấy tin nhắn mới nhất cho mỗi cuộc trò chuyện của user
       @Query(value = "{}", sort = "{'createdAt': -1}")
       List<Message> findLatestMessagesByUserId(String userId, Pageable pageable);

       // Đếm số lượng tin nhắn chưa đọc
       @Query(value = "{'conversation.participants.userId': ?0, 'sender._id': {$ne: ?0}, 'isRead': false}", count = true)
       Long countUnreadMessages(String userId);

       @Query("{'conversation._id': ?0}")
       List<Message> findByConversationId(String conversationId);

       @Query(value = "{'conversation._id': ?0}", sort = "{'createdAt': -1}")
       List<Message> findByConversationIdWithPagination(String conversationId, Pageable pageable);

       @Query(value = "{'conversation._id': ?0, 'sender._id': {$ne: ?1}, 'isRead': false}", count = true)
       Long countUnreadMessagesInConversation(String conversationId, String userId);

       @Transactional
       @Query(value = "{'conversation._id': ?0, 'sender._id': {$ne: ?1}, 'isRead': false}", fields = "{'isRead': true}")
       void markMessagesAsReadInConversation(String conversationId, String userId);

       @Query(value = "{'conversation._id': ?0, 'sender._id': ?1, 'content': ?2, 'createdAt': {$gt: ?3}}", sort = "{'createdAt': -1}")
       List<Message> findRecentMessagesByConversationAndSender(
                     String conversationId,
                     String senderId,
                     String content,
                     LocalDateTime timestamp);
}