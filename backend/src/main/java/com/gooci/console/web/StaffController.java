package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.StaffService;
import com.gooci.console.web.dto.SaveStaffCodeRuleCommand;
import com.gooci.console.web.dto.SaveStaffColumnsCommand;
import com.gooci.console.web.dto.SaveStaffCommand;
import com.gooci.console.web.dto.SaveStaffFieldCommand;
import com.gooci.console.web.dto.StaffBoardView;
import com.gooci.console.web.dto.StaffCodeRuleView;
import com.gooci.console.web.dto.StaffFieldIdCommand;
import com.gooci.console.web.dto.StaffHeadView;
import com.gooci.console.web.dto.StaffIdCommand;
import com.gooci.console.web.dto.StaffImportView;
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
 * @ClassName StaffController
 * @Description 员工管理
 * @author GooCi
 * @date 2026-03-24
 */
@RestController
@RequestMapping("/api/console/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ApiResult<StaffBoardView> board(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResult.ok(staffService.loadBoard(keyword, pageNo, pageSize));
    }

    @PostMapping
    public ApiResult<StaffBoardView> save(@Valid @RequestBody SaveStaffCommand command) {
        return ApiResult.ok(staffService.saveStaff(command));
    }

    @PostMapping("/remove")
    public ApiResult<StaffBoardView> remove(@Valid @RequestBody StaffIdCommand command) {
        return ApiResult.ok(staffService.removeStaff(command));
    }

    @PostMapping("/code-rule")
    public ApiResult<StaffCodeRuleView> saveCodeRule(@Valid @RequestBody SaveStaffCodeRuleCommand command) {
        return ApiResult.ok(staffService.saveCodeRule(command));
    }

    @PostMapping("/columns")
    public ApiResult<StaffBoardView> saveColumns(@RequestBody SaveStaffColumnsCommand command) {
        return ApiResult.ok(staffService.saveColumns(command));
    }

    @PostMapping("/fields")
    public ApiResult<StaffBoardView> saveField(@Valid @RequestBody SaveStaffFieldCommand command) {
        return ApiResult.ok(staffService.saveField(command));
    }

    @PostMapping("/fields/remove")
    public ApiResult<StaffBoardView> removeField(@Valid @RequestBody StaffFieldIdCommand command) {
        return ApiResult.ok(staffService.removeField(command));
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> template() {
        byte[] bytes = staffService.templateBytes();
        String fileName = URLEncoder.encode("员工导入模版.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @PostMapping("/import-headers")
    public ApiResult<StaffHeadView> importHeaders(@RequestParam("file") MultipartFile file) {
        return ApiResult.ok(staffService.previewHeaders(file));
    }

    @PostMapping("/import")
    public ApiResult<StaffImportView> importStaff(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "CREATE") String importMode,
            @RequestParam(required = false) String mappingJson) {
        return ApiResult.ok(staffService.importStaff(file, importMode, mappingJson));
    }
}
