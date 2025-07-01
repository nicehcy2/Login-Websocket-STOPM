package hello.chat.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    public static final String LOG_ID = "LogId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI(); // 싱글톤처럼 작동하므로 지역변수를 사용하면 언된다.
        String uuid = UUID.randomUUID().toString();

        // Controller, RequestMapping을 활용한 핸들러 매핑이 오면 HandlerMethod가 넘어온다.
        // 그렇지 않은 요청이 올 경우 다른 타입을 사용해야 하므로 instanceof를 사용한다.
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler; // 호출할 컨트롤러 메서드의 모든 정보가 포함되어 있다.
        }

        log.info("REQUEST   [{}][{}][{}]", uuid, requestURI, handler);
        return true; // false면 다음 인터셉터나 핸들러로 진행 X
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle    [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        String requestURI = request.getRequestURI();
        String logId = (String) request.getAttribute(LOG_ID);

        log.info("RESPONSE  [{}][{}]", logId, requestURI);
        if (ex != null) {
            log.error("afterCompletion error!", ex);
        }
    }
}
