package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName OpsSessionView
 * @Description 厂商运营登录会话
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class OpsSessionView {

    private String token;

    private String userId;

    private String displayName;

    private String scope;
}
