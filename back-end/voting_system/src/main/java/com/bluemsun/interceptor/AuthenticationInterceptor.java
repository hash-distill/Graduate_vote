package com.bluemsun.interceptor;

import com.bluemsun.auth.AdminSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bluemsun.entity.dto.ResultDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 管理员接口鉴权拦截器。
 *
 * 设计依据：docs/投票系统鉴权与身份投票设计.md §4.2
 * - 仅保护 /admin/**（除 /admin/login），由 WebConfig 注册；
 * - 校验请求头 X-Admin-Token 对应的会话是否有效；
 * - 无效统一返回 401 JSON（ResultDto），前端据此跳登录页。
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    /** 前端携带管理员会话 token 的请求头名称。 */
    public static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final AdminSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationInterceptor(AdminSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求，否则浏览器跨域调用 /admin/** 会在此被 401
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(ADMIN_TOKEN_HEADER);
        if (sessionManager.isValid(token)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ResultDto<Object> body = new ResultDto<>(false, "未登录或会话已过期，请重新登录", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
