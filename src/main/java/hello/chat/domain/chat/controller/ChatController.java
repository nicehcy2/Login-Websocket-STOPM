package hello.chat.domain.chat.controller;

import com.github.f4b6a3.tsid.TsidCreator;
import hello.chat.domain.chat.dto.MessageDto;
import hello.chat.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * STOMP의 가공 핸들러
 * @MessageMapping 어노테이션은 STMOP 웹 소켓 통신을 통해 메시지가 들어오면 메시지의 destination 헤더와
 * @MessageMapping에 설정된 경로가 일치한 핸들러를 찾아 해당 핸들러가 이를 처리
 * -> WebSocketBrokerConfig에서 설정한 /app prefix와 합쳐진 /app/hello 라는 destination 헤더를 가진 메시지들이 @MessageMapping("/hello") 가 붙은 핸들러를 거치게 된다.
 *
 * @SendTo 어노테이션은 핸들러에서 처리를 마친 후 결과 메시지를 설정한 경로
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService messageService;

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

    /**
     * 채팅 메시지를 특정 채팅방(roomId)으로 전송.
     * 클라이언트가 STOMP 프로토콜을 사용해 "chat.message.{roomId}" 경로로 메시지를 전송하면 처리.
     *
     * @param roomId      채팅방 ID (STOMP 경로 변수)
     * @param messageDto  전송된 메시지 데이터
     */
    @MessageMapping("chat.message.{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @RequestBody MessageDto messageDto) {

        messageService.sendMessage(messageDto); // 메시지 전송
    }

    /**
     * 클라이언트로부터 메시지를 정상적으로 수신했는지 확인하는 메서드
     * 클라이언트가 정상적으로 메시지를 수신했다면, Redis DB에 저장
     *
     * 그런데 Redis에 변경된 내용을 저장하는 과정에서 문제가 생길 가능성 떄문에 일단은 없애기로 결정
     *
     * 이걸 없애도 되는 이유가 outbox 테이블을 사용했고 이것 때문에 outbox 테이블을 적용한 의미가 없어지기 때문.
     * 전송이 실패하더라도 전송 시도를 해서 서버로 보낼 경우 DB 쪽에서 문제(즉, 저장이 되지 않는)가 발생하지 않는한
     * 동기화 과정에서 채팅 내역을 복구할 수 있기에
     *
     * 채팅 전송 과정에서 문제 시 채팅이 실시간 전송이 되지 않고, 알림도 가지 않지만
     * 네트워크 환경에서 어쩔 수 없는 상황이기에 서버 관리에 신경을 쓰자.
     */
    /*
    @MessageMapping("chat.message.ack")
    public void acknowledgeMessage(@RequestBody MessageDto messageDto) {

        messageService.saveMessage(messageDto);
    }

     */

    // 메시지 읽음 처리
    // fixme: 근데 이거 잘하면 얘도 웹소켓으로 처리해도 될듯? 프론트랑 상의 필요
    @PostMapping("/message/{chatroomId}/{messageId}/read")
    public void markAsRead(@PathVariable Long chatroomId, @PathVariable Long messageId, @RequestParam Long userId) {
        messageService.markMessageAsRead(chatroomId, messageId, userId);
    }

    // 메시지 읽은 유저 수 조회
    @GetMapping("/message/{chatroomId}/{messageId}/read/count")
    public long getReadCount(@PathVariable Long chatroomId, @PathVariable Long messageId) {
        return messageService.getReadCount(chatroomId, messageId);
    }

    // 채팅방 입장
    @MessageMapping("chat.enter.{roomId}")
    public void enterUser(@DestinationVariable("roomId") Long roomId, MessageDto messageDto) {

        MessageDto enterMessageDto = MessageDto.builder()
                .id(String.valueOf(TsidCreator.getTsid().toLong()))
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .messageType(messageDto.messageType())
                .content(messageDto.senderId() + "님이 채팅방에 입장하였습니다.")
                .timestamp(String.valueOf(LocalDateTime.now()))
                .unreadCount(0)
                .publishRetryCount(0)
                .saveStatus(false)
                .build();

        try {
            messageService.enterMessage(enterMessageDto);
        } catch (AmqpException e) {
            System.out.println("AMQP 예외");
        } catch (Exception e) {
            System.out.println("예외" + e);
        }
    }

    @MessageMapping("chat.exit.{roomId}")
    public void exitUser(@DestinationVariable("roomId") Long roomId, MessageDto messageDto) {

        MessageDto exitMessageDto = MessageDto.builder()
                .id(String.valueOf(TsidCreator.getTsid().toLong()))
                .chatRoomId(messageDto.chatRoomId())
                .senderId(messageDto.senderId())
                .messageType(messageDto.messageType())
                .content(messageDto.senderId() + "님이 채팅방에 퇴장하였습니다.")
                .timestamp(String.valueOf(LocalDateTime.now()))
                .unreadCount(0)
                .publishRetryCount(0)
                .saveStatus(false)
                .build();

        try {
            messageService.exitMessage(exitMessageDto);
        } catch (AmqpException e) {
            System.out.println("AMQP 예외");
        } catch (Exception e) {
            System.out.println("예외" + e);
        }
    }
}