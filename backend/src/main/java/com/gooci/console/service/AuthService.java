package com.gooci.console.service;

import com.gooci.console.web.dto.ConsoleOverview;
import com.gooci.console.web.dto.LoginCommand;
import com.gooci.console.web.dto.RegisterCommand;
import com.gooci.console.web.dto.SessionView;
import com.gooci.console.web.dto.SmsCodeCommand;
import com.gooci.console.web.dto.SmsCodeView;

/**
 * @ClassName AuthService
 * @Description 登录、注册与当前会话
 * @author GooCi
 * @date 2026-03-23
 */
public interface AuthService {

    SessionView login(LoginCommand command);

    SmsCodeView sendSms(SmsCodeCommand command);

    SessionView register(RegisterCommand command);

    SessionView currentSession();

    ConsoleOverview overview();
}
