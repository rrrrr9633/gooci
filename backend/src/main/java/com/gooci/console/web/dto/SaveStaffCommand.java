package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveStaffCommand
 * @Description 新增或编辑员工
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class SaveStaffCommand {

    private String userId;

    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @NotBlank(message = "姓名不能为空")
    private String displayName;

    private String loginPassword;

    private List<String> deptIds = new ArrayList<>();

    private String remarkText;

    private String mailAddr;

    private String wechatNo;

    private String dingNo;

    private String cardNo;

    private List<StaffExtraView> extras = new ArrayList<>();
}
