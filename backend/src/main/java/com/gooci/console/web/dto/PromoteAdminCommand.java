package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName PromoteAdminCommand
 * @Description 从已有人员提升为企业管理员
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class PromoteAdminCommand {

    @NotBlank(message = "请选择人员")
    private String userId;
}
