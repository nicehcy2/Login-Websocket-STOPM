package hello.chat.domain.user.service;

import hello.chat.domain.user.converter.UserConverter;
import hello.chat.domain.user.dto.UserRequestDto;
import hello.chat.domain.user.entity.User;
import hello.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void saveUser(UserRequestDto.JoinDto joinDto) {

        User user = UserConverter.toUser(joinDto, passwordEncoder);
        userRepository.save(user);
    }
}
