package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StaffCodeRuleView
 * @Description 员工编号规则
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffCodeRuleView {

    private String codeName;
    private String previewText;
    private List<StaffCodePartView> parts = new ArrayList<>();
}
