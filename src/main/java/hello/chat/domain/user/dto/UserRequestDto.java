package hello.chat.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequestDto() {

    public record JoinDto(
            @NotBlank
            String name,
            @NotNull
            Integer age,
            @NotBlank
            @Email
            String email,
            @NotBlank
            String password
    ) { }
}
