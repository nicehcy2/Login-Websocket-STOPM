package hello.chat.domain.user.converter;

import hello.chat.domain.user.dto.UserRequestDto;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.entity.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserConverter {

    public static User toUser(UserRequestDto.JoinDto joinDto, PasswordEncoder passwordEncoder) {

        return User.builder()
                .name(joinDto.name())
                .email(joinDto.email())
                .password(passwordEncoder.encode(joinDto.password()))
                .age(joinDto.age())
                .role(UserRole.USER)
                .build();
    }
}
