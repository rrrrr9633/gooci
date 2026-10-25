package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SessionView
 * @Description 登录后会话
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class SessionView {

    private String token;
    private String userId;
    private String displayName;
    private String mobile;
    private String roleName;
    private String enterId;
    private String enterName;
    private String regionText;
    private String certStatus;
    private String servicePhone;
    private List<MenuView> menus = new ArrayList<MenuView>();
}
