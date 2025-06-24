package hello.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity // Spring Security 설정 활성화
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(requests -> requests // HTTP 요청에 대한 접근 제어
                        .requestMatchers("/", "/home", "/users/join", "/webjars/bootstrap/5.3.3/css/**").permitAll() // 인증 없이 접근 가능한 경로
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 특정 역할을 가진 사용자만 접근 가능
                        .anyRequest().authenticated()) // 그 외 모든 요청에 대한 인증만 요구
                .formLogin(form -> form
                        .loginPage("/users/login") // 커스텀 로그인 페이지를 /login 경로로 지정
                        .loginProcessingUrl("/users/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/users/login?error")
                        .usernameParameter("email")
                        .permitAll()) // 로그인 페이지는 모든 사용자가 접근 가능
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
