package com.gooci.console.service;

import com.gooci.console.web.dto.ImplantOpsAppCommand;
import com.gooci.console.web.dto.LoginCommand;
import com.gooci.console.web.dto.OpsAppRowView;
import com.gooci.console.web.dto.OpsChangeRowView;
import com.gooci.console.web.dto.OpsEnterRowView;
import com.gooci.console.web.dto.OpsIconRowView;
import com.gooci.console.web.dto.OpsSessionView;
import com.gooci.console.web.dto.OpsSiteView;
import com.gooci.console.web.dto.SaveOpsAppCommand;
import com.gooci.console.web.dto.SaveOpsSiteCommand;

import java.util.List;

/**
 * @ClassName OpsService
 * @Description 厂商运营台。与企业工作台账号隔离
 * @author GooCi
 * @date 2026-03-26
 */
public interface OpsService {

    OpsSessionView login(LoginCommand command);

    OpsSessionView session();

    OpsSiteView loadSite();

    OpsSiteView saveSite(SaveOpsSiteCommand command);

    List<OpsEnterRowView> listEnters();

    List<OpsAppRowView> listApps(String enterId);

    List<OpsAppRowView> saveApp(SaveOpsAppCommand command);

    List<OpsAppRowView> implant(ImplantOpsAppCommand command);

    List<OpsIconRowView> listIcons();

    List<OpsChangeRowView> listChanges();
}
