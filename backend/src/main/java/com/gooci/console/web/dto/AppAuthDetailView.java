package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AppAuthDetailView
 * @Description 账号授权详情
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppAuthDetailView {

    private String userId;

    private String displayName;

    private String mobile;

    private String mailAddr;

    private String roleName;

    private String deptName;

    private List<AppGrantView> grants = new ArrayList<>();
}
