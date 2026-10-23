package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UpdateNameCommand
 * @Description 改姓名
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class UpdateNameCommand {

    @NotBlank(message = "姓名不能为空")
    private String displayName;
}
