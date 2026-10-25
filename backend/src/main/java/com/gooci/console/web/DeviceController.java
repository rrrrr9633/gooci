package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.DeviceService;
import com.gooci.console.web.dto.DeviceAssetIdCommand;
import com.gooci.console.web.dto.DeviceBoardView;
import com.gooci.console.web.dto.DeviceFieldIdCommand;
import com.gooci.console.web.dto.DeviceHeadView;
import com.gooci.console.web.dto.DeviceImportView;
import com.gooci.console.web.dto.DeviceQuery;
import com.gooci.console.web.dto.SaveDeviceAssetCommand;
import com.gooci.console.web.dto.SaveDeviceColumnsCommand;
import com.gooci.console.web.dto.SaveDeviceFieldCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName DeviceController
 * @Description 设备管理
 * @author GooCi
 * @date 2026-03-24
 */
@RestController
@RequestMapping("/api/console/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ApiResult<DeviceBoardView> board(DeviceQuery query) {
        return ApiResult.ok(deviceService.loadBoard(query));
    }

    @PostMapping
    public ApiResult<DeviceBoardView> save(@Valid @RequestBody SaveDeviceAssetCommand command) {
        return ApiResult.ok(deviceService.saveDevice(command));
    }

    @PostMapping("/remove")
    public ApiResult<DeviceBoardView> remove(@Valid @RequestBody DeviceAssetIdCommand command) {
        return ApiResult.ok(deviceService.removeDevice(command));
    }

    @PostMapping("/columns")
    public ApiResult<DeviceBoardView> saveColumns(@RequestBody SaveDeviceColumnsCommand command) {
        return ApiResult.ok(deviceService.saveColumns(command));
    }

    @PostMapping("/fields")
    public ApiResult<DeviceBoardView> saveField(@Valid @RequestBody SaveDeviceFieldCommand command) {
        return ApiResult.ok(deviceService.saveField(command));
    }

    @PostMapping("/fields/remove")
    public ApiResult<DeviceBoardView> removeField(@Valid @RequestBody DeviceFieldIdCommand command) {
        return ApiResult.ok(deviceService.removeField(command));
    }

    @PostMapping("/sync")
    public ApiResult<DeviceBoardView> sync(DeviceQuery query) {
        return ApiResult.ok(deviceService.syncBoard(query));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template() {
        byte[] bytes = deviceService.templateBytes();
        String fileName = URLEncoder.encode("设备导入模版.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping("/import-headers")
    public ApiResult<DeviceHeadView> importHeaders(@RequestParam("file") MultipartFile file) {
        return ApiResult.ok(deviceService.previewHeaders(file));
    }

    @PostMapping("/import")
    public ApiResult<DeviceImportView> importDevice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "CREATE") String importMode,
            @RequestParam(required = false) String mappingJson) {
        return ApiResult.ok(deviceService.importDevice(file, importMode, mappingJson));
    }
}
