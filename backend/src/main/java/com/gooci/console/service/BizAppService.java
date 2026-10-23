package com.gooci.console.service;

import com.gooci.console.web.dto.BizAppBoardView;
import com.gooci.console.web.dto.SaveBizAppAdminCommand;
import com.gooci.console.web.dto.SaveBizAppHideCommand;

/**
 * @ClassName BizAppService
 * @Description 工业 APP 快捷入口。只列已植入且带入口的模块
 * @author GooCi
 * @date 2026-03-26
 */
public interface BizAppService {

    BizAppBoardView loadBoard();

    BizAppBoardView saveHide(SaveBizAppHideCommand command);

    BizAppBoardView saveAdmin(SaveBizAppAdminCommand command);
}
