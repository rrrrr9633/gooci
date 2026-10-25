package com.gooci.console.web;

import com.gooci.console.common.ApiResult;
import com.gooci.console.service.OrgService;
import com.gooci.console.web.dto.AddStaffCommand;
import com.gooci.console.web.dto.AssignDeviceCommand;
import com.gooci.console.web.dto.AssignMemberCommand;
import com.gooci.console.web.dto.BankChangeView;
import com.gooci.console.web.dto.BizChangeView;
import com.gooci.console.web.dto.CertView;
import com.gooci.console.web.dto.DeptBoardView;
import com.gooci.console.web.dto.DeptIdCommand;
import com.gooci.console.web.dto.DevicePoolView;
import com.gooci.console.web.dto.EnterProfileView;
import com.gooci.console.web.dto.FileSavedView;
import com.gooci.console.web.dto.OrgStructureView;
import com.gooci.console.web.dto.PromoteAdminCommand;
import com.gooci.console.web.dto.RemoveDeviceCommand;
import com.gooci.console.web.dto.RemoveStaffCommand;
import com.gooci.console.web.dto.SaveBankChangeCommand;
import com.gooci.console.web.dto.SaveBizChangeCommand;
import com.gooci.console.web.dto.SaveDeptCommand;
import com.gooci.console.web.dto.SaveDeviceCommand;
import com.gooci.console.web.dto.SliceView;
import com.gooci.console.web.dto.StaffRowView;
import com.gooci.console.web.dto.UpdateEnterCommand;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName OrgController
 * @Description 组织管理四页
 * @author GooCi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/console/org")
public class OrgController {

    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/enterprise")
    public ApiResult<EnterProfileView> enterprise() {
        return ApiResult.ok(orgService.loadEnter());
    }

    @PostMapping("/enterprise")
    public ApiResult<EnterProfileView> updateEnterprise(@RequestBody UpdateEnterCommand command) {
        return ApiResult.ok(orgService.updateEnter(command));
    }

    @GetMapping("/structure")
    public ApiResult<OrgStructureView> structure() {
        return ApiResult.ok(orgService.loadStructure());
    }

    @GetMapping("/staff")
    public ApiResult<SliceView<StaffRowView>> staff(
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "5") long pageSize) {
        return ApiResult.ok(orgService.loadStaff(deptId, keyword, pageNo, pageSize));
    }

    @PostMapping("/admins")
    public ApiResult<Void> addAdmin(@Valid @RequestBody PromoteAdminCommand command) {
        orgService.addAdmin(command);
        return ApiResult.ok(null);
    }

    @PostMapping("/admins/remove")
    public ApiResult<Void> removeAdmin(@Valid @RequestBody RemoveStaffCommand command) {
        orgService.removeAdmin(command);
        return ApiResult.ok(null);
    }

    @GetMapping("/cert")
    public ApiResult<CertView> cert() {
        return ApiResult.ok(orgService.loadCert());
    }

    @GetMapping("/cert/biz")
    public ApiResult<BizChangeView> bizChange() {
        return ApiResult.ok(orgService.loadBizChange());
    }

    @PostMapping("/cert/biz")
    public ApiResult<BizChangeView> saveBizChange(@Valid @RequestBody SaveBizChangeCommand command) {
        return ApiResult.ok(orgService.saveBizChange(command));
    }

    @GetMapping("/cert/bank")
    public ApiResult<BankChangeView> bankChange() {
        return ApiResult.ok(orgService.loadBankChange());
    }

    @PostMapping("/cert/bank")
    public ApiResult<BankChangeView> saveBankChange(@Valid @RequestBody SaveBankChangeCommand command) {
        return ApiResult.ok(orgService.saveBankChange(command));
    }

    @PostMapping("/cert/file")
    public ApiResult<FileSavedView> uploadCertFile(@RequestParam("file") MultipartFile file) {
        return ApiResult.ok(orgService.uploadCertFile(file));
    }

    @GetMapping("/cert/file/{fileId:.+}")
    public ResponseEntity<Resource> certFile(@PathVariable String fileId) {
        Resource resource = orgService.loadCertFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .contentType(orgService.certFileType(fileId))
                .body(resource);
    }

    @GetMapping("/depts")
    public ApiResult<DeptBoardView> depts(
            @RequestParam(required = false) String deptId,
            @RequestParam(defaultValue = "1") long devicePage,
            @RequestParam(defaultValue = "1") long memberPage,
            @RequestParam(defaultValue = "5") long pageSize) {
        return ApiResult.ok(orgService.loadDeptBoard(deptId, devicePage, memberPage, pageSize));
    }

    @PostMapping("/depts")
    public ApiResult<DeptBoardView> saveDept(@Valid @RequestBody SaveDeptCommand command) {
        return ApiResult.ok(orgService.saveDept(command));
    }

    @PostMapping("/depts/disable")
    public ApiResult<DeptBoardView> disableDept(@Valid @RequestBody DeptIdCommand command) {
        return ApiResult.ok(orgService.disableDept(command));
    }

    @PostMapping("/depts/enable")
    public ApiResult<DeptBoardView> enableDept(@Valid @RequestBody DeptIdCommand command) {
        return ApiResult.ok(orgService.enableDept(command));
    }

    @PostMapping("/devices")
    public ApiResult<DeptBoardView> addDevice(@Valid @RequestBody SaveDeviceCommand command) {
        return ApiResult.ok(orgService.addDevice(command));
    }

    @GetMapping("/devices/pool")
    public ApiResult<DevicePoolView> devicePool(@RequestParam String deptId) {
        return ApiResult.ok(orgService.loadDevicePool(deptId));
    }

    @PostMapping("/devices/assign")
    public ApiResult<DeptBoardView> assignDevices(@Valid @RequestBody AssignDeviceCommand command) {
        return ApiResult.ok(orgService.assignDevices(command));
    }

    @PostMapping("/devices/remove")
    public ApiResult<DeptBoardView> removeDevice(@Valid @RequestBody RemoveDeviceCommand command) {
        return ApiResult.ok(orgService.removeDevice(command));
    }

    @PostMapping("/members")
    public ApiResult<DeptBoardView> addMember(@Valid @RequestBody AddStaffCommand command) {
        return ApiResult.ok(orgService.addMember(command));
    }

    @PostMapping("/members/assign")
    public ApiResult<DeptBoardView> assignMembers(@Valid @RequestBody AssignMemberCommand command) {
        return ApiResult.ok(orgService.assignMembers(command));
    }

    @PostMapping("/members/remove")
    public ApiResult<DeptBoardView> removeMember(@Valid @RequestBody RemoveStaffCommand command) {
        return ApiResult.ok(orgService.removeMember(command));
    }
}
