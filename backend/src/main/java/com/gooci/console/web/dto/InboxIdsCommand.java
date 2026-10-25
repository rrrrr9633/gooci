package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName InboxIdsCommand
 * @Description 站内信批量操作
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class InboxIdsCommand {

    @NotEmpty(message = "请先选择要操作的消息！")
    private List<String> ids = new ArrayList<>();
}
