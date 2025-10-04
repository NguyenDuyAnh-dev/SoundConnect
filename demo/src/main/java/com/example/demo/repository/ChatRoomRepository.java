package com.example.demo.repository;

import com.example.demo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p1 JOIN cr.participants p2 WHERE p1.id = :userId1 AND p2.id = :userId2 AND SIZE(cr.participants) = 2")
    Optional<ChatRoom> findOneOnOneChatRoom(@Param("userId1") String userId1, @Param("userId2") String userId2);
}
