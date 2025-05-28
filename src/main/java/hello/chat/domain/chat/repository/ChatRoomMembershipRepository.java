package hello.chat.domain.chat.repository;

import hello.chat.domain.chat.entity.ChatRoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Long> {

    Optional<ChatRoomMembership> findByUserIdAndChatroomId(Long userId, Long chatRoomId);
}
