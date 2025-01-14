package hello.chat.domain.chat.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageType {
    TEXT("text"), ENTER("enter");

    private final String value;

    public static MessageType of(String value) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.value.equals(value)) {
                return messageType;
            }
        }
        throw new IllegalArgumentException();
    }
}