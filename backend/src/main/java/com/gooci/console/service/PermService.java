package com.gooci.console.service;

import com.gooci.console.web.dto.AppAuthBoardView;
import com.gooci.console.web.dto.AppAuthDetailView;
import com.gooci.console.web.dto.AppRoleBoardView;
import com.gooci.console.web.dto.SaveAppAuthCommand;
import com.gooci.console.web.dto.SaveAppRoleCommand;

/**
 * @ClassName PermService
 * @Description APP 功能角色、数据角色、账号授权
 * @author GooCi
 * @date 2026-03-25
 */
public interface PermService {

    AppRoleBoardView loadRoles(String kind, String appCode, long pageNo, long pageSize);

    AppRoleBoardView createRole(SaveAppRoleCommand command);

    AppAuthBoardView loadAuth(String keyword, long pageNo, long pageSize);

    AppAuthDetailView loadAuthDetail(String userId);

    AppAuthBoardView saveAuth(SaveAppAuthCommand command);
}
