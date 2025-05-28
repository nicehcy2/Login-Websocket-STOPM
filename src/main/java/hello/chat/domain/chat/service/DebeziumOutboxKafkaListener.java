package hello.chat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebeziumOutboxKafkaListener {

    private final ChatService chatService;

    @KafkaListener(topics = "outbox_db_server1.testdb.outbox", groupId = "test-topic-group-01")
    public void sendTest(ConsumerRecord<String, String> record) throws Exception {

        chatService.saveMessage(DebeziumMessageParser.parse(record.value()));
        log.info("send message : {}", record.value());
    }
}
