package hello.chat.domain.chat.repository;

import hello.chat.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

   List<ChatMessage> findChatMessagesByChatRoomId(Long id);
}
