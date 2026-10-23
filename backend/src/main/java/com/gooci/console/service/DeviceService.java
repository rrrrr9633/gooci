package com.gooci.console.service;

import com.gooci.console.web.dto.DeviceAssetIdCommand;
import com.gooci.console.web.dto.DeviceBoardView;
import com.gooci.console.web.dto.DeviceFieldIdCommand;
import com.gooci.console.web.dto.DeviceHeadView;
import com.gooci.console.web.dto.DeviceImportView;
import com.gooci.console.web.dto.DeviceQuery;
import com.gooci.console.web.dto.SaveDeviceAssetCommand;
import com.gooci.console.web.dto.SaveDeviceColumnsCommand;
import com.gooci.console.web.dto.SaveDeviceFieldCommand;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName DeviceService
 * @Description 设备管理
 * @author GooCi
 * @date 2026-03-24
 */
public interface DeviceService {

    DeviceBoardView loadBoard(DeviceQuery query);

    DeviceBoardView saveDevice(SaveDeviceAssetCommand command);

    DeviceBoardView removeDevice(DeviceAssetIdCommand command);

    DeviceBoardView saveColumns(SaveDeviceColumnsCommand command);

    DeviceBoardView saveField(SaveDeviceFieldCommand command);

    DeviceBoardView removeField(DeviceFieldIdCommand command);

    DeviceBoardView syncBoard(DeviceQuery query);

    byte[] templateBytes();

    DeviceHeadView previewHeaders(MultipartFile file);

    DeviceImportView importDevice(MultipartFile file, String importMode, String mappingJson);
}
