package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName ImplantOpsAppCommand
 * @Description 厂商给某企业植入模块。写开通记录，不是客户自助加购
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class ImplantOpsAppCommand {

    @NotBlank(message = "请选择企业")
    private String enterId;

    @NotBlank(message = "请填写模块代码")
    @Size(max = 40, message = "模块代码过长")
    private String appCode;

    @NotBlank(message = "请填写模块名称")
    @Size(max = 80, message = "模块名称过长")
    private String appName;

    @Size(max = 200, message = "入口路径过长")
    private String entryPath;

    @Size(max = 300, message = "目标地址过长")
    private String targetUrl;
}
