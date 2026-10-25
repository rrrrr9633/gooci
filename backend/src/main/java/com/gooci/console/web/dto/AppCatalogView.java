package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AppCatalogView
 * @Description 已开通 APP 及其角色目录，供账号授权勾选
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppCatalogView {

    private String appCode;

    private String appName;

    private List<String> funcRoles = new ArrayList<>();

    private List<String> dataRoles = new ArrayList<>();
}
