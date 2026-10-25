package com.gooci.console.service;

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
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName StaffService
 * @Description 员工管理
 * @author GooCi
 * @date 2026-03-24
 */
public interface StaffService {

    StaffBoardView loadBoard(String keyword, String deptId, long pageNo, long pageSize);

    StaffBoardView saveStaff(SaveStaffCommand command);

    StaffBoardView removeStaff(StaffIdCommand command);

    StaffCodeRuleView saveCodeRule(SaveStaffCodeRuleCommand command);

    StaffBoardView saveColumns(SaveStaffColumnsCommand command);

    StaffBoardView saveField(SaveStaffFieldCommand command);

    StaffBoardView removeField(StaffFieldIdCommand command);

    byte[] templateBytes();

    StaffHeadView previewHeaders(MultipartFile file);

    StaffImportView importStaff(MultipartFile file, String importMode, String mappingJson);
}
