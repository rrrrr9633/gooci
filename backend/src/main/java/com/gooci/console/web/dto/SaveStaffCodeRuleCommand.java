package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveStaffCodeRuleCommand
 * @Description 保存员工编号规则
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class SaveStaffCodeRuleCommand {

    @NotBlank(message = "编码名称不能为空")
    private String codeName;

    private List<StaffCodePartView> parts = new ArrayList<>();
}
