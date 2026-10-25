package com.gooci.console.security;

/**
 * @ClassName LoginUserHolder
 * @Description 当前登录用户
 * @author GooCi
 * @date 2026-03-23
 */
public final class LoginUserHolder {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<String>();
    private static final ThreadLocal<String> SCOPE = new ThreadLocal<String>();

    private LoginUserHolder() {
    }

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static void setScope(String scope) {
        SCOPE.set(scope);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static String getScope() {
        String scope = SCOPE.get();
        return scope == null || scope.isBlank() ? "enter" : scope;
    }

    public static boolean ops() {
        return "ops".equals(getScope());
    }

    public static void clear() {
        USER_ID.remove();
        SCOPE.remove();
    }
}
