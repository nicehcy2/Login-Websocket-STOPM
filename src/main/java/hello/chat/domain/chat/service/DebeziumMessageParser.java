package hello.chat.domain.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.chat.domain.chat.dto.MessageDto;

// TODO: 추후에 무조건 리팩토링 해야됨
public class DebeziumMessageParser {

    // TODO: 추후에 공부
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static MessageDto parse(String kafkaMessageJson) throws Exception {

        // TODO: 추후에 공부
        JsonNode root = objectMapper.readTree(kafkaMessageJson);
        JsonNode payload = root.get("payload");

        if (payload == null || payload.isNull()) {
            throw new IllegalArgumentException("payload is missing or null");
        }

        return MessageDto.builder()
                .id(payload.get("message_id").asText())
                .chatRoomId(payload.get("chat_room_id").asLong())
                .senderId(payload.get("sender_id").asLong())
                .messageType(payload.get("message_type").asText())
                .content(payload.hasNonNull("content") ? payload.get("content").asText() : null)
                .timestamp(payload.hasNonNull("timestamp") ? payload.get("timestamp").asText() : null)
                .unreadCount(payload.get("unread_count").asInt())
                .publishRetryCount(payload.hasNonNull("publish_retry_count") ? payload.get("publish_retry_count").asInt() : null)
                .saveStatus(payload.hasNonNull("save_status") ? payload.get("save_status").asBoolean() : null)
                .build();
    }
}