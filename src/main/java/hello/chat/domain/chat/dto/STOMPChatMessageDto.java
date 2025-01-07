package hello.chat.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class STOMPChatMessageDto {

    private Long id;
    private String name;
    private String message;
}
