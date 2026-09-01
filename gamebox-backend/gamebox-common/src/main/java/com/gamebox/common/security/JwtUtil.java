package com.gamebox.common.security;

import com.gamebox.common.constant.AuthConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtil {

    private JwtUtil() {
    }

    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            AuthConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    public static String createToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(AuthConstants.CLAIM_USER_ID, userId)
                .claim(AuthConstants.CLAIM_USERNAME, username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + AuthConstants.TOKEN_EXPIRE_MILLIS))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 token，验签失败或过期返回 null
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
