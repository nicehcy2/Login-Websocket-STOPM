package hello.chat.domain.chat.service;

import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.entity.ChatRoom;
import hello.chat.domain.chat.repository.ChatRoomRepository;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public List<MessageDto> findMessages(Long chatRoomId) {

        String redisKey = "chat:room:" + chatRoomId + ":message";
        List<Object> messgeList = redisTemplate.opsForList().range(redisKey, 0, -1);

        return messgeList.stream()
                .map(object -> (MessageDto) object)
                .toList();
    }

    @Transactional
    public void saveMessages(MessageDto messageDto) {

        User sender = userRepository.findById(messageDto.senderId() )
                .orElseThrow(() -> new RuntimeException());

        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.chatRoomId())
                .orElseThrow(() -> new RuntimeException());

        String redisKey = "chat:room:" + messageDto.chatRoomId() + ":message";
        redisTemplate.opsForList().leftPush(redisKey, messageDto);
    }
}