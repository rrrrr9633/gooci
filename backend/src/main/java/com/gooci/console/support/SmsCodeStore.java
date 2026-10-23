package com.gooci.console.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName SmsCodeStore
 * @Description 演示环境短信验证码（内存，不接真实短信通道）
 * @author GooCi
 * @date 2026-03-23
 */
@Component
public class SmsCodeStore {

    private static final long LIVE_MS = 5 * 60 * 1000L;

    private final ConcurrentHashMap<String, Holder> codes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String issue(String mobile) {
        purge();
        String code = String.format("%06d", random.nextInt(1_000_000));
        codes.put(mobile, new Holder(code, System.currentTimeMillis() + LIVE_MS));
        return code;
    }

    public boolean match(String mobile, String code) {
        if (mobile == null || code == null) {
            return false;
        }
        Holder holder = codes.get(mobile);
        if (holder == null || holder.expireAt < System.currentTimeMillis()) {
            codes.remove(mobile);
            return false;
        }
        return holder.code.equals(code.trim());
    }

    public void consume(String mobile) {
        codes.remove(mobile);
    }

    private void purge() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Holder>> iterator = codes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Holder> entry = iterator.next();
            if (entry.getValue().expireAt < now) {
                iterator.remove();
            }
        }
    }

    private static final class Holder {
        private final String code;
        private final long expireAt;

        private Holder(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
