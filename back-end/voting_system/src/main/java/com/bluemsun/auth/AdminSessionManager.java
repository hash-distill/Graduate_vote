package com.bluemsun.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理员会话管理（内网轻量版）。
 *
 * 设计依据：docs/投票系统鉴权与身份投票设计.md §4
 * - 管理员口令来自配置/环境变量，此处与提交值做恒定时间比较，避免时序侧信道；
 * - 登录成功签发随机 token，会话存内存并带过期时间；
 * - 仅 1~2 名管理员使用，无需数据库；如需多实例/重启保留，可替换为 DB 存储。
 */
@Component
public class AdminSessionManager {

    /** 管理员口令（application.yml 的 app.admin.password，建议生产用环境变量覆盖）。 */
    @Value("${app.admin.password:}")
    private String adminPassword;

    /** 会话有效期：4 小时。 */
    private static final long SESSION_TTL_MILLIS = 4L * 60 * 60 * 1000;

    /** token -> 过期时间戳。 */
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    /** 校验口令是否正确（恒定时间比较）。 */
    public boolean passwordMatches(String input) {
        if (adminPassword == null || adminPassword.isEmpty() || input == null) {
            return false;
        }
        return MessageDigest.isEqual(
                input.getBytes(StandardCharsets.UTF_8),
                adminPassword.getBytes(StandardCharsets.UTF_8));
    }

    /** 创建会话并返回 token。 */
    public String createSession() {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, System.currentTimeMillis() + SESSION_TTL_MILLIS);
        return token;
    }

    /** 校验 token 是否有效（未过期）。 */
    public boolean isValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Long expireAt = sessions.get(token);
        if (expireAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireAt) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    /** 注销会话。 */
    public void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
