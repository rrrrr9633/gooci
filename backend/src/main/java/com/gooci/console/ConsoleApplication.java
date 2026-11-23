package com.gooci.console;

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
        SpringApplication.run(ConsoleApplication.class, args);
    }
}
