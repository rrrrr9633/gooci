package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveStaffColumnsCommand
 * @Description 保存员工列表可见列
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class SaveStaffColumnsCommand {

    private List<String> keys = new ArrayList<>();
}
