package com.gooci.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @ClassName ConsoleApplication
 * @Description GooCi 工业互联网控制台启动入口
 * @author GooCi
 * @date 2026-03-23
 */
@SpringBootApplication
@MapperScan("com.gooci.console.mapper")
public class ConsoleApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(ConsoleApplication.class, args);
    }

    /** Load KEY=VALUE pairs from the repo-root .env without overriding a real process environment. */
    static void loadDotEnv() {
        Path working = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path file = Files.isRegularFile(working.resolve(".env")) ? working.resolve(".env")
                : working.getParent() != null && Files.isRegularFile(working.getParent().resolve(".env"))
                        ? working.getParent().resolve(".env") : null;
        if (file == null) return;
        try {
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int split = line.indexOf('=');
                if (split <= 0) continue;
                String key = line.substring(0, split).trim();
                if (key.isEmpty() || System.getenv(key) != null || System.getProperties().containsKey(key)) continue;
                String value = unquote(line.substring(split + 1).trim());
                if (value.isEmpty()) continue;
                System.setProperty(key, value);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + file, ex);
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
