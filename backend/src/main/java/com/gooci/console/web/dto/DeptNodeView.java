package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName DeptNodeView
 * @Description 部门树节点
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class DeptNodeView {

    private String id;
    private String parentId;
    private String deptName;
    private String deptDesc;
    private String enableFlag;
}
