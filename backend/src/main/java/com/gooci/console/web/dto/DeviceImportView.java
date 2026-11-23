package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DeviceImportView
 * @Description 设备导入结果
 * @author GooCi
 * @date 2026-03-24
 */
@Getter
@Setter
public class DeviceImportView {

    private int createdCount;
    private int updatedCount;
    private int skippedCount;
    private int failedCount;
    private List<String> messages = new ArrayList<>();
}
