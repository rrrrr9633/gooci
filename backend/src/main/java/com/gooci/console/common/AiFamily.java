package com.gooci.console.common;

import java.util.List;

/**
 * @ClassName AiFamily
 * @Description AI 功能共用一条中台门口。工作台砖块带 fn 直达，中台内目录统一开放
 * @author GooCi
 * @date 2026-03-26
 */
public final class AiFamily {

    public static final String HUB = "AI_HUB";

    public record Fn(String code, String name) {
    }

    public static final List<Fn> FUNCTIONS = List.of(
            new Fn("APP_EQKB", "AI设备知识库"),
            new Fn("APP_SAFETY", "安全生产AI行为监控"),
            new Fn("APP_RDKB", "知识库研发辅助"),
            new Fn("APP_APS", "AI排程预测"),
            new Fn("APP_VOUCHER", "AI凭证自动处理"),
            new Fn("APP_RECON", "AI自动对账"));

    private AiFamily() {
    }

    public static boolean function(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String key = code.trim();
        for (Fn fn : FUNCTIONS) {
            if (fn.code().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static String entry(String code) {
        return "/workbench/ext/" + HUB + "?fn=" + code;
    }
}
