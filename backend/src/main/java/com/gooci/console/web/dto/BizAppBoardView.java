package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName BizAppBoardView
 * @Description 工业 APP 页。含已隐藏入口，配置时用
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class BizAppBoardView {

    private List<BizAppTileView> apps = new ArrayList<>();
}
