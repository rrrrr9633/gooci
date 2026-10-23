package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName OrgStructureView
 * @Description 企业组织管理页
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class OrgStructureView {

    private EnterProfileView enter;
    private List<StaffRowView> admins = new ArrayList<>();
}
