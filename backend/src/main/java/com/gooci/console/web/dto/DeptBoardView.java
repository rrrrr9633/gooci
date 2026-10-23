package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DeptBoardView
 * @Description 部门管理页
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class DeptBoardView {

    private List<DeptNodeView> tree = new ArrayList<>();
    private DeptNodeView current;
    private String parentName;
    private SliceView<DeviceRowView> devices;
    private SliceView<StaffRowView> members;
}
