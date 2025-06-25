    package hello.chat.domain.user.controller;

    import hello.chat.domain.user.dto.UserRequestDto;
    import hello.chat.domain.user.service.UserServiceImpl;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;

    @Slf4j
    @Controller
    @RequestMapping("users")
    @RequiredArgsConstructor
    @Validated
    public class UserController {

        private final UserServiceImpl userService;

        @GetMapping("/join")
        public String join(@ModelAttribute UserRequestDto.JoinDto joinDto) {

            return "users/join";
        }

        @PostMapping("/join")
        public String registerUser(@Validated @ModelAttribute UserRequestDto.JoinDto joinDto,
                                 BindingResult bindingResult,
                                 Model model) {

            /*
            * Validator를 상속 받는 커스텀 클래스의 validate 메서드에 아래 코드를 작성하고 해당 검증기를 주입 받아서
            * 호출하면 쉽게 검증기를 만들 수 있다.
            if (!StringUtils.hasText(joinDto.email())) {
                bindingResult.addError(new FieldError("joinDto", "email", joinDto.email(), false, null, null, "이메일은 필수입니다."));
            }
            if (!StringUtils.hasText(joinDto.name())) {
                bindingResult.addError(new FieldError("joinDto", "name", joinDto.name(), false, null, null, "이름은 필수입니다."));
            }
            if (!StringUtils.hasText(joinDto.password())) {
                bindingResult.addError(new FieldError("joinDto", "password", joinDto.password(), false, null, null, "비밀번호는 필수입니다."));
            }
            if (joinDto.age() == null || joinDto.age() > 0) {
                bindingResult.addError(new FieldError("joinDto", "age", joinDto.age(), false, null, null, "나이는 1세부터 가능합니다."));
            }*/

            if (bindingResult.hasErrors()) {
                log.error("errors = {}", bindingResult);
                return "users/join";
            }

            try {
                userService.saveUser(joinDto);
                return "redirect:/users/login";
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                return "users/join";
            }
        }

        @GetMapping("/login")
        public String login() {

            return "users/login";
        }
    }
