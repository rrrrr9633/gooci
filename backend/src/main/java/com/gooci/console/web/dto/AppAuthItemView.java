package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AppAuthItemView
 * @Description 账号授权列表一行
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppAuthItemView {

    private String userId;

    private String mobile;

    private String displayName;

    private String deptName;

    private String mailAddr;
}
