package com.gooci.console.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveOpsAppCommand
 * @Description 厂商改某企业已植入模块的隐藏与内网目标
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class SaveOpsAppCommand {

    @NotBlank(message = "请选择企业")
    private String enterId;

    @NotBlank(message = "请选择模块")
    private String appCode;

    @Size(max = 1, message = "隐藏标记不正确")
    private String hideFlag;

    @Size(max = 300, message = "目标地址过长")
    private String targetUrl;
}
