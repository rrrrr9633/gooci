package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName StaffBoardView
 * @Description 员工管理页
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class StaffBoardView {

    private SliceView<StaffItemView> slice = new SliceView<>();
    private List<StaffColumnView> columns = new ArrayList<>();
    private List<StaffFieldView> fields = new ArrayList<>();
    private List<DeptNodeView> depts = new ArrayList<>();
    private StaffCodeRuleView codeRule = new StaffCodeRuleView();
}
