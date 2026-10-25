package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ProfileView
 * @Description 个人信息页
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class ProfileView {

    private String displayName;
    private String mobile;
    private String mailAddr;
    private String roleName;
    private String enterName;
    private List<String> deptNames = new ArrayList<>();
    private List<AppGrantView> grants = new ArrayList<>();
}
