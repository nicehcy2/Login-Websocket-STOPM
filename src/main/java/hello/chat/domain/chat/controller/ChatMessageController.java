package hello.chat.domain.chat.controller;

import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * STOMP의 가공 핸들러
 * @MessageMapping 어노테이션은 STMOP 웹 소켓 통신을 통해 메시지가 들어오면 메시지의 destination 헤더와
 * @MessageMapping에 설정된 경로가 일치한 핸들러를 찾아 해당 핸들러가 이를 처리
 * -> WebSocketBrokerConfig에서 설정한 /app prefix와 합쳐진 /app/hello 라는 destination 헤더를 가진 메시지들이 @MessageMapping("/hello") 가 붙은 핸들러를 거치게 된다.
 *
 * @SendTo 어노테이션은 핸들러에서 처리를 마친 후 결과 메시지를 설정한 경로
 */
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessageSendingOperations template;
    private final ChatMessageService messageService;

    // 채팅 리스트 반환
    // 근데 이 메서드는 계속해서 서버 DB에서 가져오므로 성능 문제가 발생함. 가급적 한번만 호출되도록 해야됨.
    @GetMapping("/chat/{chatRoomId}")
    public ResponseEntity<List<MessageDto>> getChatMessages(@PathVariable Long chatRoomId) {

        // TODO: 로그인한 회원의 ID

        // User - chatroom에서 해당 user가 구독하고 있는 채팅방의 메시지만 디비에서 가져옴.

        List<MessageDto> messageDtos = messageService.findMessages(chatRoomId);

        // 타입 변환 필요
        return ResponseEntity.ok().body(messageDtos);
    }

    // 메시지 송신 및 수신, /pub가 생략된 모습. 클라이언트 단에선 /pub/message로 요청
    // 여기서 사용자가 보낸 메시지를 해석해서 알맞은 그룹 채팅방으로 보내야됨.
    @MessageMapping("/message")
    public void receiveMessage(@RequestBody MessageDto chat) {

        // 메시지 해석
        String messageType = chat.messageType();
        Long roomId = chat.chatRoomId();

        String destination = "/sub/chatroom/" + roomId;
        // 메시지를 해당 채팅방 구독자들에게 전송
        template.convertAndSend(destination, chat);
        messageService.saveMessages(chat);
        System.out.println("ChatMessageController.receiveMessage");
    }
}
