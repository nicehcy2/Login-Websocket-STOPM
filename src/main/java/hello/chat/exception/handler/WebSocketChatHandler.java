package hello.chat.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.chat.domain.chat.dto.ChatMessageDto;
import hello.chat.domain.chat.entity.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket Handler 작성
 * 소켓 통신은 서버와 클라이언트가 1:n으로 관계를 맺는다. 따라서 한 서버에 여러 클라이언트 접속 가능
 * 서버에는 여러 클라이언트가 발송한 메시지를 받아 처리해줄 핸들러가 필요
 * TextWebSocketHandler를 상속받아 핸들러를 작성
 * 클라이언트로 받은 메시지를 log로 출력하고 클라이언트로 환영 메시지를 보내줌
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

    // Jackson 라이브러리에서 제공하는 클래스로, Java 객체를 JSON 형식으로 직렬화하거나, JSON 데이터를 Java 객체로 역직렬화하는 기능
    private final ObjectMapper mapper;

    // 현재 연결된 세션들
        // 메모리 상에 현재 연결된 웹 소켓을 담고, 세션이 추가되거나 종료되면 반영
        // afterConnectionEstablished(), afterConnectionClosed()에서 반영
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // chatRoomId: {session1, session2}
    // chatRoomSessionMap: 채팅방 당 연결된 세션을 담은 Map
        // 채팅 메시지를 보낼 채팅방을 찾고, 해당 채티방에 속한 세션들에게 메시지를 전송한다.
    // TODO: 채팅방을 DB에 넣어야 함.
    private final Map<Long, Set<WebSocketSession>> chatRoomSessionMap = new HashMap<>();

    // 소켓 연결 확인
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // TODO Auto-generated method stub
        log.info("{} 연결됨", session.getId());
        sessions.add(session);
    }

    // 소켓 통신 시 메시지의 전송을 다루는 부분

    /**
     * // 웹 소켓 통신 메시지를 TextMessage로 받고, 이를 mapper로 파싱하여 ChatMessageDto로 변환
     * 채팅방 번호를 가져오고, 만약 메모리 상에 채팅방에 대한 세션이 없으면 만들어준다.
     * 메세지 ChatMessageDto의 메세지타입이 ENTER라면 채팅룸 세션에 웹소켓 클라이언트의 세션을 넣어준다.
     * 마지막에 sendMessageToChatRoom(chatMessageDto, chatRoomSession) 호출하여 해당 채팅방에 있는 모든 세션에 메세지 전송
     * @param session 웹 소켓이 연결될 떄 생기는 연결정보를 담고 있는 객체
     * @param message 웹 소켓 통신 메시지
     * @throws Exception
     */

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        log.info("payload {}", payload);

        // 페이로드 -> chatMessageDto로 변환
        // JSON -> Java 객체로 변환
        ChatMessageDto chatMessageDto = mapper.readValue(payload, ChatMessageDto.class);
        log.info("session {}", chatMessageDto.toString());

        Long chatRoomId = chatMessageDto.getChatRoomId();
        // 메모리 상에 채팅방에 대한 세션 없으면 만들어줌
        // TODO: 채팅방 DB에 저장하는 코드로 수정해야함
        if (!chatRoomSessionMap.containsKey(chatRoomId)) {
            chatRoomSessionMap.put(chatRoomId, new HashSet<>());
        }
        Set<WebSocketSession> chatRoomSession = chatRoomSessionMap.get(chatRoomId);

        // message 에 담긴 타입을 확인한다.
        // 이때 message 에서 getType 으로 가져온 내용이
        // ChatDTO 의 열거형인 MessageType 안에 있는 ENTER 과 동일한 값이라면
        if (chatMessageDto.getMessageType().equals(MessageType.ENTER)) {
            // sessions 에 넘어온 session 을 담고,
            chatRoomSession.add(session);
        }
        if (chatRoomSession.size()>=3) {
            removeClosedSession(chatRoomSession);
        }

        // 메시지 전송
        sendMessageToChatRoom(chatMessageDto, chatRoomSession);
    }

    // 소켓 종료 확인

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }

    // ====== 채팅 관련 메서드 =====
    private void removeClosedSession(Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.removeIf(sess -> !sessions.contains(sess));
    }

    private void sendMessageToChatRoom(ChatMessageDto chatMessageDto, Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.parallelStream().forEach(sess -> sendMessage(sess, chatMessageDto));
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try{
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
