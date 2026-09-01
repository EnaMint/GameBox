package com.gamebox.common.security;

import com.gamebox.common.constant.AuthConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从网关注入的 X-User-Id 请求头恢复当前用户，放入 ThreadLocal。
 * 各业务服务扫描 com.gamebox 包即可自动注册；网关（WebFlux）不扫描本类。
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId = request.getHeader(AuthConstants.HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                UserHolder.setUserId(Long.valueOf(userId));
            }
            filterChain.doFilter(request, response);
        } finally {
            UserHolder.clear();
        }
    }
}
