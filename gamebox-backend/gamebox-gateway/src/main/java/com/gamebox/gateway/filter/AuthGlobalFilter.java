package com.gamebox.gateway.filter;

import com.gamebox.common.constant.AuthConstants;
import com.gamebox.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局认证过滤器：
 * 1. 白名单（注册/登录）与 OPTIONS 预检直接放行；
 * 2. 带 token：验签，失败返回 401；成功后删除客户端可能伪造的 X-User-Id/X-User-Name，再注入解析出的真实值；
 * 3. 无 token：GET/HEAD 允许匿名浏览，其余方法返回 401。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/register",
            "/api/auth/login"
    );

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (request.getMethod() == HttpMethod.OPTIONS || isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(AuthConstants.TOKEN_PREFIX)) {
            Claims claims = JwtUtil.parseToken(authHeader.substring(AuthConstants.TOKEN_PREFIX.length()));
            if (claims == null) {
                return writeUnauthorized(exchange, "登录已过期，请重新登录");
            }
            ServerHttpRequest mutated = request.mutate()
                    .headers(headers -> {
                        headers.remove(AuthConstants.HEADER_USER_ID);
                        headers.remove(AuthConstants.HEADER_USER_NAME);
                        headers.set(AuthConstants.HEADER_USER_ID,
                                String.valueOf(claims.get(AuthConstants.CLAIM_USER_ID)));
                        String username = claims.get(AuthConstants.CLAIM_USERNAME, String.class);
                        if (username != null) {
                            headers.set(AuthConstants.HEADER_USER_NAME,
                                    URLEncoder.encode(username, StandardCharsets.UTF_8));
                        }
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        }

        HttpMethod method = request.getMethod();
        if (method == HttpMethod.GET || method == HttpMethod.HEAD) {
            return chain.filter(exchange);
        }
        return writeUnauthorized(exchange, "请先登录");
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}")
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }
}
