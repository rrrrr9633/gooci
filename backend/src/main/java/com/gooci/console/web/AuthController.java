package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.AuthService;
import com.gooci.console.web.dto.LoginCommand;
import com.gooci.console.web.dto.RegisterCommand;
import com.gooci.console.web.dto.SessionView;
import com.gooci.console.web.dto.SmsCodeCommand;
import com.gooci.console.web.dto.SmsCodeView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName AuthController
 * @Description 登录与注册
 * @author GooCi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResult<SessionView> login(@Valid @RequestBody LoginCommand command) {
        return ApiResult.ok(authService.login(command));
    }

    @PostMapping("/sms")
    public ApiResult<SmsCodeView> sms(@Valid @RequestBody SmsCodeCommand command) {
        return ApiResult.ok(authService.sendSms(command));
    }

    @PostMapping("/register")
    public ApiResult<SessionView> register(@Valid @RequestBody RegisterCommand command) {
        return ApiResult.ok(authService.register(command));
    }
}
