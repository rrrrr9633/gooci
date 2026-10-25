package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName OpsEnterRowView
 * @Description 运营台企业一行
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class OpsEnterRowView {

    private String id;

    private String enterCode;

    private String enterName;

    private String certStatus;

    private String regionText;

    private int appCount;

    private int visibleAppCount;
}
