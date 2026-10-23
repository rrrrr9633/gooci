package com.gooci.console.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName UpdateMailCommand
 * @Description 改邮箱，空串表示清空
 * @author GooCi
 * @date 2026-03-23
 */
@Getter
@Setter
public class UpdateMailCommand {

    private String mailAddr;
}
