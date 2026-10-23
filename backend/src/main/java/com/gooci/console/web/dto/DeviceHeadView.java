package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DeviceHeadView
 * @Description 设备导入表头预览
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceHeadView {

    private List<String> headers = new ArrayList<>();
}
