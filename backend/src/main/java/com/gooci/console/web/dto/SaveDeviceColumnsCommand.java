package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveDeviceColumnsCommand
 * @Description 保存设备列表可见列
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class SaveDeviceColumnsCommand {

    private List<String> keys = new ArrayList<>();
}
