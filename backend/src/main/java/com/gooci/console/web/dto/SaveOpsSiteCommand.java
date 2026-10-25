package com.gooci.console.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName SaveOpsSiteCommand
 * @Description 保存官网参数
 * @author GooCi
 * @date 2026-03-26
 */
@Getter
@Setter
public class SaveOpsSiteCommand {

    @Size(max = 30, message = "客服电话过长")
    private String servicePhone;
}
