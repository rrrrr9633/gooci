package com.gooci.console.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.ApiResult;
import com.gooci.console.domain.SysParameter;
import com.gooci.console.mapper.SysParameterMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SiteController
 * @Description 官网公开信息（客服电话预留）
 * @author GooCi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final SysParameterMapper sysParameterMapper;

    public SiteController(SysParameterMapper sysParameterMapper) {
        this.sysParameterMapper = sysParameterMapper;
    }

    @GetMapping("/meta")
    public ApiResult<Map<String, String>> meta() {
        SysParameter parameter = sysParameterMapper.selectOne(new LambdaQueryWrapper<SysParameter>()
                .eq(SysParameter::getParamCode, "SERVICE_PHONE"));
        Map<String, String> meta = new HashMap<String, String>();
        meta.put("brandName", "GOOCI 谷器");
        meta.put("servicePhone", parameter == null || parameter.getParamValue() == null ? "" : parameter.getParamValue());
        return ApiResult.ok(meta);
    }
}
