package com.gooci.console.service;

import com.gooci.console.web.dto.ProfileView;
import com.gooci.console.web.dto.UpdateMailCommand;
import com.gooci.console.web.dto.UpdateNameCommand;
import com.gooci.console.web.dto.UpdatePasswordCommand;

/**
 * @ClassName ProfileService
 * @Description 当前用户个人信息
 * @author GooCi
 * @date 2026-03-23
 */
public interface ProfileService {

    ProfileView loadProfile();

    ProfileView updateName(UpdateNameCommand command);

    ProfileView updateMail(UpdateMailCommand command);

    void updatePassword(UpdatePasswordCommand command);
}
