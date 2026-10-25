package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveAppAuthCommand
 * @Description 给账号写入功能角色或数据角色。items 为本次勾选的 APP 全集
 * @author GooCi
 * @date 2026-03-25
 */
@Getter
@Setter
public class SaveAppAuthCommand {

    @NotBlank(message = "请选择人员")
    private String userId;

    @NotBlank(message = "角色类型不能为空")
    private String kind;

    private List<AppAuthPickCommand> items = new ArrayList<>();
}
