package com.gooci.console.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.gooci.console.security.LoginUserHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @ClassName AuditMetaHandler
 * @Description 审计字段填充
 * @author GooCi
 * @date 2026-03-23
 */
@Component
public class AuditMetaHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String userId = LoginUserHolder.getUserId();
        this.strictInsertFill(metaObject, "crtTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "crtUsrId", String.class, userId);
        this.strictInsertFill(metaObject, "updUsrId", String.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updUsrId", String.class, LoginUserHolder.getUserId());
    }
}
