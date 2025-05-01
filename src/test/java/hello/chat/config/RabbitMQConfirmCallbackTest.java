package hello.chat.config;

import hello.chat.domain.chat.dto.MessageCorrelationData;
import hello.chat.domain.chat.dto.MessageDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQConfirmCallbackTest {

    @Spy
    private RabbitTemplate rabbitTemplate; // 실제 RabbitTemplate 객체를 스파이로 생성
    @InjectMocks
    private TestRabbitConfirmCallbackConfig config; // 테스트 전용으로 만든 config를 주입 받아 사용
    private RabbitTemplate.ConfirmCallback confirmCallback;

    @BeforeEach
    void setUp() throws Exception {

        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        // 1) 실제 전송 시도를 막기 위해 convertAndSend(...)를 stub 처리
        lenient().doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(),
                any(MessageDto.class),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        // @InjectMocks에 의해 자동으로 rabbitTemplate에 ConfirmCallback이 설정된다.
        config.registerConfirmCallback(rabbitTemplate);

        // 리플렉션으로 private confirmCallback 필드 추출
        Field field = RabbitTemplate.class.getDeclaredField("confirmCallback");
        field.setAccessible(true);
        confirmCallback = (RabbitTemplate.ConfirmCallback) field.get(rabbitTemplate);

        Assertions.assertThat(confirmCallback)
                .as("ConfirmCallback이 정상적으로 등록되어야 합니다.")
                .isNotNull();
    }

    @Test
    void whenNack_thenRetry() {

        MessageDto messageDto = MessageDto.builder()
                .id("123456789")
                .chatRoomId(1L)
                .senderId(1L)
                .messageType("MESSAGE")
                .content("hello")
                .timestamp(LocalDateTime.now().toString())
                .unreadCount(0)
                .publishRetryCount(0)
                .saveStatus(false)
                .build();

        MessageCorrelationData correlationData = new MessageCorrelationData(messageDto.id(), messageDto);

        // NACK 상황 시뮬레이션
        confirmCallback.confirm(correlationData, false, "simulate-failure");

        // convertAndSend 재전송 호출 캡처
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MessageDto> dtoCap = ArgumentCaptor.forClass(MessageDto.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MessagePostProcessor> mppCap = ArgumentCaptor.forClass(MessagePostProcessor.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<CorrelationData> cdCap = ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate, times(1))
                .convertAndSend(
                        anyString(), anyString(),
                        dtoCap.capture(),
                        mppCap.capture(),
                        cdCap.capture()
                );

        // 재전송된 DTO의 retryCount가 1 증가했는지 검증
        MessageDto retried = dtoCap.getValue();
        Assertions.assertThat(retried.publishRetryCount()).isEqualTo(1);

        // MessagePostProcessor 적용 시 expiration="1000" 설정 확인
        org.springframework.amqp.core.Message mockMsg = mock(org.springframework.amqp.core.Message.class);
        org.springframework.amqp.core.MessageProperties props = new org.springframework.amqp.core.MessageProperties();
        when(mockMsg.getMessageProperties()).thenReturn(props);

        mppCap.getValue().postProcessMessage(mockMsg);
        Assertions.assertThat(props.getExpiration()).isEqualTo("1000");

        // CorrelationData에도 retryCount=1인 DTO가 담겼는지 확인
        CorrelationData usedCd = cdCap.getValue();
        Assertions.assertThat(usedCd).isInstanceOf(MessageCorrelationData.class);
        MessageDto cdDto = ((MessageCorrelationData) usedCd).getMessageDto();
        Assertions.assertThat(cdDto.publishRetryCount()).isEqualTo(1);
    }

    @Test
    void whenAck_thenDoNotRetry() {

        MessageDto messageDto = MessageDto.builder()
                .id("123456789")
                .chatRoomId(1L)
                .senderId(1L)
                .messageType("MESSAGE")
                .content("hello")
                .timestamp(LocalDateTime.now().toString())
                .unreadCount(0)
                .publishRetryCount(0)
                .saveStatus(false)
                .build();
        CorrelationData corr = new MessageCorrelationData(messageDto.id(), messageDto);

        // ACK 상황 시뮬레이션
        confirmCallback.confirm(corr, true, null);

        // 성공 시에는 convertAndSend 재전송 호출이 없어야 함
        verify(rabbitTemplate, never())
                .convertAndSend(
                        anyString(), anyString(),
                        any(MessageDto.class),
                        any(MessagePostProcessor.class),
                        any(CorrelationData.class)
                );
    }
}