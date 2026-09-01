package com.gamebox.common.constant;

public final class AuthConstants {

    private AuthConstants() {
    }

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";

    /** JWT 签名密钥（HS256 要求至少 256 位） */
    public static final String JWT_SECRET = "gamebox-graduate-design-jwt-secret-2026-09-01";
    /** token 有效期：7 天 */
    public static final long TOKEN_EXPIRE_MILLIS = 7L * 24 * 60 * 60 * 1000;

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
}
