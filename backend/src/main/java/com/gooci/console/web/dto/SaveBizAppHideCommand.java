package com.gooci.console.web.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SaveBizAppHideCommand
 * @Description 配置入口：按本次勾选写入隐藏标记
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class SaveBizAppHideCommand {

    @Valid
    private List<BizAppHideItem> items = new ArrayList<>();
}
