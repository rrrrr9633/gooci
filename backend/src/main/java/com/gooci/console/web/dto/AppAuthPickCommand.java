package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AppAuthPickCommand
 * @Description 某个已开通 APP 勾选的角色名
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class AppAuthPickCommand {

    private String appCode;

    private List<String> roleNames = new ArrayList<>();
}
