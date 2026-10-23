package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName StaffIdCommand
 * @Description 员工主键
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffIdCommand {

    @NotBlank(message = "人员不能为空")
    private String userId;
}
