package com.gamebox.common.security;

/**
 * 当前登录用户上下文（由网关解析 JWT 后经 X-User-Id 请求头传入）
 */
public final class UserHolder {

    private UserHolder() {
    }

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 可能为 null（匿名访问）
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}
