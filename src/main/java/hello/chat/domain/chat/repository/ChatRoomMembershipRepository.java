package hello.chat.domain.chat.repository;

import hello.chat.domain.chat.entity.ChatRoom;
import hello.chat.domain.chat.entity.ChatRoomMembership;
import hello.chat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomMembershipRepository extends JpaRepository<ChatRoomMembership, Long> {

    Optional<ChatRoomMembership> findByUserAndChatRoom(User user, ChatRoom chatRoom);
}
