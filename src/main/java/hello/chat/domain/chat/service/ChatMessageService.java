package hello.chat.domain.chat.service;

import hello.chat.domain.chat.dto.STOMPChatMessageDto;
import hello.chat.domain.chat.entity.ChatMessage;
import hello.chat.domain.chat.entity.ChatRoom;
import hello.chat.domain.chat.entity.MessageType;
import hello.chat.domain.chat.repository.ChatMessageRepository;
import hello.chat.domain.chat.repository.ChatRoomRepository;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatMessage> findMessages(Long chatRoomId) {

        return chatMessageRepository.findChatMessagesByChatRoomId(chatRoomId);
    }

    @Transactional
    public void saveMessages(STOMPChatMessageDto chat) {

        User sender = userRepository.findById(chat.getSenderId())
                .orElseThrow(() -> new RuntimeException());

        ChatRoom chatRoom = chatRoomRepository.findById(chat.getChatRoomId())
                .orElseThrow(() -> new RuntimeException());

        ChatMessage chatMessage = ChatMessage.builder()
                .user(sender)
                .chatRoom(chatRoom)
                .messageType(MessageType.TEXT) // 수정 필요
                .content(chat.getContent())
                .unreadCount(10)
                .build();

        chatMessageRepository.save(chatMessage);
    }
}