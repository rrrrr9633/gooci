package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AssignMemberCommand
 * @Description 把已有人员绑定到部门
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class AssignMemberCommand {

    @NotBlank(message = "部门不能为空")
    private String deptId;

    private List<String> userIds = new ArrayList<>();
}
