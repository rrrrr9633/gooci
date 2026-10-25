package com.gooci.console.service;

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
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName OrgService
 * @Description 组织管理：企业、组织树、实名、部门
 * @author GooCi
 * @date 2026-03-23
 */
public interface OrgService {

    EnterProfileView loadEnter();

    EnterProfileView updateEnter(UpdateEnterCommand command);

    OrgStructureView loadStructure();

    SliceView<StaffRowView> loadStaff(String deptId, String keyword, long pageNo, long pageSize);

    void addAdmin(PromoteAdminCommand command);

    void removeAdmin(RemoveStaffCommand command);

    CertView loadCert();

    BizChangeView loadBizChange();

    BizChangeView saveBizChange(SaveBizChangeCommand command);

    BankChangeView loadBankChange();

    BankChangeView saveBankChange(SaveBankChangeCommand command);

    FileSavedView uploadCertFile(MultipartFile file);

    Resource loadCertFile(String fileId);

    MediaType certFileType(String fileId);

    DeptBoardView loadDeptBoard(String deptId, long devicePage, long memberPage, long pageSize);

    DeptBoardView saveDept(SaveDeptCommand command);

    DeptBoardView disableDept(DeptIdCommand command);

    DeptBoardView enableDept(DeptIdCommand command);

    DeptBoardView addDevice(SaveDeviceCommand command);

    DevicePoolView loadDevicePool(String deptId);

    DeptBoardView assignDevices(AssignDeviceCommand command);

    DeptBoardView removeDevice(RemoveDeviceCommand command);

    DeptBoardView addMember(AddStaffCommand command);

    DeptBoardView assignMembers(AssignMemberCommand command);

    DeptBoardView removeMember(RemoveStaffCommand command);
}
