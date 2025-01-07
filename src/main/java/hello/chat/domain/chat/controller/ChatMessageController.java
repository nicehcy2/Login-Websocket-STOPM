package hello.chat.domain.chat.controller;

import hello.chat.exception.handler.WebSocketChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatMessageController {

    private final WebSocketChatHandler webSocketChatHandler;

}
